/**
 * 	\file commands.cpp
 * 	\brief Implementacia jednotlivych prikazov POP3 protokolu
 * 	\author Jakub Handzus xhandz00
 * 	
 * 	Projekt: ISA - Programovanie sietovej sluzby
 * 	Tema: POP3 Server
 * 	Rocnik: 2017/18
 */

#include "popser.h"

/**
 * @brief      Zmeni vsetky pismena vstupneho stringu na velke
 *
 * @param[in]  str   vtupny retazec
 *
 * @return     retazec pozostavajuci iba z velkych pismen
 */
string to_upper(string str) {
	for (unsigned i = 0; i < str.length(); i++) {
		str[i] = toupper(str[i]);
	}
	return str;
}

/**
 * @brief      Vygeneruje timestamp ktory sa zklada z cisla procesu,
 * 			   unixoveho casu v sekundach a nazvu hosta
 *
 * @return     timestamp
 */
string generate_timestamp() {
	int epoch_time = time(NULL);

	char hostname[1024];
    gethostname(hostname, 1024); // zistenie host name

	string timestamp = "<" + to_string(getpid()) + "." +
		to_string(epoch_time) + "@" + hostname + ">";

	return timestamp;
}

/**
 * @brief      Odznaci vsetky subory na vymazanie
 *
 * @param      dir_vec  vektor zo vsetkymi informacia o suboroch
 * @param      size     sluzi ako navratovi parameter s novou informaciou o velkosi suborov
 * @param      number   sluzi ako navratovi parameter s novou informaciou o pocte mailov
 */
void cmd_reset(std::vector<t_file> *dir_vec, long *size, int *number) {
	*size = 0;
	*number = 0;
	for (unsigned i = 0; i < dir_vec->size(); i++) {
		dir_vec->at(i).to_be_del = false;
		*size = *size + dir_vec->at(i).file_size;
		(*number)++;
	}
	return;
}

/**
 * @brief      Vygeneruje spravu pre TOP prikaz
 *
 * @param      file     subor na otvorenie
 * @param[in]  maildir  adresar maildir
 * @param[in]  lines    pocet riadkov
 *
 * @return     sprava, ktora bude odoslana klientovi
 */
string cmd_top(t_file * file, string maildir, long lines) {
	string data;
	string content = "";

    // Otvori subor
	infile.open(maildir +"/cur/"+file->name);
	if (!infile) {
		return "-ERR cannot open file\r\n";
	}
	do {
		getline(infile, data);
		// zdvojenie bodky na zaciatku riadku
		if (data.front() == '.') {
			content += ".";
		}
		// musi sa ukoncit CRLF
		if (data.length() != 0 && data.back() == '\r') {
			content += data + "\n";
		}
		else {
			content += data + "\r\n";
		}
	} while (data != "\r" && data != ""); // cita po prvy prazdny riadok

	long i = 0;
	while(getline(infile, data)) {
		if (i >= lines) {
			break;
		}
		i++;
		// zdvojenie bodky na zaciatku riadku
		if (data.front() == '.') {
			content += ".";
		}
		if (data.length() != 0 && data.back() == '\r') {
			content += data + "\n";
		}
		else {
			content += data + "\r\n";
		}
	}
	
	infile.close();
	// regex zdvoji bodku na samostatnom riadku
	return "+OK\r\n" + content + ".\r\n";
}

/**
 * @brief      Vygeneruje spravu pre UIDL prikaz bez parametrov
 *
 * @param      dir_vec  vektor zo vsetkymi informacia o suboroch
 *
 * @return     sprava, ktora bude odoslana klientovi
 */
string cmd_uidl(std::vector<t_file> *dir_vec) {
	string tmp_string = "";
	for (unsigned i = 0; i < dir_vec->size(); i++) {
		if (dir_vec->at(i).to_be_del == false) {
			// jedinecny nazov pozostava z nazvu suboru
			tmp_string += to_string(i + 1) + " " + dir_vec->at(i).name + "\r\n";
		}
	}
	return "+OK\r\n" + tmp_string + ".\r\n";
}

/**
 * @brief      Vygeneruje spravu pre LIST prikaz
 *
 * @param      dir_vec  vektor zo vsetkymi informacia o suboroch
 *
 * @return     sprava, ktora bude odoslana klientovi
 */
string cmd_list(std::vector<t_file> *dir_vec) {
	string tmp_string = "";
	for (unsigned i = 0; i < dir_vec->size(); i++) {
		if (dir_vec->at(i).to_be_del == false) {
			tmp_string += to_string(i + 1) + " " + to_string(dir_vec->at(i).file_size) + "\r\n";
		}
	}
	tmp_string += ".\r\n";
	return tmp_string;
}

/**
 * @brief      Vygeneruje spravu pre RETR prikaz
 *
 * @param      file     subor na otvorenie
 * @param[in]  maildir  adresar maildir
 *
 * @return     sprava, ktora bude odoslana klientovi
 */
