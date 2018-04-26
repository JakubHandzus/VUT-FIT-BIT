#!/usr/bin/env python3.6

# PYTHON skript pre projekt z predmetu IPP
# -----------------------------------
#  Tema: XQR (XML Query)
#  Autor: Jakub Handzus
#  Login: xhandz00
#  Datum: 29.03.2017
#

import getopt
import sys
import os
import re
import xml.dom.minidom as mdom
import xml.etree.ElementTree as ET

# Globalne premenne (konstanty)
OK = 0
WRONG_PARAM = 1
INPUT_ERROR = 2
OUTPUT_ERROR = 3
INPUT_FORMAT_ERROR = 4
QUERY_ERROR = 80
BAD_QUERY = "Spatny dotaz"

##
## @brief      Funkcia volana v pripade chyby.
##             Vypise na stderr hlasenie a ukonci sa.
##
## @param      errStr   Chybova sprava.
## @param      errCode  Navratovy kod.
##
def exitFunction(errStr, errCode):
    print("Chyba:", errStr, "| Navratovy kod:", errCode, file=sys.stderr)
    sys.exit(errCode)

##
## @brief      Skontroluje spravnost zapisu XML elementu/atribitu.
##
## @param      element  Nazov xml elementu.
##
## @return     V pripade chyby ukonci program.
##
def isElement(element):
    if not re.match('^[:A-Z_a-z][:A-Z_a-z\-0-9]*$', element):
        exitFunction(BAD_QUERY, QUERY_ERROR)
    restricted = ["SELECT", "FROM", "ROOT", "WHERE", "NOT", "LIMIT", "CONTAINS"]
    if element in restricted :
        exitFunction(BAD_QUERY, QUERY_ERROR)

##
## @brief      Funkcia na vypisanie napovedy.
##
## @return     Funkcia ukonci program.
##
def printHelp():
    string = "Skript provadi vyhodnoceni zadaneho dotazu, jenz je podobny prikazu SELECT jazyka SQL, "\
    "nad vstupem ve formatu XML. Vystupem je XML obsahujici elementy splnujici pozadavky dane dotazem.\n"\
    "Skript pracuje s nasledujicimmi parametry:\n\n"\
    "--help\t\t\t Vypise na standardni vystup napovedu skriptu.\n\n"\
    "--input=filename\t Zadany vstupni soubor ve formatu XML.\n\n"\
    "--output=filename\t Zadany vystupni soubor ve formatu XML s obsahem podla zadaneho dotazu.\n\n"\
    "--query='dotaz'\t\t Zadany dotaz v dotazovaciom jazyku podobnemu SQL.\n\n"\
    "--qf=filename\t\t Dotaz v dotazovaciom jazyku nachadzejici se v externim textovem souboru.\n\n"\
    "-n \t\t\t Ve vystupu skriptu nebude generovana XML hlavicka.\n\n"\
    "--root=element\t\t Jmeno paroveho korenoveho elementu, obalujici vysledky.\n"
    sys.stdout.write(string)
    exit(OK)

##
## @brief      Funkcia zisti co bolo zadane pri WHERE.
##
## @param      string     Nazov XML elementu/atributu.
## @param      queryArgs  Trieda, kde sa ulozi element.
##
## @return     V pripade chyby ukonci program. Inak funkcia nic nevracia.
##
def elemOrAtr(string, queryArgs):
    if string[0:1] == ".":
        queryArgs.qWhereAtr = string[1:]
    elif string.find(".") != -1:
        # Medzera za elementom
        if string[string.find(".")-1:string.find(".")].isspace():
            exitFunction(BAD_QUERY, QUERY_ERROR)
        queryArgs.qWhereEl = string[0:string.find(".")]
        queryArgs.qWhereAtr = string[string.find(".") +1:]
    else:
        queryArgs.qWhereEl = string

    # Orezenie a kontrola
    if queryArgs.qWhereEl != None:
        queryArgs.qWhereEl = queryArgs.qWhereEl.strip()
        isElement(queryArgs.qWhereEl)
    if queryArgs.qWhereAtr != None:
        queryArgs.qWhereAtr = queryArgs.qWhereAtr.strip()
        isElement(queryArgs.qWhereAtr)

