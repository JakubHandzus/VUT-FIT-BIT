#include "popser.h"

/**
 * @brief      Nacita vstupne argumenty programu a vyhodnoti ich spravnost
 *
 * @param      arg   struktura, ktora sa naplni informaciami o zadanych vstypnych argumentoch
 * @param[in]  argc  pocet vstupnych argumentov
 * @param      argv  pole vstupnych argumentov
 *
 * @return     Informaciu o uspesnosti zadania vstypnych argumentoch
 */
int parse_arg(t_arguments *arg, int argc, char const *argv[]) {

	int character;
	char * strtol_error;
	long tmp_port;

	//Zitenie cesty k binarke
	get_program_path(string(argv[0]));

	while ((character = getopt(argc, (char **)argv, "ha:cp:d:r")) != EOF) {
		switch (character) {
			// Help
			case 'h':
				if (arg->b_help){
					fprintf (stderr, "Error: Bad arguments (duplicite -h)\n");
					return E_ARG;
				}
				arg->b_help = true;

				break;

			// Auth file
			case 'a':
				if (arg->b_auth_file){
					fprintf (stderr, "Error: Bad arguments (duplicite -a)\n");
					return E_ARG;
				}
				arg->b_auth_file = true;
				arg->auth_file = optarg;

				break;

			// Clear pass
			case 'c':
				if (arg->b_clear){
					fprintf (stderr, "Error: Bad arguments (duplicite -c)\n");
					return E_ARG;
				}
				arg->b_clear = true;

				break;

			// Port
			case 'p':
				if (arg->b_port){
					fprintf (stderr, "Error: Bad arguments (duplicite -p)\n");
					return E_ARG;
				}

				arg->b_port= true;
				// prevod na cislo
				tmp_port = strtol(optarg, &strtol_error, 10);
				// Overenie na zlyhanie
				if (*strtol_error != '\0' || tmp_port <= 0 ) {
					fprintf (stderr, "Error: Bad port number\n");
					return E_ARG;
				}
				arg->port = (int)tmp_port;

				break;

			// Directory
			case 'd':
				if (arg->b_maildir){
					fprintf (stderr, "Error: Bad arguments (duplicite -d)\n");
					return E_ARG;
				}
				arg->b_maildir = true;
				arg->maildir = optarg;

				break;

			// Reset
			case 'r':
				if (arg->b_reset){
					fprintf (stderr, "Error: Bad arguments (duplicite -r)\n");
					return E_ARG;
				}
				arg->b_reset = true;

				break;
			
			default:
				return E_ARG;
				break;
		}
	}

	argc -= optind;
	argv += optind;

	//Je zadany iba reset
	if (!arg->b_port && !arg->b_maildir && !arg->b_auth_file && !arg->b_clear && arg->b_reset) {
		arg->only_reset = true;
	}

	//Chyba argumentov ak
	if (!((arg->b_port && arg->b_maildir && arg->b_auth_file) || arg->only_reset || arg->b_help)) {
		fprintf (stderr, "Error: Bad arguments\n");
		return E_ARG;
	}
	//Boli vstetky argumenty spracovane? inak chyba
	if (argc != 0) {
		fprintf (stderr, "Error: Bad arguments\n");
		return E_ARG;
	}

	return OK;
}

/**
 * @brief      Zisti cestu k binarke
 *
 * @param[in]  path  cesta, z akou bol program spusteny
 */
void get_program_path(string path) {
	// pozicia poslednej "/"
	size_t pos = path.rfind("/");
    // ulozenie do globalnej premennej
    path_to_binary = path.substr(0, pos + 1);
}
