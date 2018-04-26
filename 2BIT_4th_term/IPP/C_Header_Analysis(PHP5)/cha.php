<?php

/** PHP skript pre projekt z predmetu IPP
 *  -----------------------------------
 *  Tema: CHA (C Header Analysis)
 *	Autor: Jakub Handzus
 *	Login: xhandz00
 *	Datum: 21.02.2017
 *
 */

/* Konstanty navratovych kodov */
define("WRONG_PARAM", 1);
define("INPUT_ERROR", 2);
define("OUTPUT_ERROR", 3);

/* Stavy automatu */
define("SAVE", 0);
define("SLASH", 1);
define("LINE_COMMT", 2);
define("MULIT_COMMT", 3);
define("END_MULTI_COMMT", 4);
define("MACRO", 5);
define("STR", 6);

iconv_set_encoding("internal_encoding", "UTF-8");

/* HELP priznak */
$help = FALSE;
/* INPUT subor alebo adresar */
$input = NULL;
/* OUTPUT subor */
$output = NULL;
/* XML format */
$XMLoffset = NULL;
/* ziadne INLINE */
$noInline = FALSE;
/* MAX pocet parametrov */
$maxPar = NULL;
/* ziadne duplikaty */
$noDuplicates = FALSE;
/* vymaz biele znaky */
$removeSpace = FALSE;


try {
	argumentsLoad($argv);

	// Volanie funkcie help
	if ($help) {
		printHelp();
		exit(0);
	}	

	/*---------------Spracovanie inputu parametra-----------------------*/
	// Bez input parametra	
	if ($input === NULL) {
		$input = getcwd() . "/";
		$files = sortFiles(getHeadersFile($input));
		$xmlDir = "./";
	}
	elseif (is_file($input)) {
		$file = realpath($input);
		$files[] = $file;
		$xmlDir = "";
	}
	elseif (is_dir($input)) {
		if (substr($input, -1) !== "/") {
			$input .= "/";
		}
		$files = sortFiles(getHeadersFile($input));
		$xmlDir = $input;
	}
	else {
		throw new customException("Neplatny vstupni soubor nebo adresar", INPUT_ERROR);
	}
	/*------------------------------------------------------------------*/
	// Odsadenie na novy riadok pri prepinaci --pretty-xml 
	$newLine = $XMLoffset !== NULL ? "\n" : "";
	// Vypis do pomocnej premennej
	$xmlDoc = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	$xmlDoc .= $newLine."<functions dir=\"".$xmlDir."\">";

	/*---------------Analyza kazdeho suboru zvlast----------------------*/
	foreach ($files as $key => $file) {
		// Citanie subora
		if (is_readable($file)) {
			/* Najdolezitejsia cast skrpiptu */
			$content = file_get_contents($file);
			$editFile = junkDelete($content);
			analysis($file, $editFile);
			/* ----------------------------- */
		}
		else {
			throw new customException("Nelze otevrit soubor".$file, INPUT_ERROR);
		}

	}

	$xmlDoc .= $newLine."</functions>"."\n";

	
	/* Zaistenie OUTPUT subora */
	if(isset($output)) {
		@$output= fopen($output, 'w');
		if (!$output) {
			throw new customException("Nelze vytvorit vystupni soubor", OUTPUT_ERROR);
		}
	}
	else {
		$output = STDOUT;
	}

	// Zapis do output subora/na standartny vystup 
	fwrite($output, $xmlDoc);
	fclose($output);
	exit(0);

}

catch (customException $e) {
	//vypise spravu na errorovy vystup a ukonci program s urcenou navratovou hodnotou
	fprintf(STDERR, $e->errorMessage());
	exit($e->retval);
}

/**
 * Funkcia rekurzivne vyhladava hlavickove subory v adresari a podadresaroch
 *
 * @param      string  $dir      Adresar
 * @param      array   $results  Pole ciest hlavickovych suborov
 *
 * @return     array   Pole ciest hlavickovych suborov
 */
function getHeadersFile($dir, &$results = array()){
	// Zoskenuje adresar
	$files = scandir($dir);

	// Pre vsetky adresare a subory
	foreach ($files as $key => $file) {    	
		$path = $dir.$file;
		// Pokial je to subor s priponou ".h", pridaj ho do pola
		if (!is_dir($path) && ((substr($path, -2)) === ".h" )) {
			$results[] = $path;
		}
		// Pokial je to adresar ale nieje to "." alebo ".." rekurzivne sa zavolaj
		elseif (is_dir($path) && $file != "." && $file != "..") {
			$path .= DIRECTORY_SEPARATOR;
			getHeadersFile($path, $results);            
		}
	}
	return $results;
}