##
## @brief      Funkcia na analyzovanie dotazu.
##
## @param      query      Dotaz (retazec).
## @param      queryArgs  Trieda na ulozenie rozanalyzovaneho dotazu.
##
## @return     V pripade chyby ukonci program. Inak funkcia nic nevracia.
##
def queryParse(query, queryArgs):
    # SELECT ako prve slovo
    # odstranenie medzier
    query = re.sub(r'^\s*', '', query)
    # _____________________________________
    # SELECT
    matchObj = re.match('(^.*?)\s+', query)
    if matchObj:
        if matchObj.group(1) != "SELECT":
            exitFunction(BAD_QUERY, QUERY_ERROR)
    else:
        exitFunction(BAD_QUERY, QUERY_ERROR)
    query = re.sub(r'^\s*', '', query[len(matchObj.group()):])
    # _____________________________________
    # ELEMENT
    matchObj = re.match('(^.*?)\s+', query)
    if matchObj:
        queryArgs.qSelect = matchObj.group(1)
    else:
        exitFunction(BAD_QUERY, QUERY_ERROR)
    isElement(queryArgs.qSelect)
    query = re.sub(r'^\s*', '', query[len(matchObj.group()):])
    # _____________________________________
    # FROM
    matchObj = re.match('(^.*?)\s+', query)
    # plati regex
    if matchObj:
        if matchObj.group(1) != "FROM":
            exitFunction(BAD_QUERY, QUERY_ERROR)
        query = re.sub(r'^\s*', '', query[len(matchObj.group()):])
        if len(query) == 0:
            return
    # neplati regex a nie je tam jedine FROM
    elif query != "FROM":
        exitFunction(BAD_QUERY, QUERY_ERROR)
    # Je tam jedine FROM
    else:
        return
    # _____________________________________
    # Za FROM (ak je tam ROOT)
    if query[0:4] == "ROOT":
        queryArgs.qFrom = "ROOT"
        # orezanie
        query = query[4:]
        # Je to prazdne za ROOT
        if query == "":
            return
        # Je tam medzera?
        elif query[0:1].isspace():
            query = re.sub(r'^\s*', '', query[0:])
            # Je tam este nieco?
            if query == "":
                return
        # Ak tam je nieco, co necakam
        else:
            exitFunction(BAD_QUERY, QUERY_ERROR)

    elif query[0:5] != "WHERE" and query[0:5] != "LIMIT":   # ORDER
        # element or atribute
        matchObj = re.match('(^.*?)\s+', query)
        if matchObj:
            queryArgs.qFrom = matchObj.group(1)
            query = re.sub(r'^\s*', '', query[len(matchObj.group()):])
        elif len(query) != 0:
            queryArgs.qFrom = query
            query = ""
        else:
            exitFunction(BAD_QUERY, QUERY_ERROR)
        # rozdelenie
        if queryArgs.qFrom[0:1] == ".":
            queryArgs.qFromAtr = queryArgs.qFrom[1:]
        elif queryArgs.qFrom.find(".") != -1:
            queryArgs.qFromEl = queryArgs.qFrom[0:queryArgs.qFrom.find(".")]
            queryArgs.qFromAtr = queryArgs.qFrom[queryArgs.qFrom.find(".") +1:]
        else:
            queryArgs.qFromEl = queryArgs.qFrom
        # Orezenie a kontrola
        if queryArgs.qFromEl != None:
            queryArgs.qFromEl = queryArgs.qFromEl.strip()
            isElement(queryArgs.qFromEl)
        if queryArgs.qFromAtr != None:
            queryArgs.qFromAtr = queryArgs.qFromAtr.strip()
            isElement(queryArgs.qFromAtr)
        # vymazanie z From
        queryArgs.qFrom = None

    if len(query) != 0:
        # Ak je tam WHERE
        if query[0:5] == "WHERE":
            # Musi byt za WHERE medzera
            if not query[5:6].isspace():
                exitFunction(BAD_QUERY, QUERY_ERROR)
            query = re.sub(r'^\s*', '', query[5:])
            # cyklus na NOT
            while (query[0:3] == "NOT"):
                # Za NOT musi byt medzera
                if not query[3:4].isspace():
                    exitFunction(BAD_QUERY, QUERY_ERROR)
                queryArgs.qWhereNot = not queryArgs.qWhereNot
                query = re.sub(r'^\s*', '', query[3:])
            # Hladanie operatorov
            if query.find("=") != -1:
                queryArgs.qWhereOperand = "="
                elemOrAtr(query[0:query.find("=")], queryArgs)
                query = re.sub(r'^\s*', '', query[query.find("=") +1:])
            elif query.find(">") != -1:
                queryArgs.qWhereOperand = ">"
                elemOrAtr(query[0:query.find(">")], queryArgs)
                query = re.sub(r'^\s*', '', query[query.find(">") +1:])
            elif query.find("<") != -1:
                queryArgs.qWhereOperand = "<"
                elemOrAtr(query[0:query.find("<")], queryArgs)
                query = re.sub(r'^\s*', '', query[query.find("<") +1:])
            elif query.find("CONTAINS") != -1:
                queryArgs.qWhereOperand = "CONTAINS"
                elemOrAtr(query[0:query.find("CONTAINS")], queryArgs)
                query = re.sub(r'^\s*', '', query[query.find("CONTAINS") +8:])
            else:
                exitFunction(BAD_QUERY, QUERY_ERROR)

            # Ak tam je string
            if query[0:1] == "\"":
                if query[1:].find("\"") != -1:
                    queryArgs.qWhereStr = query[1:query[1:].find("\"")+1]
                    query = re.sub(r'^\s*', '', query[query[1:].find("\"")+2:])
                else:
                    exitFunction(BAD_QUERY, QUERY_ERROR)
            # Inak cislo
            else:
                matchObj = re.match('(^.*?)\s+', query)
                if matchObj:
                    queryArgs.qWhereNum = matchObj.group(1)
                    query = re.sub(r'^\s*', '', query[len(matchObj.group()):])
                elif len(query) != "":
                    queryArgs.qWhereNum = query
                    query = ""
                else:
                    exitFunction(BAD_QUERY, QUERY_ERROR)
                # (+ -) Overenie cisla
                if not queryArgs.qWhereNum[0:1].isdigit():
                    if queryArgs.qWhereNum[0:1] == "-" or queryArgs.qWhereNum[0:1] == "+":
                        if not queryArgs.qWhereNum[1:].isdigit():
                            exitFunction(BAD_QUERY, QUERY_ERROR)
                    else:
                        exitFunction(BAD_QUERY, QUERY_ERROR)
                elif not queryArgs.qWhereNum.isdigit():
                    exitFunction(BAD_QUERY, QUERY_ERROR)

            # Ak je cislo a contains -> chyba
            if queryArgs.qWhereNum != None and queryArgs.qWhereOperand == "CONTAINS":
                exitFunction(BAD_QUERY, QUERY_ERROR)

        # Ak je tam LIMIT
        if query[0:5] == "LIMIT":
            if not query[5:6].isspace():
                exitFunction(BAD_QUERY, QUERY_ERROR)
            query = re.sub(r'^\s*', '', query[5:])
            matchObj = re.match('(^.*?)\s+', query)
            if matchObj:
                queryArgs.qLimitNum = matchObj.group(1)
                query = re.sub(r'^\s*', '', query[len(matchObj.group()):])
            elif len(query) != "":
                queryArgs.qLimitNum = query
                query = ""
            else:
                exitFunction(BAD_QUERY, QUERY_ERROR)
            # (+) Overenie cisla
            if not queryArgs.qLimitNum[0:1].isdigit():
                if queryArgs.qLimitNum[0:1] == "+":
                    if not queryArgs.qLimitNum[1:].isdigit():
                        exitFunction(BAD_QUERY, QUERY_ERROR)
                else:
                    exitFunction(BAD_QUERY, QUERY_ERROR)
            elif not queryArgs.qLimitNum.isdigit():
                exitFunction(BAD_QUERY, QUERY_ERROR)

        if query != "":
            exitFunction(BAD_QUERY, QUERY_ERROR)

    return


