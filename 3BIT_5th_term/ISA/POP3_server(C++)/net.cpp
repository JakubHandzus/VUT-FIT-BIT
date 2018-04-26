/**
 * 	\file net.cpp
 * 	\brief Praca so sietovimi kniznicami
 * 	\author Jakub Handzus xhandz00
 * 	
 * 	Projekt: ISA - Programovanie sietovej sluzby
 * 	Tema: POP3 Server
 * 	Rocnik: 2017/18
 */

 #include "popser.h"

/**
 * @brief      Odosle spravu
 *
 * @param[in]  msg        Sprava
 * @param[in]  connectfd  Cislo file descriptora pre soket
 */
void send_msg(string msg, int connectfd) {

	fd_set set;
	FD_ZERO(&set);				// vynuluje set
	FD_SET(connectfd, &set);	// prida do setu sledonavy file descriptor

	int done = 0;
	int length;
	int last_send = 0;

	// odosiela kym sa neodosle cela sprava
	do {
		msg = msg.substr(last_send);
		length = msg.length();

		int rv = select(connectfd + 1, NULL, &set, NULL, NULL);
		// ak sa moze odosielat
		if (rv > 0) {
			last_send = write(connectfd, msg.c_str(), length);
			if (last_send > 0) {	// ak sa nieco odoslalo
				done += last_send;
			}
		}

	} while (last_send < length);
}

/**
 * @brief      ukonci spojenie a vlakno
 *
 * @param[in]  connectfd  file descriptor soketu
 * @param[in]  lock       bol nastaveny zamok
 */
void kill_thread(int fd, bool lock) {

	close(fd);
	glob_fd_map.erase(fd);
	if (lock) {
		mtx.unlock();
	}
	glob_thread_map.erase(pthread_self());
	pthread_exit((void *) 0);

}

/**
 * @brief      Prijme spravu
 *
 * @param[in]  connectfd  Cislo file descriptora pre soket
 * @param[in]  lock       bol nastaveny zamok
 *
 * @return     Prijata sprava
 */
string rcv_msg(int connectfd, bool lock) {
	int n;
	char buf[BUFSIZE];
	bzero(buf, BUFSIZE);
	string rcv_buf = "";

	fd_set set;
	FD_ZERO(&set);				// vynuluje set
	FD_SET(connectfd, &set);	// prida do setu sledonavy file descriptor
	struct timeval timeout = {600, 0}; // nastavi casovac

	int rv = select(connectfd + 1, &set, NULL, NULL, &timeout);
	if (rv == -1) {
		// error selektu
		fprintf(stderr, "ERROR select in file descriptor %d\n", connectfd);
	}
	else if (rv == 0) {
		// casovac na citanie
		kill_thread(connectfd, lock);
	}
	else {
	    while ((n = read(connectfd, buf, BUFSIZE)) > 0) {
			rcv_buf += string(buf, n);
			if (rcv_buf.find("\r\n") != string::npos) {
				break;
			}
		}
		if (n == 0) {
			kill_thread(connectfd, lock);
		}
	}
	return rcv_buf;
}

/**
 * @brief      Odosle a zaroven prijme dalsiu spravu
 *
 * @param[in]  msg        sprava na odoslanie
 * @param[in]  connectfd  cislo file descriptora pre soket
 *
 * @return     Prijata sprava
 */
string send_and_recv(string msg, int connectfd, bool lock) {
	send_msg(msg, connectfd);
	return rcv_msg(connectfd, lock);
}

/**
 * @brief      Nastavi uvitaci neblokujuci soket prijimajuci TCP spojenia.
 *   		   Po nadviazani spojenia s klientom vytvori novy neblokujuci soket,
 *   		   vytvori nove vlakno, vytvori casovu peciatku a ulozi vsetky informacie
 *   		   do globalnych map.
 *
 * @param      arg   struktura argumentov
 * @param      auth  autentifikacne udaje
 *
 * @return     chybu v pripade nepodarenho pokusu o vytvorenie soketu/spojenia/vlakna
 */