/**
 * Zoradi pole ciest suborov, na zaklade hlbky zanorenia v podadresaroch
 *
 * @param      array of string  $files  Pole nezoradenych suborov s ich cestami
 *
 * @return     array of string  Zoradene pole suborov
 */
function sortFiles($files) {
	foreach ($files as $key => $value) {
		// spocita uroven zanorenia
		$sortFiles[$value] = substr_count($value, '/');
	}
	//pokial nieje prazdne
	if(!empty($sortFiles)) {
		asort($sortFiles);
		unset($files);
		$i = 0;
		//prehodenie kluca a hodnoty
		foreach ($sortFiles as $key => $value) {
			$files[$i++] = $key;
		}
	}
	return $files;
}

/**
 * Rozdeli retazec na pole znakov
 *
 * @param      string  $str    Retazec
 *
 * @return     array   pole znakov
 */
function utf8Split($str) {
	$arr = array();
	$strLen = mb_strlen($str, 'UTF-8');
	for ($i = 0; $i < $strLen; $i++) {
		$arr[] = mb_substr($str, $i, 1, 'UTF-8');
	}
	return $arr;
}

/**
 * Funkcia vymaze komenty, retazce a makra
 * Je zalozena na konecnom automate. 
 * Konecny automat je blizsie popisany v dokumentacii
 *
 * @param      string $content  obsah analyzovaneho suboru
 * 
 * @return     string $editFile	kde je ulozeny uzitocny kod pre analyzu
 */
function junkDelete($content) {

	$state = SAVE;
	$prevState = SAVE;
	$ESCskip = FALSE;
	$useless = FALSE;
	$editFile = NULL;
	// Osetrenie CL RF
	$content = preg_replace("/\r\n/", "\n", $content);
	// pole pismen
	$contentArray = utf8Split($content);

	$from = 0;
	$i = 0;
	$length = count($contentArray);

	while ($i < $length) {
		$char = $contentArray[$i];
		switch ($state) {
			case SAVE:
				if ($char === "#") {
					$editFile .= iconv_substr($content, $from, $i - $from);
					$state = MACRO;
				}
				elseif ($char === "/") {
					$state = SLASH;
					$prevState = SAVE;
				}
				elseif ($char === "\"" || $char === "'") {
					$editFile .= iconv_substr($content, $from, $i - $from + 1);
					$state = STR;
					$prevState = SAVE;
					$strEnd = $char;
				}
				elseif ($i === $length - 1) {
					$editFile .= iconv_substr($content, $from, $i - $from + 1);
				}
				break;

			case MACRO:
				if ($ESCskip === TRUE) {
					$ESCskip = FALSE;
				}
				elseif ($char === "\\") {
					$ESCskip = TRUE;
				}
				elseif ($char === "/") {
					$state = SLASH;
					$prevState = MACRO;
				}
				elseif ($char === "\n") {
					$state = SAVE;
					$from = $i;
				}
				break;

			case SLASH:
				if ($char === "/") {
					if ($prevState === SAVE) {
						$editFile .= iconv_substr($content, $from, $i - $from - 1);
					}
					$state = LINE_COMMT;
				} elseif ($char === "*") {
					if ($prevState === SAVE) {
						$editFile .= iconv_substr($content, $from, $i - $from - 1);
					}
					$state = MULIT_COMMT;
				} 
				else {
					$state = $prevState;
				}
				break;

			case LINE_COMMT:
				if ($ESCskip === TRUE) {
					$ESCskip = FALSE;
					if ($prevState === MACRO) {
						$state = MACRO;
					}
				}
				elseif ($char === "\n") {
					if ($prevState === MACRO) {
						$state = SAVE;
					}
					else {
						$state = $prevState;
					}
					$from = $i;
				}
				elseif ($char ==="\\") {
					$ESCskip = TRUE;
				}
				break;

			case MULIT_COMMT:
				if ($char === "*") {
					$state = END_MULTI_COMMT;
				}
				break;
			
			case END_MULTI_COMMT:
				if ($char === "/") {
					if ($prevState === MACRO) {
						$state = MACRO;
					}
					else {
						$state = SAVE;
						$from = $i + 1;
					}
				}
				elseif ($char !== "*") {
					$state = MULIT_COMMT;
				}
				break;

			case STR:
				if ($ESCskip === TRUE) {
					$ESCskip = FALSE;
				}
				elseif ($char === "\\") {
					$ESCskip = TRUE;
				}
				elseif ($char === $strEnd) {
					$from = $i;
					$state = $prevState;
				}
				break;

			default:
				$state = $state;
				break;
		}
		$i++;
	}
	return $editFile;
}

