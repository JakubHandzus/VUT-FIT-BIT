/**
 * 	\file fsm.cpp
 * 	\brief Konecny automat primajuci POP3 protokolu
 * 	\author Jakub Handzus xhandz00
 * 	
 * 	Projekt: ISA - Programovanie sietovej sluzby
 * 	Tema: POP3 Server
 * 	Rocnik: 2017/18
 */

 #include "popser.h"

/**
 * @brief      Konecny automat, odpovedajuci poziadavkam POP3.
 *
 * @param      tmp_struct  Struktura t_thread, v ktorej su ulozene vstupne argumenty
 * 			   + meno a heslo pouzivatela
 * 			   Funkcia pracuje aj s globalnymi premennymi ktore upravuje
 *
 * @return     
 */
void *pop3_fsm(void *tmp_struct) {

	t_thread thread = *(t_thread*)tmp_struct;	// pretypovanie vstupneho argumentu


	// zistenie filedescriptoru soketu pre vlakno
	// mutex caka, kym sa pri vytvarani vlakna zapise informacia do globalnej mapy
	mtx_thread.lock();
	int connectfd = glob_thread_map.find(pthread_self())->second;
	mtx_thread.unlock();

	string timestamp = glob_fd_map.find(connectfd)->second;

	int cmd;				// cislo prikazu od klienta
	int state = sAUTH;		// pociatocny stav
	string msg;				// sprava od/pre klienta
	std::vector<t_file> dir_vec;	// vektor informacii o suboroch adresara
	char * strtol_error;	// errorova premenna pri premienani na cislo
	string recv_msg = rcv_msg(connectfd, false);	// prijatie prvej spravy od klienta
	// docasne premenne pre rozne pouzitie
	long tmp_long = 0;
	long tmp_long2 = 0;
	int tmp_int = 0;
	string tmp_string = "";

	// zaciatok konecneho automatu
	while (state != sEND) {

		// rozpoznanie prikazu a argumentu spravy od klienta
		cmd_recognition(recv_msg, &cmd, &msg);

		switch (state) {
			
			/****************************** Autorizacna faza ********************************/
			case sAUTH:
				if (cmd == cmd_APOP && !thread.arg.b_clear) {
					state = sAPOP;
				}
				else if (cmd == cmd_USER && thread.arg.b_clear) {
					state = sUSER;
				}
				else if (cmd == cmd_QUIT && msg == "") {
					state = sQUIT;
				}
				else {
					recv_msg = send_and_recv("-ERR invalid command\r\n", connectfd, false);
					state = sAUTH;
				}
				break;

			/*------------------------- QUIT --------------------------*/
			case sQUIT:
				// Odoslanie spravy, zatvorenie file descriptor a mymazanie ho z mapy
				msg = "+OK POP3 server signing off\r\n";
				send(connectfd, msg.c_str(), msg.length(), 0);
				close(connectfd);
				glob_fd_map.erase(connectfd);
				state = sEND;
				break;

			/*------------------------- USER --------------------------*/
			case sUSER:
				// bol zadany prikaz USER
				tmp_string = msg;
				recv_msg = send_and_recv("+OK please give password for name \"" + tmp_string + "\"\r\n", connectfd, false);
				state = sPASS;
				break;

			/*------------------------- PASS --------------------------*/
			case sPASS:
				// caka na prikaz PASS
				if (cmd == cmd_PASS) {
					if (tmp_string == thread.auth.username && msg == thread.auth.password) {	// ak je spravne heslo
						if (mtx.try_lock()) {			// pozriet na zdroje
							if (!new_to_cur(thread.arg.maildir, false)) {
								dir_vec = check_mailbox(thread.arg.maildir);	//zisti informacie o mailoch v cur
								num_and_octets(&dir_vec, &tmp_long, &tmp_int);
								recv_msg = send_and_recv("+OK " + thread.auth.username + "'s maildrop has " +
									to_string(tmp_int) + " messages (" + to_string(tmp_long) + " octets)\r\n", connectfd, true);
								state = sTRANS;
							}
							else {	// chyba vo funkcie new_to_cur
								mtx.unlock();
								recv_msg = send_and_recv("-ERR in Maildir structure\r\n", connectfd, false);
								state = sAUTH;
							}
						}
						else {	// ak niesu zdroje
							recv_msg = send_and_recv("-ERR unable to lock maildrop\r\n", connectfd, false);
							state = sAUTH;
						}
					}
					else {		// zle meno alebo heslo
						recv_msg = send_and_recv("-ERR invalid username or password\r\n", connectfd, false);
						state = sAUTH;
					}
				}
				else if (cmd == cmd_QUIT && msg == "") {
					state = sQUIT;
				}
				else {
					state = sAUTH;
				}
				break;

			/*------------------------- APOP --------------------------*/
			case sAPOP:
				// bol zadany prikaz APOP
				if (msg == (thread.auth.username + " " + md5(timestamp + thread.auth.password))) {		// spravne heslo
					if (mtx.try_lock()) {		// pozriet na zdroje
						if (!new_to_cur(thread.arg.maildir, false)) {
							dir_vec = check_mailbox(thread.arg.maildir);	//zisti informacie o mailoch v cur
							num_and_octets(&dir_vec, &tmp_long, &tmp_int);
							recv_msg = send_and_recv("+OK " + thread.auth.username + "'s maildrop has " +
								to_string(tmp_int) + " messages (" + to_string(tmp_long) + " octets)\r\n", connectfd, true);
							state = sTRANS;
						}
						else {	// chyba vo funkcie new_to_cur
							mtx.unlock();
							recv_msg = send_and_recv("-ERR in Maildir structure\r\n", connectfd, false);
							state = sAUTH;
						}
					}
					else {		// nie su zdroje
						recv_msg = send_and_recv("-ERR unable to lock maildrop\r\n", connectfd, false);
						state = sAUTH;
					}
				}
				else {		// zle heslo
					recv_msg = send_and_recv("-ERR permission denied\r\n", connectfd, false);
					state = sAUTH;
				}
				break;

			/****************************** Transakcna faza ********************************/
			case sTRANS:

				switch (cmd) {

					/*------------------------- NOOP --------------------------*/
					case cmd_NOOP:
						recv_msg = send_and_recv("+OK\r\n", connectfd, true);
						break;

					/*------------------------- QUIT --------------------------*/
					case cmd_QUIT:
						state = sUPDATE;
						break;

					/*------------------------- STAT --------------------------*/
					case cmd_STAT:
						num_and_octets(&dir_vec, &tmp_long, &tmp_int);
						recv_msg = send_and_recv("+OK " + to_string(tmp_int) + " " + to_string(tmp_long) + "\r\n", connectfd, true);
						break;

					/*------------------------- DELE --------------------------*/
					case cmd_DELE:
						// prevod cisla
						tmp_long = strtol(msg.c_str(), &strtol_error, 10);
						if (*strtol_error != '\0' || msg.length() == 0) {
							recv_msg = send_and_recv("-ERR invalid number\r\n", connectfd, true);
						}
						else {
							if (tmp_long -1 < 0 || dir_vec.size() < (unsigned) tmp_long) {
								recv_msg = send_and_recv("-ERR no such message\r\n", connectfd, true);
							}
							else if (dir_vec.at(tmp_long -1).to_be_del == true) {
								recv_msg = send_and_recv("-ERR message " + to_string(tmp_long) + " already deleted\r\n", connectfd, true);
							}
							else if (dir_vec.at(tmp_long -1).to_be_del == false) {
								dir_vec.at(tmp_long -1).to_be_del = true;
								recv_msg = send_and_recv("+OK message " + to_string(tmp_long) + " deleted\r\n", connectfd, true);
							}
							else {
								recv_msg = send_and_recv("-ERR unexpected error\r\n", connectfd, true);
							}
						}
						break;

					/*------------------------- LIST + cislo --------------------------*/
					case cmd_LIST_opt:
						// prevod cisla
						tmp_long = strtol(msg.c_str(), &strtol_error, 10);
						if (*strtol_error != '\0' || msg.length() == 0) {
							recv_msg = send_and_recv("-ERR invalid number\r\n", connectfd, true);
						}
						else {
							// Osetrenie hranic + osetrenie na vymazanu spravu
							if (tmp_long -1 < 0 || dir_vec.size() < (unsigned) tmp_long || dir_vec.at(tmp_long -1).to_be_del == true) {
								recv_msg = send_and_recv("-ERR no such message\r\n", connectfd, true);
							}
							else if (dir_vec.at(tmp_long -1).to_be_del == false) {
								recv_msg = send_and_recv("+OK " + to_string(tmp_long) + " " + to_string(dir_vec.at(tmp_long -1).file_size) + "\r\n", connectfd, true);
							}
							else {
								recv_msg = send_and_recv("-ERR unexpected error\r\n", connectfd, true);
							}
						}
						break;
						
					/*------------------------- LIST --------------------------*/
					case cmd_LIST:
						num_and_octets(&dir_vec, &tmp_long, &tmp_int);
						tmp_string = "+OK " + to_string(tmp_int) + " messages (" + to_string(tmp_long) + " octets)\r\n";	// prvy riadok
						tmp_string += cmd_list(&dir_vec);
						recv_msg = send_and_recv(tmp_string, connectfd, true);
						break;

					/*------------------------- RETR --------------------------*/
					case cmd_RETR:
						// prevod cisla
						tmp_long = strtol(msg.c_str(), &strtol_error, 10);
						if (*strtol_error != '\0' || msg.length() == 0) {
							recv_msg = send_and_recv("-ERR invalid number\r\n", connectfd, true);
						}
						else {
							// Osetrenie hranic + osetrenie na vymazanu spravu
							if (tmp_long -1 < 0 || dir_vec.size() < (unsigned) tmp_long || dir_vec.at(tmp_long -1).to_be_del == true) {
								recv_msg = send_and_recv("-ERR no such message\r\n", connectfd, true);
							}
							else if (dir_vec.at(tmp_long -1).to_be_del == false) {
								// ziskanie obsahu
								tmp_string = cmd_retr(&dir_vec.at(tmp_long -1), thread.arg.maildir);
								recv_msg = send_and_recv(tmp_string, connectfd, true);

							}
							else {
								recv_msg = send_and_recv("-ERR unexpected error\r\n", connectfd, true);
							}
						}
						break;

					/*------------------------- RSET --------------------------*/
					case cmd_RSET:
						cmd_reset(&dir_vec, &tmp_long, &tmp_int);
						recv_msg = send_and_recv("+OK maildrop has " + to_string(tmp_int) +
							" messages (" + to_string(tmp_long) + " octets)\r\n", connectfd, true);
						break;

					/*------------------------- TOP --------------------------*/
					case cmd_TOP:
						if (msg.find(" ") != string::npos) {
							// overenie prveho cisla
							tmp_string = msg.substr(0, msg.find(" "));
							tmp_long = strtol(tmp_string.c_str(), &strtol_error, 10);
							if (*strtol_error != '\0' || tmp_string.length() == 0) {
								recv_msg = send_and_recv("-ERR invalid number\r\n", connectfd, true);
							}
							// Osetrenie hranic + osetrenie na vymazanu spravu
							else if (tmp_long -1 < 0 || dir_vec.size() < (unsigned) tmp_long || dir_vec.at(tmp_long -1).to_be_del == true) {
								recv_msg = send_and_recv("-ERR no such message\r\n", connectfd, true);
							}
							// sprava najdena
							else if (dir_vec.at(tmp_long -1).to_be_del == false) {
								// overenie druheho cisla
								tmp_string = msg.substr(msg.find(" ")+1);
								tmp_long2 = strtol(tmp_string.c_str(), &strtol_error, 10);
								if (*strtol_error != '\0' || tmp_string.length() == 0) {
									recv_msg = send_and_recv("-ERR invalid number\r\n", connectfd, true);
								}
								else if (tmp_long2 >= 0) {
									tmp_string = cmd_top(&dir_vec.at(tmp_long -1), thread.arg.maildir, tmp_long2);
									recv_msg = send_and_recv(tmp_string, connectfd, true);
								}
							}
							else {
								recv_msg = send_and_recv("-ERR unexpected error\r\n", connectfd, true);
							}
						}
						else {
							recv_msg = send_and_recv("-ERR invalid format\r\n", connectfd, true);
						}
						break;

					/*------------------------- UIDL + cislo --------------------------*/
					case cmd_UIDL_opt:
						// prevod cisla
						tmp_long = strtol(msg.c_str(), &strtol_error, 10);
						if (*strtol_error != '\0' || msg.length() == 0) {
							recv_msg = send_and_recv("-ERR invalid number\r\n", connectfd, true);
						}
						else {
							// Osetrenie hranic + osetrenie na vymazanu spravu
							if (tmp_long -1 < 0 || dir_vec.size() < (unsigned) tmp_long || dir_vec.at(tmp_long -1).to_be_del == true) {
								recv_msg = send_and_recv("-ERR no such message\r\n", connectfd, true);
							}
							else if (dir_vec.at(tmp_long -1).to_be_del == false) {
								// vypis jedinecneho mena (nazov suboru)
								recv_msg = send_and_recv("+OK " + to_string(tmp_long) + " " + dir_vec.at(tmp_long -1).name + "\r\n", connectfd, true);
							}
							else {
								recv_msg = send_and_recv("-ERR unexpected error\r\n", connectfd, true);
							}
						}
						break;

					/*------------------------- UIDL --------------------------*/
					case cmd_UIDL:
						tmp_string = cmd_uidl(&dir_vec);
						recv_msg = send_and_recv(tmp_string, connectfd, true);
						break;

					/*------------------------- neplatny prikaz --------------------------*/
					default:
						recv_msg = send_and_recv("-ERR invalid command\r\n", connectfd, true);
						break;
				}

				break;

			/****************************** UPDATE faza ********************************/
			case sUPDATE:
				if (delete_files(&dir_vec, thread.arg.maildir, &tmp_int)) {
					// v poriadku sa vymazu vsetky spravy
					msg = "+OK POP3 server signing off (" + to_string(tmp_int) + " messages left)\r\n";
				}
				else {
					msg = "-ERR some deleted messages not removed\r\n";
				}
				// odomknutie adresara, odoslanie spravy, zatvorenie file descriptor a mymazanie ho z mapy
				mtx.unlock();
				send(connectfd, msg.c_str(), msg.length(), 0);
				close(connectfd);
				glob_fd_map.erase(connectfd);
				state = sEND;
				break;

			/****************************** UPDATE faza ********************************/
			default:
				// neocakavany stav
				state = sEND;
				break;
		}

	}

	// odstranenie identifikatora z mapy vlakien + ukoncenie vlakna
	glob_thread_map.erase(pthread_self());
	pthread_exit((void *) 0);

}
