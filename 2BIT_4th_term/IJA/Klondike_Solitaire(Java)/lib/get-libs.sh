#!/usr/bin/env bash
#skript na stiahnutie externych obrazkov
wget --no-check-certificate -P $(pwd)/lib "https://www.stud.fit.vutbr.cz/~xgrzyb00/IJA/img.zip";
unzip lib/img.zip -d lib;
rm -rf lib/img.zip;