/**
 * Najdvolezitejsia funkcia celeho skriptu. Analyzuje vstupny subor,
 * a prida do globalnej premennej ($xmlDoc) vacsinu XML elementov.
 * Funkcia je zalozena na regularnom vyraze, ktory analyzuje funkcie v subore.
 * V funkcii sa vyuzivaju prepinace, ktore boli zadane ako argumenty skriptu
 *
 * @param      string  $file      Cesta suboru, ktora sa vypise
 * @param      string  $editFile  Upraveny obsah suboru (uz po odstraneni nechcenych casti)
 */
function analysis($file, $editFile) {

	//Argumenty
	global $help, $input, $output, $XMLoffset, $noInline, $maxPar, $noDuplicates, $removeSpace;
	global $xmlDoc, $newLine;
	$arrNames =  array();
	
	// Regularny vyraz najde deklaraciu alebo definiciu funkcie, a roztriedi ju na navratovu hodnotu, meno a vsetky parametre
	$funCount = preg_match_all("/\s*(?<ret>(?:[a-zA-Z_]\w*[\s\*]+)+)(?<name>[a-zA-Z_]\w*)\s*\((?<par>[\s\S]*?)\)\s*(\{|;)/u", $editFile, $function);

	// Pre kazdu funkciu 
	for ($i = 0; $i < $funCount; $i++) {
		$varargs = FALSE;
		$xmlPar = NULL;	// vymazanie pola parametrov funkcie

		// Uprava navratovej hodnoty a parametrov - nahradenie bielych znakov medzerami + orezanie
		$function['ret'][$i] = str_replace(array(PHP_EOL, "\t"), ' ', $function['ret'][$i]);
		$function['ret'][$i] = trim($function['ret'][$i]);
		$function['par'][$i] = str_replace(array(PHP_EOL, "\t"), ' ', $function['par'][$i]);
		$function['par'][$i] = trim($function['par'][$i]);
		$function['par'][$i] = explode(",", $function['par'][$i]);

		// Prepinac --no-inline
		if ($noInline) {
			if (preg_match("/\s*(?:inline)\s+/u", $function['ret'][$i])) {
				continue;
			}
		}
		// Zisti realny pocet parametrov
		$parCount = realPar($function['par'][$i], $varargs, $xmlPar);

		// Prepinac --max-param=
		if ($maxPar !== NULL && $parCount > $maxPar) {
			continue;			
		}
		// Prepinac -no-duplicates
		if ($noDuplicates) {
			if (in_array($function['name'][$i], $arrNames)) {
				continue;
			}
			else {
				array_push($arrNames, $function['name'][$i]);
			}
		}
		// Pripravenie vypisu cesty suboru
		if (is_file($input)){
			$xmlFile = $input;
		}
		else {
			$xmlFile = preg_replace("~^".addslashes($input)."~", "", $file);
		}
		// Prepinac --remove-whitespace
		if ($removeSpace) {
			$function['ret'][$i] = removeSpace($function['ret'][$i]);
		}		
		// Prepinac --pretty-xml
		$offset = str_repeat(" ", $XMLoffset);

		$xmlDoc .= $newLine.$offset."<function file=\"". $xmlFile."\" name=\"".$function['name'][$i]."\" varargs=\"".($varargs ? "yes" : "no")."\" rettype=\"".$function['ret'][$i]."\">";
		$xmlDoc .= $xmlPar;
		$xmlDoc .= $newLine.$offset."</function>";
	}
}