##
## @brief      Funkcia ktora na zaklade cisla, operatora, a elementu vyhodnoti vyraz.
##
## @param      element  Nazov elementu/atribitu.
## @param      number   Cislo.
## @param      operand  Operand.
## @param      pBool    Povodna pravdivostna hodnota.
##
## @return     Vrati novu pravdivostnu hodnotu po vykonani vyrazu.
##
def numericOp(element, number, operand, pBool):
    try:
        # Ak je operand <
        if operand == "<":
            if float(element) >= int(number):
                pBool = not pBool
        # Ak je operand >
        elif operand == ">":
            if float(element) <= int(number):
                pBool = not pBool
        # Ak je operand =
        elif operand == "=":
            if float(element) != int(number):
                pBool = not pBool
        return pBool
    except ValueError:
        return not pBool

##
## @brief      Funkcia ktora na zaklade retazca, operatora, a elementu vyhodnoti vyraz.
##
## @param      element  Nazov elementu/atribitu.
## @param      string   Retazec.
## @param      operand  Operand.
## @param      pBool    Povodna pravdivostna hodnota.
##
## @return     Vrati novu pravdivostnu hodnotu po vykonani vyrazu.
##
def stringOp(element, string, operand, pBool):
    # Ak je operand CONTAINS
    if operand == "CONTAINS":
        if string not in element:
            pBool = not pBool
    # Ak je operand <
    elif operand == "<":
        if element >= string:
            pBool = not pBool
    # Ak je operand >
    elif operand == ">":
        if element <= string:
            pBool = not pBool
    # Ak je operand =
    elif operand == "=":
        if element != string:
            pBool = not pBool
    return pBool