int setup_connection(t_arguments *arg, t_auth *auth) {

	int listenfd, connectfd;
	struct sockaddr_in server;
	memset(&server, 0, sizeof(server));
	server.sin_family = AF_INET;
	server.sin_addr.s_addr = htonl(INADDR_ANY);
	server.sin_port = htons(arg->port);

	if ((listenfd = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
		fprintf (stderr, "ERROR: socket\n");
		return E_SOCK;
	}

	// pridanie fd do mapy vsetkych filedescriptorov
	glob_fd_map.insert(pair<int, string>(listenfd, ""));

	int enable = 1;
	if (setsockopt(listenfd, SOL_SOCKET, SO_REUSEADDR, &enable, sizeof(int)) < 0) {
		fprintf (stderr, "ERROR: setsockopt(SO_REUSEADDR) failed\n");
		return E_SOCK;
	}

	// nastavenie neblokujuceho soketu
	int status = fcntl(listenfd, F_SETFL, fcntl(listenfd, F_GETFL, 0) | O_NONBLOCK);
	if (status == -1) {
		fprintf (stderr, "ERROR: setsockopt(SO_REUSEADDR) failed\n");
		return E_SOCK;
	}

	if (::bind(listenfd, (struct sockaddr *)&server, sizeof(server)) == -1) {
		fprintf (stderr, "ERROR: port is not available\n");
		return E_SOCK;
	}

	if (listen(listenfd, QUEUE) == -1) {
		fprintf (stderr, "ERROR: listen\n");
		return E_SOCK;
	}

	fd_set readset, tempset;
	int maxfd;
	int result, len;
	sockaddr_in addr;

	
	FD_ZERO(&readset);
	FD_SET(listenfd, &readset);
	maxfd = listenfd;

	pthread_t thread_id;
	string tmp_string;
	t_thread for_thread;
	for_thread.arg = *arg;
	for_thread.auth = *auth;
	
	do {
		memcpy(&tempset, &readset, sizeof(tempset));

		// Select pre neblokujuci port
		result = select(maxfd + 1, &tempset, NULL, NULL, NULL);

		if (result < 0 && errno != EINTR) {
			fprintf(stderr, "Error in select(): %s\n", strerror(errno));
		}
		else if (result > 0) {

			if (FD_ISSET(listenfd, &tempset)) {
				len = sizeof(addr);
				connectfd = accept(listenfd, (struct sockaddr *) &addr, (unsigned *)&len);
				if (connectfd < 0) {
					fprintf(stderr, "Error in accept(): %s\n", strerror(errno));
				}
				else { 
					// nastavenie neblokujuceho soketu
					int status = fcntl(connectfd, F_SETFL, fcntl(connectfd, F_GETFL, 0) | O_NONBLOCK);
					if (status == -1) {
						fprintf (stderr, "ERROR: setsockopt(SO_REUSEADDR) failed\n");
						close(connectfd);
						continue;
					}

					// Generovanie a odoslanie uvodnej spravy
					tmp_string = generate_timestamp();
					glob_fd_map.insert(pair<int, string>(connectfd, tmp_string));

					send_msg("+OK POP3 server ready" + tmp_string + "\r\n" , connectfd);

					// vytvorenie vlakna + naplnenie struktury s informaciami o vlakne a deskriptorom soketu
					mtx_thread.lock();
					if (pthread_create(&thread_id, NULL, pop3_fsm, (void*) &for_thread) < 0 ) {
						fprintf(stderr, "Error: could not create thread: %s\n", strerror(errno));
						mtx_thread.unlock();
					}
					else {
						glob_thread_map.insert(pair<pthread_t, int>(thread_id, connectfd));
						mtx_thread.unlock();
					}
				}
				FD_CLR(listenfd, &tempset);
			}
		}

	} while (1);

	return OK;
}


/**
 * @brief      Pri sachyteni SIGINT sa program bezpecne ukonci
 *
 * @param[in]  signum  Zachyteny signal
 */
void signal_handler(int signum) {

	if (signum == SIGINT) {

		exit_all();
		exit(OK);
	}
	
}

/**
 * @brief      Ukonci vsetky vlakna, otvorene spojenia a subory
 */
void exit_all() {
	// Ukoncenie vlakien
	while(!glob_thread_map.empty()) {
		pthread_cancel(glob_thread_map.begin()->first);
		glob_thread_map.erase(glob_thread_map.begin()->first);
	}

	// Ukoncenie spojeni
	while(!glob_fd_map.empty()) {
		close(glob_fd_map.begin()->first);
		glob_fd_map.erase(glob_fd_map.begin()->first);
	}

	// Zatvorenie vsetkych suborov (v pripade nezatvorenia)
	infile.close();
	if (out != NULL) {
		fclose(out);
	}

}