/**
 * Funkcia sa stara o analyzu parametrov funkcie a nasledne ulozenie do XML formatu 
 *
 * @param      <type>          $parArr   Pole vsetkych parametrov funkcie
 * @param      <type>          $varargs  Nastavi na true ak ma funkcia premenlivy pocet parametrov  
 * @param      <type>          $xmlPar   XML element pre vsetky parametre, ktore budu vypisane na vystupe
 *
 * @return     integer  	Pocet uzitocnych parametrov na analyzu
 */
function realPar(&$parArr, &$varargs, &$xmlPar) {
	global $help, $input, $output, $XMLoffset, $noInline, $maxPar, $noDuplicates, $removeSpace;
	global $newLine;
	$parNumber = 0;
	// Analyza pre kazdy parameter
	foreach ($parArr as $key => $value) {
		// Ak ma funkcia premenlivy pocet parametrov, nastavi $varargs ako TRUE
		if (trim($value) === "...") {
			$varargs = TRUE;
			unset($parArr[$key]);
		}
		// Ak je void alebo prazdny parameter, nezapisuje sa do vysledneho XML suboru
		elseif (trim($value) === "void" || trim($value) === ""){
			unset($parArr[$key]);
		}
		//Inak regularny vyraz rozanalyzuje typy parametrov a zapise to do pomocnej premennej, ktora sa vypise do XML dokumentu
		else {
			preg_match_all("/(?<type>(?:[a-zA-Z_]\w*(?:[\s\*]|\[[^\]]*\])*)+?[\s\*]*)(?<name>[a-zA-Z_]\w*(\[[^\]]*\]*)*)*?\s*$/u", $value, $functionPar);
			$parArr[$key] = trim($functionPar['type'][0]);
			if ($parArr[$key] === "") {
				continue;
			}
			$parNumber++;

			// Prepinac --remove-whitespace
			if ($removeSpace) {
				$parArr[$key] = removeSpace($parArr[$key]);
			}

			// Zapis do xml
			$offset = str_repeat(" ", 2*$XMLoffset);
			$xmlPar .= $newLine.$offset."<param number=\"".$parNumber."\" type=\"".$parArr[$key]."\" />";
		}
	}
	return $parNumber;
}

/**
 * Nahradi viacero bielych znakov jednou medzerou
 *
 * @param      string  $str    retazec
 *
 * @return     retazec bez zbytocnych medzier
 */
function removeSpace($str) {
	$str = preg_replace("/\s\s+/u", " ", $str);
	$str = preg_replace("/\s*\*\s*/u", "*", $str);
	return $str;
}

/**
 * Funkcia nacita argumenty a nastavi globalne premenne
 *
 * @param      array            $argv   Pole vstupnych argumentov
 *
 * @throws     customException  Ked nastane chyba pri nacitavani
 */