##
## @brief      Trieda na ulozenie analyzovaneho dotazu
##
class queryClass(object):
    qSelect = None
    qFrom = None
    qFromEl = None
    qFromAtr = None
    qWhereNot = True
    qWhereOperand = None
    qWhereEl = None
    qWhereAtr = None
    qWhereStr = None
    qWhereNum = None
    qLimitNum = None

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# ------------------Zaciatok Programu------------------
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# Nacitanie argumentov
try:
    opts, args = getopt.getopt(sys.argv[1:],'n',["help", "input=", "output=", "query=", "qf=", "root="])
except getopt.GetoptError as err:
    exitFunction(str(err), 1)

# Globalne premenne ktore sa nastavia pri spracovani argumentov
fHelp = False
fInput = None
fOutput = None
query = None
fQueryFile = None
fNotGener = False
fRoot = None

# Overovanie pre kazdy parameter
for o, value in opts:
    # HELP
    if o[:6] == "--help":
        if fHelp is True:
            exitFunction("Help zadan vice krat", WRONG_PARAM)
        elif len(opts) != 1:
            exitFunction("Help musi byt pouze jediny parametr", WRONG_PARAM)
        fHelp = True
    # INPUT
    elif o[:7] == '--input':
        if fInput is not None:
            exitFunction("Input zadan vice krat", WRONG_PARAM)
        if not os.path.isfile(value):
            exitFunction("Soubor neexistuje", INPUT_ERROR)
        else:
            fInput = value
    # OUTPUT
    elif o[:8] == "--output":
        if fOutput is not None:
            exitFunction("Output zadan vice krat", WRONG_PARAM)
        fOutput = value
    # OUTPUT
    elif o[:7] == "--query":
        if query is not None or fQueryFile is not None:
            exitFunction("Query zadan vice krat", WRONG_PARAM)
        query = value
    # QUERRY_FILE
    elif o[:4] == "--qf":
        if query is not None or fQueryFile is not None:
            exitFunction("Query zadan vice krat", WRONG_PARAM)
        if not os.path.isfile(value):
            exitFunction("Soubor neexistuje", QUERY_ERROR)
        else:
            fQueryFile = value
    # negenerovat hlavicku
    elif o[:2] == "-n":
        if fNotGener is True:
            exitFunction("'-n' zadan vice krat", WRONG_PARAM)
        fNotGener = True
    # ROOT
    elif o[:6] == "--root":
        if fRoot is not None:
            exitFunction("Root zadan vice krat", WRONG_PARAM)
        if not re.match('^[:A-Z_a-z][:A-Z_a-z\-\.0-9]*$', value):
            exitFunction("Root element neodpovida XML elementu", WRONG_PARAM)
        fRoot = value
    else:
        exitFunction("Chybny argument", WRONG_PARAM)

# Ak je nieco v tomto poli, jedna sa o chybu
if args:
    exitFunction("Chybny argument", WRONG_PARAM)

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# Vypis napovedy
if fHelp:
    printHelp()

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# Zaistenie dotazu
# Ak nebol zadany dotaz ako argument
if query is None:
    # Ak nebol zadany dotaz ani v subore, tak skonci s chybou
    if fQueryFile is None:
        exitFunction("Nebol najdeny dotaz", QUERY_ERROR)
    # Inak sa zadany subor nacita
    else:
        try:
            tmpfile = open(fQueryFile, 'r')
            query = tmpfile.read()
            tmpfile.close()
        except IOError:
            exitFunction("Nelze otevrit", QUERY_ERROR) # NAVRATOVY KOOOOOOOOOOOD

