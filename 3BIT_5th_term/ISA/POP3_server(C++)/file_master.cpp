/**
 * 	\file file_master.cpp
 * 	\brief Subor funkcii pracujucich so subormi a adresarmi
 * 	\author Jakub Handzus xhandz00
 * 	
 * 	Projekt: ISA - Programovanie sietovej sluzby
 * 	Tema: POP3 Server
 * 	Rocnik: 2017/18
 */


#include "popser.h"

/**
 * @brief      Zisti ci je to adresar alebo file alebo zla cesta
 *
 * @param[in]  path  cesta
 *
 * @return     DIRECTORY ak je to zlozka
 * 			   MYFILE ked je to subor
 * 			   WRONG_PATH ak je neplatna cesta
 * 			   ked je to nieco ine tak -1
 */
int is_dir_or_file(string path) {
	struct stat statbuf;
	if (stat(path.c_str(), &statbuf) != -1) {
		if (S_ISDIR(statbuf.st_mode)) {
			return DIRECTORY;
		}
		else if (S_ISREG(statbuf.st_mode)) {
			return MYFILE;
		}
		return -1;
	}
	return WRONG_PATH;
}


/**
 * @brief      presuva subory mezdzi adresarmi /new a /cur
 *
 * @param[in]  path     cesta k maildir
 * @param[in]  reverse  priznak, ktory udava smer presunu
 * 						false: z /new do /cur
 * 						true: z /cur do /new
 *
 * @return     OK: bezchybneho vykonania funkcie
 * 			   ERROR: aspon jedna chyba
 */
int new_to_cur(string path, bool reverse) {
	string src, dst;
	// ak bolo aktovovane cur_to_new (argument -r)
	if (reverse) {
		src = "/cur";
		dst = "/new";
	}
	// inak je to new_to_cur
	else {
		src = "/new";
		dst = "/cur";
	}

	// Kontrola Maildir
	int tmp = is_dir_or_file(path);
	if (tmp != DIRECTORY) {
		return ERROR;
	}
	// kontrola zdroja
	tmp = is_dir_or_file(path + src);
	if (tmp != DIRECTORY) {
		return ERROR;
	}
	//kontrola ciela
	tmp = is_dir_or_file(path + dst);
	if (tmp != DIRECTORY) {
		return ERROR;
	}

	DIR *dir;
	struct dirent *ent;
	string folder = path + src;	//cesta zdrojoveho adresara
	int ret_val = OK;

	if ((dir = opendir (folder.c_str())) != NULL) {
		while ((ent = readdir (dir)) != NULL) {
			// Ak to nieje aktualny adresar alebo nadradena zlozka
			if (ent->d_name != string(".") && ent->d_name != string("..")) {

				// presun suboru pomocou premenovania
				if (rename((path + src + "/" + ent->d_name).c_str(), (path + dst + "/" + ent->d_name).c_str()) != 0) {
					fprintf (stderr, "Error in moving file %s\n", (path + src + "/" + ent->d_name).c_str());
					ret_val = ERROR;
					continue;
				}
				else if (reverse == false) {
					size_write(ent->d_name, file_size(path + dst + "/" + ent->d_name));
				}
			}
		}
		closedir(dir);
	}
	return ret_val;

}

/**
 * @brief      Dopise na koniec subora s velkostami meno a velkost presuvaneho suboru
 *
 * @param[in]  name  meno subora
 * @param[in]  size  velkost
 *
 * @return     uspesnost akcie
 */
int size_write(string name, long size) {
	string file_path = path_to_binary + SIZE_FILE;
	// otvorenie subora
	out = fopen(file_path.c_str(), "a");
	if (out == NULL) {
		return ERROR;
	}
	
	fprintf(out, "name: %s\nsize: %ld\n", name.c_str(), size);
	fclose(out);
	out = NULL;		//reset globalnej premenny
	return OK;
}

/**
 * @brief      Zistuje velkost subora. Velkost znaku konca riadku je 2bajty.
 *
 * @param[in]  file_path  cesta k suboru
 *
 * @return     velkost suboru
 */