function argumentsLoad(array $argv) {

	global $help, $input, $output, $XMLoffset, $noInline, $maxPar, $noDuplicates, $removeSpace;

	// Vymaze nazov skriptu
	unset($argv[0]);

	// Pre kazdy argument spravi vyhodnotenie
	foreach($argv as $arg) {

		/* HELP */
		if ($arg === "--help") {
			if($help == TRUE) {
				throw new customException("Vice --help v argumentech", WRONG_PARAM);
			}
			// Je help jedniny?
			if (count($argv) > 1) {
				throw new customException("--help musi byt pouze jediny parametr", WRONG_PARAM);
			}
			$help = TRUE;
		}

		/* INPUT subor alebo adresar */
		elseif (substr($arg, 0, strlen("--input=")) === "--input=") {
			if ($input !== NULL) {
				throw new customException("Vice --input argumentu", WRONG_PARAM);
			}
			if (strlen(substr($arg, strlen("--input="))) === 0) {
				throw new customException("Zadny adresar nebo soubor v --input argumentu", INPUT_ERROR);
			}
			$input = substr($arg, strlen("--input="));
		}

		/* OUTPUT subor */
		elseif (substr($arg, 0, strlen("--output=")) === "--output=") {
			if ($output !== NULL) {
				throw new customException("Vice --output argumentu", WRONG_PARAM);
			}
			$output = substr($arg, strlen("--output="));
		}

		/* XML format */
		elseif (substr($arg, 0, strlen("--pretty-xml")) === "--pretty-xml") {
			if ($XMLoffset !== NULL) {
				throw new customException("Vice --pretty-xml=... argumentu", WRONG_PARAM);
			}
			$tmp = substr($arg, strlen("--pretty-xml"));
			if (substr($tmp, 0, 1) === "=") {
				$tmp = substr($tmp, 1);
				if (ctype_digit($tmp)) {
					$XMLoffset = $tmp;
				}
				else {
					throw new customException("V --pretty-xml=k, k neni cele cislo", WRONG_PARAM);
				}
			}
			elseif (strlen($tmp) === 0) {
				$XMLoffset = 4;
			}
			else {
				throw new customException("Za --pretty-xml je nedefinovany retezec", WRONG_PARAM);
			}
		}

		/* ignoruj inline */
		elseif ($arg === "--no-inline") {
			if ($noInline) {
				throw new customException("Vice --no-inline argumentu", WRONG_PARAM);
			}
			$noInline = TRUE;
		}

		/* MAX pocet parametrov */
		elseif (substr($arg, 0, strlen("--max-par=")) === "--max-par=") {
			if ($maxPar != NULL) {
				throw new customException("Vice --max-par=... argumentu", WRONG_PARAM);
			}
			$tmp = substr($arg, strlen("--max-par="));
			if (strlen($tmp) === 0) {
				throw new customException("Pocet nebyl zadan v --max-par=", WRONG_PARAM);
			}
			elseif (ctype_digit($tmp)) {
				$maxPar = $tmp;
			}
			else {
				throw new customException("V --max-par=n, n neni cele cislo", WRONG_PARAM);
			}
		}

		/* ziadne duplikaty */
		elseif ($arg === "--no-duplicates"){
			if ($noDuplicates) {
				throw new customException("Vice --no-duplicates argumentu", WRONG_PARAM);
			}
			$noDuplicates = TRUE;
		}

		/* vymaze biele znaky */
		elseif ($arg === "--remove-whitespace"){
			if ($removeSpace) {
				throw new customException("Vice --remove-whitespace argumentu", WRONG_PARAM);
			}
			$removeSpace = TRUE;
		}

		/* Nepovoleny argument */
		else {
			throw new customException("Neplatny argument" . $arg, WRONG_PARAM);
		}

	}
}

/**
 * Funkcia, ktora vypise na STDOUT informacie o skripte
 */
function printHelp() {
	print "Skript pro analyzu hlavickovych souboru jazyka C (pripona .h) podle standardu ISO C99,\nktery vytvori databazi nalezenych funkci v techto souborech.\n";
	print "Vsechny parametry jsou nepovinne a na jejich poradi nezalezi\n\n";
	print "--help\t\t\t Vypise na standardni vystup napovedu skriptu\n\n";
	print "--input=fileordir\t Zadany vstupni soubor nebo adresar se zdrojovym kodem v jazyce C\n\n";
	print "--output=filename\t Zadany vystupni soubor ve formatu XML v kodovani UTF-8.\n\n";
	print "--pretty-xml=k\t\t Pri pouziti tohoto parametru skript zformatuje vysledny XML dokument tak, ze kazde nove zanoreni bude odsazeno o k mezer oproti predchozimu a XML hlavicka bude od korenoveho elementu oddelena znakem noveho radku. Pokud k neni zadano, tak se pouzije hodnota 4.\n\n";
	print "--no-inline\t\t Pri pouziti tohoto parametru skript preskoci funkce deklarovane se specifikatorem inline";
	print "--max-par=n\t Pri pouziti tohoto parametru skript bude brat v uvahu pouze funkce, ktere maji n ci mene parametru (n musi byt vzdy zadano).";
	print "--no-duplicates\t Pri pouziti tohoto parametru pokud se v souboru vyskytne vice funkci se stejnym jmenem, tak se do vysledneho XML souboru ulozi pouze prvni z nich.\n\n";
	print "--remove-whitespace\t Pri pouziti tohoto parametru skript odstrani z obsahu atributu rettype a type vsechny prebytecne mezery.\n";
}

/**
 * Trieda, ktora je volana v pripade chyby
 */
class customException extends Exception {
	public $errorMsg, $retval;

	public function __construct($errorMsg, $retval) {
		$this->errorMsg = $errorMsg;
		$this->retval = $retval;
	}

	/**
	 * @return     string  Retezec, v ktorom je popisana chyba a navratovy kod
	 */
	public function errorMessage() {
		//error sprava
		return "Chyba: " . $this->errorMsg . " | Navratovy kod: " . $this->retval . "\n";
	}
}

?>