# Vytvorenie noveho objektu triedy pre analyzovanie dotazu
queryArgs = queryClass()
# Analyza
queryParse(query, queryArgs)

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# Precitanie vstupu zo suboru
if fInput is not None:
    try:
        tmpfile = open(fInput, 'r')
        inputXml = tmpfile.read()
        tmpfile.close()
    except IOError:
        exitFunction("Nelze otevrit", INPUT_ERROR)
# Ak nieje input tak sa berie XML zo stdin
else:
    inputXml = sys.stdin.read()

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# Spracovavanie vstupneh XML a vyhodnocovanie dotazu
try:
    inputXml = mdom.parseString(inputXml)
except:
    exitFunction("Chybny format vystupniho souboru", INPUT_FORMAT_ERROR)

# V tejto premennej bude vysledok SELECT _ FROM
selResul = []

# ~~~~~~~~~~~~~~~~~~~~~~FROM + SELECT~~~~~~~~~~~~~~~~~~~~~~
# FROM ROOT
if queryArgs.qFrom == "ROOT":
    root = [inputXml.documentElement]
    # Ak je hladany element korenovy
    if inputXml.documentElement.tagName == queryArgs.qSelect:
        selResul = [inputXml.documentElement]
    else:
        for node in root:
            # vyhovujuce elementy
            selResul.extend(node.getElementsByTagName(queryArgs.qSelect))

# FROM element
elif queryArgs.qFromEl != None and queryArgs.qFromAtr == None:
    root = inputXml.getElementsByTagName(queryArgs.qFromEl)
    for node in root:
        # vyberanie elementov pre prvu zhodu
        selResul.extend(node.getElementsByTagName(queryArgs.qSelect))
        break
# FROM atribute
elif queryArgs.qFromEl == None and queryArgs.qFromAtr != None:
    # pre vsetky elementy
    tmp_root = inputXml.getElementsByTagName("*")
    for node in tmp_root:
        # prvy s hladanym atributom
        if node.hasAttribute(queryArgs.qFromAtr):
            # vyhovujuce elementy
            selResul.extend(node.getElementsByTagName(queryArgs.qSelect))
            break
# FROM element.atribute
elif queryArgs.qFromEl != None and queryArgs.qFromAtr != None:
    root = inputXml.getElementsByTagName(queryArgs.qFromEl)
    # hlada prvy vyskyt elementu, ktory ma zadany atribut
    for node in root:
        if node.hasAttribute(queryArgs.qFromAtr):
            selResul.extend(node.getElementsByTagName(queryArgs.qSelect))
            break


