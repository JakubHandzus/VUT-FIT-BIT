/**
 * 	\file popser.cpp
 * 	\brief Subor obsahujuci main, napovedu
 * 	\author Jakub Handzus xhandz00
 * 	
 * 	Projekt: ISA - Programovanie sietovej sluzby
 * 	Tema: POP3 Server
 * 	Rocnik: 2017/18
 */

 #include "popser.h"

mutex mtx, mtx_thread;
map<int,string> glob_fd_map;
map<pthread_t, int> glob_thread_map;
ifstream infile;
FILE* out;
string path_to_binary;



/**
 * @brief      Vypisanie napovedy
 */
void print_help() {
	string tmp_string = "";
	tmp_string = "USAGE: ./popser [-h] [-a PATH] [-c] [-p PORT] [-d PATH] [-r]\n\n";
	tmp_string +="   h (help)       - an optional prameter, if it is entered, it prints the help and the program is terminated\n";
	tmp_string +="   a (auth file)  - path to the login file\n";
	tmp_string +="   c (clear pass) - an optional prameter, if it is entered, server accepts commands USER and PASS\n";
	tmp_string +="   p (port)       - server port number\n";
	tmp_string +="   d (directory)  - path to Maildir directory\n";
	tmp_string +="   r (reset)      - an optional prameter, server deletes all of its auxiliary files and emails from";
	tmp_string +="the Maildir directory structure and returns to the original state as if the process of popser has never been triggered\n\n";
	cout << tmp_string;
}


int main(int argc, const char * argv[]) {

	// SIGINT handler
	signal(SIGINT, signal_handler);

	// SPRACOVANIE PARAMETROV
	t_arguments arg;
	if (parse_arg(&arg, argc, argv) != OK) {
		fprintf (stderr, "Try use help \"./popser -h\"\n");
		return E_ARG;
	}

	// Vypis napovedy
	if (arg.b_help) {
		print_help();
		return OK;
	}

	// reset
	if (arg.b_reset) {
		string tmp_string;
		int tmp_int = OK;
		// neexistuje 
		if (is_dir_or_file((path_to_binary + AUX_FILE).c_str()) != MYFILE) {
			fprintf (stderr, "Error: %s not found\n", (path_to_binary + AUX_FILE).c_str());
			tmp_int = EXIT_FAILURE;
		}
		// reset existuje
		else {
			// zisti nazov maildir
			if (tmp_read(&tmp_string) == OK) {
				// presun naspet do new
				if (new_to_cur(tmp_string, true) != OK) {
					fprintf (stderr, "Error in reset\n");
					tmp_int = EXIT_FAILURE;
				}
				remove((path_to_binary + AUX_FILE).c_str());
				remove((path_to_binary + SIZE_FILE).c_str());
			}
			// neotvori tmp subor
			else {
				fprintf (stderr, "Error in tmp file\n");
				tmp_int = EXIT_FAILURE;
			}
		}
		// ak bol iba reset, ukonci sa
		if (arg.only_reset) {
			return tmp_int;
		}
	}

	if (arg.b_maildir) {
		tmp_write(arg.maildir);
	}

	int tmp;
	// Kontrola autentifikacneho suboru
	tmp = is_dir_or_file(arg.auth_file);
	if (tmp == -1 || tmp == DIRECTORY) {
		fprintf (stderr, "Error: Authentication file problem\n");
		return EXIT_FAILURE;
	}
	// Nacitanie autorizacneho suboru
	t_auth auth_info;
	if (get_auth_info(&auth_info, arg.auth_file) == ERROR) {
		return EXIT_FAILURE;
	}

	if (setup_connection(&arg, &auth_info) != OK) {
		exit_all();
		return EXIT_FAILURE;
	}

	return 0;

}