string cmd_retr(t_file * file, string maildir) {

	string data;
	string content = "";

	// Otvori subor
	infile.open(maildir +"/cur/"+file->name);
	if (!infile) {
		return "-ERR cannot open file\r\n";
	}

	while(getline(infile, data)) {
		// zdvojenie bodky na zaciatku riadku
		if (data.front() == '.') {
			content += ".";
		}
		// konce riadkov
		if (data.length() != 0 && data.back() == '\r') {
			content += data + "\n";
		}
		else {
			content += data + "\r\n";
		}
	}
	
	infile.close();

	//pridavanie posledneho \r\n
	if (content.length() > 1 && content.end()[-1] == '\n') {
		if (content.end()[-2] != '\r') {
			content.insert(content.length() - 1, "\r");
		}
	}
	else if (content.length() != 0 && content.end()[-1] != '\n') {
		content += "\r\n";
	}
	else if (content.length() == 0) {
		content += "\r\n";
	}

    return "+OK " + to_string(file->file_size) + " octets\r\n" + content + ".\r\n";

}

/**
 * @brief      Rozpoznanie prikazu zadaneho klientom
 *
 * @param[in]  recv_msg  prijata sprava od klienta
 * @param      cmd       sluzi ako navratovi parameter s informaciou o zadanom prikaze
 * 						 zoznam moznych prikazov je vypisany v enum cmd
 * @param      msg       sluzi ako navratovi parameter s argumentami prikazu
 */
void cmd_recognition(string recv_msg, int *cmd, string *msg) {
	string up_recv_msg;		// sprava s velkymi pismenami

	// Rozpoznanie clientskeho prikazu a argumentov + overenie korektnosti
	if (recv_msg.length() >= 6 ) {	// aspon 4 znaky + \r\n
		up_recv_msg = to_upper(recv_msg);
		if (up_recv_msg.find("USER ") == 0) {
			*cmd = cmd_USER;
			*msg = recv_msg.substr(5).substr(0,recv_msg.size()-7);
		}
		else if (up_recv_msg.find("PASS ") == 0) {
			*cmd = cmd_PASS;
			*msg = recv_msg.substr(5).substr(0,recv_msg.size()-7);
		}
		else if (up_recv_msg.find("APOP ") == 0) {
			*cmd = cmd_APOP;
			*msg = recv_msg.substr(5).substr(0,recv_msg.size()-7);
		}
		else if (up_recv_msg.find("QUIT ") == 0) {
			*cmd = cmd_QUIT;
			*msg = "";
		}
		else if (up_recv_msg.find("QUIT") == 0 && recv_msg.length() == 6) {
			*cmd = cmd_QUIT;
			*msg = "";
		}
		else if (up_recv_msg.find("NOOP ") == 0) {
			*cmd = cmd_NOOP;
			*msg = "";
		}
		else if (up_recv_msg.find("NOOP") == 0 && recv_msg.length() == 6) {
			*cmd = cmd_NOOP;
			*msg = "";
		}
		else if (up_recv_msg.find("STAT ") == 0) {
			*cmd = cmd_STAT;
			*msg = "";
		}
		else if (up_recv_msg.find("STAT") == 0 && recv_msg.length() == 6) {
			*cmd = cmd_STAT;
			*msg = "";
		}
		else if (up_recv_msg.find("DELE ") == 0) {
			*cmd = cmd_DELE;
			*msg = recv_msg.substr(5).substr(0,recv_msg.size()-7);
		}
		else if (up_recv_msg.find("LIST ") == 0) {
			*cmd = cmd_LIST_opt;
			*msg = recv_msg.substr(5).substr(0,recv_msg.size()-7);
		}
		else if (up_recv_msg.find("LIST") == 0 && recv_msg.length() == 6) {
			*cmd = cmd_LIST;
			*msg = "";
		}
		else if (up_recv_msg.find("RETR ") == 0) {
			*cmd = cmd_RETR;
			*msg = recv_msg.substr(5).substr(0,recv_msg.size()-7);
		}
		else if (up_recv_msg.find("RSET ") == 0) {
			*cmd = cmd_RSET;
			*msg = "";
		}
		else if (up_recv_msg.find("RSET") == 0 && recv_msg.length() == 6) {
			*cmd = cmd_RSET;
			*msg = "";
		}
		else if (up_recv_msg.find("UIDL ") == 0) {
			*cmd = cmd_UIDL_opt;
			*msg = recv_msg.substr(5).substr(0,recv_msg.size()-7);
		}
		else if (up_recv_msg.find("UIDL") == 0 && recv_msg.length() == 6) {
			*cmd = cmd_UIDL;
			*msg = "";
		}
		else if (up_recv_msg.find("TOP ") == 0) {
			*cmd = cmd_TOP;
			*msg = recv_msg.substr(4).substr(0,recv_msg.size()-6);
		} 
		else {
			*cmd = cmd_ERROR;
			*msg = recv_msg.substr(0,recv_msg.size()-2);
		}
	}
	else {
		*cmd = cmd_ERROR;
		*msg = recv_msg.substr(0,recv_msg.size()-2);
	}

}