long file_size(string file_path) {
	long size = 0;
	string data;
	// otvorenie subora
	infile.open(file_path);
	if (!infile) {
		fprintf (stderr, "Error cannot open file %s\n", file_path.c_str());
		return 0;
	}
	//
	while(getline(infile, data)) {
		// ak sa nachadza \r\n
		if (data.back() == '\r') {
			size += data.length() + 1;
		}
		// iba \n
		else {
			size += data.length() + 2;
		}
	}

	infile.close();
	return size;
}

/**
 * @brief      Nacita autorizacny subor
 *
 * @param      auth_info  struktura do ktorej sa zapisu informacie
 * @param[in]  path       cesta
 *
 * @return     uspesnost akcie
 */
int get_auth_info(t_auth *auth_info, string path) {

	string data;

	// Otvori subor
	infile.open(path);
	if (!infile) {
		fprintf (stderr, "Error in opening auth file\n");
		return ERROR;
	}

	// Nacita prvy riadok + overenie
	getline(infile, data);
	if (data.length() > 11) {
		auth_info->username = data.substr(11);	//username
	}
	else {
		fprintf (stderr, "Error in structure of auth file\n");
		return ERROR;
	}
	// druhy riadok
	getline(infile, data);
	if (data.length() > 11) {
		auth_info->password = data.substr(11);	//password
	}
	else {
		fprintf (stderr, "Error in structure of auth file\n");
		return ERROR;
	}
	infile.close();	// zatvorenie suboru
	return OK;
}

/**
 * @brief      Prezre mailovu zlozku
 *
 * @param[in]  maildir  cesta k mailovej zlozke
 *
 * @return     vektor informacii o kazdom subore
 */
std::vector<t_file> check_mailbox(string maildir) {
	DIR *dir;
	struct dirent *ent;
	string folder = maildir + "/cur";	//cesta
	int number = 0;	//cislovanie suborov
	std::vector<t_file> tmp_dir;
	map<string,long> file_size_map;	// mapa velkosti suborov zo suboru
	// Otvorenie zlozky
	if ((dir = opendir (folder.c_str())) != NULL) {
		// nacitanie mapy velkosti
		size_map(&file_size_map);

		while ((ent = readdir (dir)) != NULL) {
			// Ak to nieje aktualny adresar alebo nadradena zlozka
			if (ent->d_name != string(".") && ent->d_name != string("..")) {
				t_file tmp_file;
				tmp_file.number = number++;			// prideli cislo
				tmp_file.name = ent->d_name;		// zisti meno
				tmp_file.file_size = file_size_map.find(tmp_file.name)->second;	// zisti velkost
				tmp_dir.push_back(tmp_file);		// nahranie do pola
			}
		}
		closedir (dir);
	} else {
		// nemoze sa otvorit adresar
		fprintf (stderr, "Error in structure of Maildir\n");
	}
	return tmp_dir;
}

/**
 * @brief      vytvori zo subora s velkostami mapu, s dvojicami nazov a velkost
 *
 * @param      file_size_map  vytvorena mapa
 */
void size_map(map<string,long> *file_size_map) {
	string file_path = path_to_binary + SIZE_FILE;
	infile.open(file_path);
	if (!infile) {
		return;
	}

	string name, data, tmp_int;
	long size;
	char * strtol_error;
	while (getline(infile, data)) {
		name = data.substr(6);		// meno
		getline(infile, data);
		tmp_int = data.substr(6);	// velkost
		// prevod cisla
		size = strtol(tmp_int.c_str(), &strtol_error, 10);
		if (*strtol_error != '\0' || tmp_int.length() == 0) {
			continue;
		}
		// vlozenie dvojice
		file_size_map->insert(pair<string, long>(name, size));
	}

	infile.close();

	return;
}

/**
 * @brief      
 *
 * @param      dir_vec  vektor informacii o suboroch
 * @param      size     sluzi ako navratovi parameter, kde sa ulozi velkost vsetkych mailov v bytoch
 * @param      number   sluzi ako navratovi parameter, kde sa ulozi pocet mailov
 */