# ~~~~~~~~~~~~~~~~~~~~~~~~~~WHERE~~~~~~~~~~~~~~~~~~~~~~~~~~
whereResul = []
root = []
# ak tam bolo WHERE
if queryArgs.qWhereOperand != None:

    for node in selResul:
        # bool na vypis
        pBool = queryArgs.qWhereNot

        # WHERE element
        if queryArgs.qWhereEl != None and queryArgs.qWhereAtr == None:
            root = node.getElementsByTagName(queryArgs.qWhereEl)
            # Pre kazdy najdeny element
            for nodeWhere in root:
                # Ak WHERE element obsahuje dalsie elementy
                tmp = ET.fromstring(nodeWhere.toxml())
                if len(tmp.getchildren()) != 0:
                    exitFunction("Chybny format vystupniho souboru", INPUT_FORMAT_ERROR)
                # Ak porovnavame cislo
                if queryArgs.qWhereNum != None:
                    # vyhodnotenie operacie
                    pBool = numericOp(nodeWhere.firstChild.nodeValue, queryArgs.qWhereNum, queryArgs.qWhereOperand, queryArgs.qWhereNot)
                # Ak je to string
                elif queryArgs.qWhereStr != None:
                    # vyhodnotenie operacie
                    pBool = stringOp(nodeWhere.firstChild.nodeValue, queryArgs.qWhereStr, queryArgs.qWhereOperand, queryArgs.qWhereNot)
                break

        # WHERE .atribute
        elif queryArgs.qWhereEl == None and queryArgs.qWhereAtr != None:
            root = node.getElementsByTagName("*")
            # Pre kazdy element
            found = False
            for nodeWhere in root:
                # prvy s hladanym atributom
                if node.hasAttribute(queryArgs.qWhereAtr):
                    found = True
                    # Ak porovnavame cislo
                    if queryArgs.qWhereNum != None:
                        # vyhodnotenie operacie
                        pBool = numericOp(node.getAttribute(queryArgs.qWhereAtr), queryArgs.qWhereNum, queryArgs.qWhereOperand, queryArgs.qWhereNot)
                    # Ak je to string
                    elif queryArgs.qWhereStr != None:
                        # vyhodnotenie operacie
                        pBool = stringOp(node.getAttribute(queryArgs.qWhereAtr), queryArgs.qWhereStr, queryArgs.qWhereOperand, queryArgs.qWhereNot)
                    break
            # Ak nebol najdeny - neguje sa hodnota vyrazu
            if not found:
                    pBool = not pBool

        # WHERE atribute.element
        elif queryArgs.qWhereEl != None and queryArgs.qWhereAtr != None:
            # ak je element ten ktory vyberame
            if node.tagName == queryArgs.qWhereEl and node.hasAttribute(queryArgs.qWhereAtr):
                # Ak porovnavame cislo
                if queryArgs.qWhereNum != None:
                    # vyhodnotenie operacie
                    pBool = numericOp(node.getAttribute(queryArgs.qWhereAtr), queryArgs.qWhereNum, queryArgs.qWhereOperand, queryArgs.qWhereNot)
                # Ak je to string
                elif queryArgs.qWhereStr != None:
                    # vyhodnotenie operacie
                    pBool = stringOp(node.getAttribute(queryArgs.qWhereAtr), queryArgs.qWhereStr, queryArgs.qWhereOperand, queryArgs.qWhereNot)
            else:
                root = node.getElementsByTagName(queryArgs.qWhereEl)
                # Pre kazdy najdeny element
                found = False
                for nodeWhere in root:
                    # prvy s hladanym atributom
                    if nodeWhere.hasAttribute(queryArgs.qWhereAtr):
                        found = True
                        # Ak porovnavame cislo
                        if queryArgs.qWhereNum != None:
                            # vyhodnotenie operacie
                            pBool = numericOp(nodeWhere.getAttribute(queryArgs.qWhereAtr), queryArgs.qWhereNum, queryArgs.qWhereOperand, queryArgs.qWhereNot)
                        # Ak je to string
                        elif queryArgs.qWhereStr != None:
                            # vyhodnotenie operacie
                            pBool = stringOp(nodeWhere.getAttribute(queryArgs.qWhereAtr), queryArgs.qWhereStr, queryArgs.qWhereOperand, queryArgs.qWhereNot)
                        break
                # Ak nebol najdeny - neguje sa hodnota vyrazu
                if not found:
                    pBool = not pBool
        # Ak je hodnota vyrazu pravdiva, uklada sa do premennej
        if pBool:
            whereResul.append(node)

# ~~~~~~~~~~~~~~~~~~~~Priprava na zapis~~~~~~~~~~~~~~~~~~~~
# ake neblolo WHERE
if queryArgs.qWhereOperand == None:
    whereResul = selResul

outXML = ""
# Hlavicka
if not fNotGener:
    outXML += '<?xml version="1.0"?>'
# Root element
if fRoot != None:
    outXML += "<"+ fRoot + ">"

# ~~~~~~~~~~~~~~~~~~~~~~~~~~LIMIT~~~~~~~~~~~~~~~~~~~~~~~~~~
i = 0
for node in whereResul:
    # Ak bol dany limit a je presiahnuty, konci s vypisom
    if queryArgs.qLimitNum != None and i >= int(queryArgs.qLimitNum):
        break
    i += 1
    outXML += node.toxml()

# Root element
if fRoot != None:
    outXML += "</"+ fRoot + ">"


# ~~~~~~~~~~~~~~~~~Zapis do suboru/stdout~~~~~~~~~~~~~~~~~
if fOutput != None:
    try:
        tmpfile = open(fOutput, 'w')
    except IOError:
        exitFunction("Nelze otevrit", OUTPUT_ERROR)
else:
    # Vypis na stdout
    tmpfile = sys.stdout

tmpfile.write(outXML+"\n")
tmpfile.close()
