#ifndef POPSER_H
#define POPSER_H

// C POSIX:
#include <string.h>
#include <unistd.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <pthread.h>
#include <fcntl.h>
#include <dirent.h>
#include <sys/stat.h>
#include <stdio.h>
#include <stdlib.h>

// C++ standart
#include <iostream>
#include <vector>
#include <map>
#include <mutex>
#include <csignal>
#include <fstream>

// Externy zdroj
#include "md5.h" // hashovacia funkcia

#define DIRECTORY 2
#define MYFILE 1
#define WRONG_PATH -1

#define AUX_FILE "reset.txt"
#define SIZE_FILE "size_file.txt"

#define BUFSIZE 1
#define QUEUE	(2)

using namespace std;


/* Navratove hodnoty */
enum ret_codes {
	OK = 0,
	ERROR = 1,
	E_ARG = 2,
	E_SOCK
};

/* Stavy konecneho automatu */
enum states {
	sAUTH,
	sUSER,
	sPASS,
	sAPOP,
	sQUIT,
	sTRANS,
	sUPDATE,
	sEND	
};

enum cmd {
	cmd_USER,
	cmd_PASS,
	cmd_APOP,
	cmd_QUIT,
	cmd_NOOP,
	cmd_STAT,
	cmd_DELE,
	cmd_LIST_opt,
	cmd_LIST,
	cmd_RETR,
	cmd_RSET,
	cmd_UIDL_opt,
	cmd_UIDL,
	cmd_TOP,
	cmd_ERROR
};

/* Informacie o argumentoch */
struct t_arguments {
	bool b_help = false;
	string auth_file;
	bool b_auth_file = false;
	bool b_clear = false;
	int port;
	bool b_port = false;
	string maildir;
	bool b_maildir = false;
	bool b_reset = false;
	bool only_reset = false;

};

/* Autorizacna struktura s menom a heslom */
struct t_auth {
	string username;
	string password;
};

/* Struktura vyuzivana vlaknami */
struct t_thread {
	t_arguments arg;
	t_auth auth;
};

/* Informacie o subore */
struct t_file {
	int number;
	string name;
	long file_size;
	bool to_be_del = false;
};

extern mutex mtx, mtx_thread;
extern map<pthread_t, int> glob_thread_map;
extern map<int,string> glob_fd_map;
extern ifstream infile;
extern FILE* out;
extern string path_to_binary;


int is_dir_or_file(string path);
int new_to_cur(string path, bool reverse);
int parse_arg(t_arguments *arg, int argc, char const *argv[]);
void print_help();
int get_auth_info(t_auth *auth_info, string path);
string generate_timestamp();
std::vector<t_file> check_mailbox(string maildir);
void num_and_octets(std::vector<t_file> *dir_vec, long *size, int *number);
bool delete_files(std::vector<t_file> *dir_vec, string maildir, int *left);
void cmd_reset(std::vector<t_file> *dir_vec, long *size, int *number);
string cmd_top(t_file * file, string maildir, long lines);
string cmd_uidl(std::vector<t_file> *dir_vec);
string cmd_list(std::vector<t_file> *dir_vec);
string cmd_retr(t_file * file, string maildir);
void send_msg(string msg, int connectfd);
string rcv_msg(int connectfd, bool lock);
string send_and_recv(string msg, int connectfd, bool lock);
void kill_thread(int fd, bool lock);
string to_upper(string str);
void cmd_recognition(string recv_msg, int *cmd, string *msg);
void *pop3_fsm(void *tmp_struct);
int setup_connection(t_arguments *arg, t_auth *auth);
void signal_handler(int signum);
void exit_all();
int tmp_write(string maildir_path);
int tmp_read(string * path_in_file);
void size_map(map<string,long> *file_size_map);
int size_write(string name, long size);
long file_size(string file_path);
void remove_form_file(string name);
void get_program_path(string path);


#endif