void num_and_octets(std::vector<t_file> *dir_vec, long *size, int *number) {
	*size = 0;
	*number = 0;
	for (unsigned i = 0; i < dir_vec->size(); i++) {
		if (dir_vec->at(i).to_be_del == false) {	// iba tie ktore niesu na vymazanie
			*size = *size + dir_vec->at(i).file_size;
			(*number)++;
		}
	}
	return;
}

/**
 * @brief      zapisanie do pomocneho suboru
 *
 * @param[in]  maildir_path  retazec, ktory sa sklada z Maildir cesty
 *
 * @return     uspesnost akcie
 */
int tmp_write(string maildir_path) {
	string file_path = path_to_binary + AUX_FILE;
	// ovorenie na zapis
	out = fopen(file_path.c_str(), "w");
	if (out == NULL) {
		return ERROR;
	}
	
	fprintf(out, "%s", maildir_path.c_str());
	fclose(out);
	out = NULL;		//reset globalnej premenny
	return OK;
}

/**
 * @brief      precitanie hodnoty z pomocneho subora
 *
 * @param      path_in_file  retazec, ktory sa nachadza v pomocnom subore (cesta k Maildir)
 *
 * @return     uspesnost akcie
 */
int tmp_read(string * path_in_file) {
	string file_path = path_to_binary + AUX_FILE;
	// otvori subor
	infile.open(file_path.c_str());
	if (!infile) {
		fprintf (stderr, "Error in opening file with path to Maildir\n");
		return ERROR;
	}

	getline(infile, * path_in_file);
	infile.close();	// zatvorenie suboru
	return OK;
}

/**
 * @brief      Vymaze oznacene subory z disku
 *
 * @param      dir_vec  vektor informacii o suboroch
 * @param[in]  maildir  cesta k adresaru Maildir
 * @param      left     sluzi ako navratovi parameter, kde sa ulozi aktualny pocet mailov v Maildir
 *
 * @return     true ak nenastala ziadna chyba
 * 			   false ak nastala aspon jedna chyba
 */
bool delete_files(std::vector<t_file> *dir_vec, string maildir, int *left) {
	bool tmp = true;
	left = 0;
	for (unsigned i = 0; i < dir_vec->size(); i++) {
		if (dir_vec->at(i).to_be_del == true) {
			if (remove((maildir + "/cur/" + dir_vec->at(i).name).c_str()) != 0) {
				tmp = false;	// pri nepodarenom vymazani sa zaznaci flag o neuspechu
				left++;			// zapocita sa do poctu celkovych suborov
			}
			else {
				remove_form_file(dir_vec->at(i).name);
			}
		}
		// pocitadlo nevymazanych sprav
		else {
			left++;
		}
	}
	return tmp;
}

/**
 * @brief      Vymaze nazov a velkost vymanazeho mailu zo subora,
 *  		   kde su ukladane velkosti suborov
 *
 * @param[in]  name  nazov subora
 */
void remove_form_file(string name) {
	string file_path = path_to_binary + SIZE_FILE;
	string data;
	// citanie suboru v ktorom su ulozene velkosti mailov
	infile.open(file_path);
	if (!infile) {
		return;
	}
	// otvorenie noveho suboru na zapis
	out = fopen("tmp.txt", "w");
	if (out == NULL) {
		return;
	}

	// prepis data
	while (getline(infile, data)) {
		// kopirovanie potrebnych dat
		if (data != "name: " + name) {
			fprintf(out, "%s\n", data.c_str());
			getline(infile, data);
			fprintf(out, "%s\n", data.c_str());
		}
		// zahodenie nepotrebnych
		else {
			getline(infile, data);
		}
	}

	// zatvorenie suborov
	infile.close();
	fclose(out);
	out = NULL;

	// vymazanie stareho
	remove(file_path.c_str());
	// nahradenie novym
	rename("tmp.txt", file_path.c_str());

	return;

}
