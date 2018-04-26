CC = g++
CFLAGS = -Wall -Wextra -pedantic -std=c++11 -pthread

all: popser

popser: popser_md5.o popser1.o popser2.o popser3.o popser4.o popser5.o popser6.o
	$(CC) $(CFLAGS) popser_md5.o popser1.o popser2.o popser3.o popser4.o popser5.o popser6.o -o $@ 

popser_md5.o: md5.h md5.cpp
	$(CC) $(CFLAGS) -c md5.cpp -o $@ 

popser1.o: popser.h popser.cpp
	$(CC) $(CFLAGS) -c popser.cpp -o $@ 

popser2.o: popser.h arguments.cpp
	$(CC) $(CFLAGS) -c arguments.cpp -o $@

popser3.o: popser.h commands.cpp
	$(CC) $(CFLAGS) -c commands.cpp -o $@ 

popser4.o: popser.h file_master.cpp
	$(CC) $(CFLAGS) -c file_master.cpp -o $@ 

popser5.o: popser.h fsm.cpp
	$(CC) $(CFLAGS) -c fsm.cpp -o $@ 

popser6.o: popser.h net.cpp
	$(CC) $(CFLAGS) -c net.cpp -o $@ 
