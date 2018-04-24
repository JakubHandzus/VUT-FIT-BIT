Vytvořte komunikující aplikaci podle konkrétní vybrané specifikace
pomocí síťové knihovny BSD sockets (pokud není ve variantě zadání
uvedeno jinak). Projekt bude vypracován v jazyce C/C++. Pokud
individuální zadání nespecifikuje vlastní referenční systém, musí být
projekt přeložitelný a spustitelný na serveru merlin.fit.vutbr.cz.\
\
Vypracovaný projekt uložený v archívu .tar a se jménem xlogin00.tar
odevzdejte elektronicky přes IS. Soubor nekomprimujte.

-   **Termín odevzdání je 20.11.2017** **(hard deadline)**. Odevzdání
    emailem po uplynutí termínu není možné.
-   Odevzdaný projekt musí obsahovat:
    1.  soubor se zdrojovým kódem (dodržujte jména souborů uvedená v
        konkrétním zadání),
    2.  funkční *Makefile*pro překlad zdrojového souboru,
    3.  dokumentaci (soubor *manual.pdf*), která bude obsahovat uvedení
        do problematiky, návrhu aplikace, popis implementace, základní
        informace o programu, návod na použití. V dokumentaci se očekává
        následující: titulní strana, obsah, logické strukturování textu,
        přehled nastudovaných informací z literatury, popis
        zajímavějších pasáží implementace, použití vytvořených programů
        a literatura.
    4.  soubor *README*obsahující krátký textový popis programu s
        případnými rozšířeními/omezeními, příklad spuštění a seznam
        odevzdaných souborů,
    5.  další požadované soubory podle konkrétního typu zadání. 
-   Pokud v projektu nestihnete implementovat všechny požadované
    vlastnosti, je nutné veškerá omezení jasně uvést v dokumentaci a v
    souboru README.
-   Co není v zadání jednoznačně uvedeno, můžete implementovat podle
    svého vlastního výběru. Zvolené řešení popište v dokumentaci.
-   Při řešení projektu respektujte zvyklosti zavedené v OS unixového
    typu (jako je například formát textového souboru).
-   Vytvořené programy by měly být použitelné a smysluplné, řádně
    komentované a formátované a členěné do funkcí a modulů. Program by
    měl obsahovat nápovědu informující uživatele o činnosti programu a
    jeho parametrech. Případné chyby budou intuitivně popisovány
    uživateli.
-   Aplikace nesmí v žádném případě skončit s chybou SEGMENTATION FAULT
    ani jiným násilným systémovým ukončením (např. dělení nulou).
-   Pokud přejímáte velmi krátké pasáže zdrojových kódů z různých
    tutoriálů či příkladů z Internetu (ne mezi sebou), tak je nutné
    vyznačit tyto sekce a jejich autory dle licenčních podmínek, kterými
    se distribuce daných zdrojových kódů řídí. V případě nedodržení bude
    na projekt nahlíženo jako na plagiát.
-   Konzultace k projektu podává vyučující, který zadání vypsal.
-   Před odevzdáním zkontrolujte, zda jste dodrželi všechna jména
    souborů požadovaná ve společné části zadání i v zadání pro konkrétní
    projekt. Zkontrolujte, zda je projekt přeložitelný.

**Hodnocení projektu**:

-   **Maximální počet bodů za projekt je 20 bodů.**
-   Příklad kriterií pro hodnocení projektů:
    -   nepřehledný, nekomentovaný zdrojový text: až -7 bodů
    -   nefunkční či chybějící Makefile: až -4 body
    -   nekvalitní či chybějící dokumentace: až -8 bodů
    -   nedodržení formátu vstupu/výstupu či konfigurace: -10 body
    -   odevzdaný soubor nelze přeložit, spustit a odzkoušet: 0 bodů
    -   odevzdáno po termínu: 0 bodů
    -   nedodržení zadání: 0 bodů
    -   nefunkční kód: 0 bodů
    -   opsáno: 0 bodů (pro všechny, kdo mají stejný kód)

\

**Popis varianty:**

Vytvorte program popser, ktorý bude plniť úlohu POP3 [1] serveru (ďalej
iba server). Na server sa budú pripájať klienti, ktorý si pomocou
protokolu POP3 sťahujú dáta zo serveru. Server bude pracovať s emailmi
uloženými vo formáte IMF [4] v adresárovej štruktúre typu Maildir [2].\
\
Vašim cieľom je teda vytvoriť program, ktorý bude vedieť pracovať s
emilami uloženými v adresárovej štruktúre Maildir. Program bude pracovať
iba s jednou poštovou schránkou. Takto uložené emaily budú
prostredníctvom protokolu POP3 poskytované jedinému používateľovi.\
\
**Vyžadované vlastnosti:**\

Program využíva iba hlavičkové súbory pro prácu so soketmi a ďalšie
funkcie používané v sieťovom prostredí (ako je netinet/\*, sys/\*,
arpa/\* apod.), knižnicu pre prácu s vláknami (pthread), signálmi,
časom, rovnako ako štandardnú knižnicu jazyka C (varianty ISO/ANSI i
POSIX), C++ a STL. Ostatné knižnice nie sú povolené.

Program nespúšťa žiadne ďalšie procesy.

Pre prácu s emailom sa musí používateľ vždy autentizovať.

Server musí využívať paralelný a neblokujúci soket.

Preklad sa spustí príkazom "make", ktorý vytvorí binárku s názvom
"popser".

V odovzdanom archíve sa nenachádzajú žiadne adresáre.

Povolené jazyky pre dokumentáciu, readme a komentáre zdrojových kódov sú
slovenčina, čeština a angličtina.

Všetky súbory, ktoré sú potrebné k behu programu sa musia nachádzať v
rovnakej zložke ako binárka. Používateľ, ktorý daný proces spúšťa bude
mať vždy právo na zápis+čítanie z danej zložky.

Okrem parametru -h by program na stdout nemal vypisovať vôbec nič.
Všetky chybové (napr. port obsadený) a ladiace výpisy vypisujte na
stderr.

V prípade, že nastane ľubovoľná chyba (napr. port obsadený, zlé
parametre), je potrebné na STDERR vypísať chybovú hlášku popisujúcu
problém (text typu "Nastala nejaká chyba" je nedostačujúci). Nie je
vyžadovaný žiadny striktný formát, je len potrebné aby používateľ z
textu pochopil problém.

Poradie parametrov nie je pevne určené.

Na danej stanici beží vždy len 1 proces pracujúci s danou Maildir
zložkou. Takže nie je nutné riešiť vyhradený prístup k danej zložke.

Je potrebné implementovať celú špecifikáciu POP3 (povinné, odporúčané aj
voliteľné časti [3]) [1] vrátane sekcie č.13, okrem:

-   implementácie šifrovania (TLS/SSL),
-   sekcie č.8, ktorá obsahuje "úvahy" (norma totiž nevyžaduje ich
    implementovanie),
-   príkaz TOP, ktorý bude braný iba ako rozšírenie projektu.

NEimplementujte žiadne ďalšie rozšírenia ani "triky" z protokolu IMAP.

S výnimkou príkazov TOP a RETR je možné čítať obsah súborov s emailmi
maximálne 1x a to pri presune emailu zo zložky /new/ do /cur/ v rámci
adresárovej štruktúry Maildiru.

Server beží až kým neprijme signál SIGINT. Vtedy musí svoj beh správne a
bezpečne ukončiť.

Je potrebné počítať s tým, že proces serveru môže byť ukončený a
následne znovu spustený.

Do adresárovej štruktúry Maildir sa nevytvárajú žiadne nové súbory ani
zložky. Jediná vykonávaná činnosť je presunutie/premenovanie už
existujúcich súborov.

Pracujte len s IPv4.

\

**Súbor s prihlasovacími údajmi:**

Konfiguračný súbor s prihlasovacími údajmi bude obsahovat meno a heslo v
jednoduchom formáte (dodržujte konvencie pre textové súbory v prostredí
UNIX/Linux):

`username = menopassword = heslo`

**\
Použitie:**\
./popser [-h] [-a PATH] [-c] [-p PORT] [-d PATH] [-r]\

-   h (help) - voliteľný parameter, pri jeho zadaní sa vypíše nápoveda a
    program sa ukončí
-   a (auth file) - cesta k súboru s prihlasovacími údajmi
-   c (clear pass) - voliteľný parameter, pri zadaní server akceptuje
    autentizačnú metódu, ktorá prenáša heslo v nešifrovanej podobe (inak
    prijíma iba heslá v šifrovanej podobe - hash)
-   p (port) - číslo portu na ktorom bude bežať server
-   d (directory) - cesta do zložky Maildir (napr. \~/Maildir/)
-   r (reset) - voliteľný parameter, server vymaže všetky svoje pomocné
    súbory a emaily z Maildir adresárovej štruktúry vráti do stavu, ako
    keby proces popser nebol nikdy spustený (netýka sa časových
    pečiatok, iba názvov a umiestnení súborov) (týka sa to len emailov,
    ktoré sa v adresárovej štruktúre nachádzajú).

**3 režimy behu:**

-   výpis nápovedy - zadaný parameter "-h"
-   iba reset - zadaný iba parameter "-r"
-   bežný režím - zadané parametre "-a", "-p", "-d" a voliteľné
    parametre "-c" a "-r"

**Časté chyby v dokumentáciách**(väčšina bodov platí pre každú
dokumentáciu, technickú správu a teda aj bakalársku prácu)**:**

-   chýba titulná strana
-   chýba obsah
-   zlé číslovanie obsahu (číslovanie chýba úplne alebo čísla strán sú
    nesprávne)
-   použitie nevhodných citácií (citovanie z podivných stránok namiesto
    noriem)
-   chýbajúce odkazy na literatúru v texte
-   nekonzistentná norma vysádzania literatúry
-   chýbajúce obrázky v texte (záleží od zadania, ale väčšinou sa vždy
    nájde minimálne 1 vhodný obrázok do dokumentácie)
-   nekonzistentné obrázky (všetky texty vo všetkých obrázkoch musia mať
    rovnaký typ a veľkosť písma, texty v obrázkoch sú v rovnakom jazyku
    ako text práce, všetky obrázky majú rovnaký vizuálny štýl)
-   každý obrázok by mal byť očíslovaný, obsahovať popis a malo by sa na
    neho odkazovať v texte
-   popis k čomu vlastne vytvorený program slúži
-   popis ako vôbec program spustiť
-   krátka dokumentácia
-   nepoužívanie nezalomiteľných medzier
-   nezarovnávanie textu do blokov
-   zlé používanie čiarok a bodiek v zoznamoch
-   chýbajúce zvýrazňovanie textu (tučné písmo, kurzíva, podčiarknutý
    text - zároveň však nezvýrazňovať každú maličkosť)
-   chýba číslovanie strán
-   nečlenenie textu do kapitol a podkapitol (zároveň to nepreháňať)
-   príliš krátke odstavce (odstavce o veľkosti 1-2 riadky vyzerajú
    divne - hlavne ak ich je 5 za sebou)
-   príliš dlhé odstavce
-   viacero myšlienok v 1 odstavci (správne: 1 odstavec = 1 hlavná
    myšlienka)
-   viacero myšlienok v 1 vete (správne: 1 veta = 1 myšlienka)
-   to, že niekto použije latex automaticky neznamená, že dokumentácia
    vyzerá dobre

*!!! V prvom rade je nutné mať kvalitný text. Splnením vyššie uvedených
bodov (práca vyzerá typograficky veľmi dobre, neobsahuje žiadne chyby)
študent nezíska ani bod, ak po obsahovej stránky je práca odfláknutá,
obsahuje hlúposti a nedá sa čítať !!!*\
\
**Rozšírenie** (súčet bodov za projekt nemôže presiahnuť 20b):\

-   príkaz TOP

\
**Literatúra:** \

1.  https://tools.ietf.org/html/rfc1939
2.  https://cr.yp.to/proto/maildir.html
3.  https://tools.ietf.org/html/rfc2119
4.  https://tools.ietf.org/html/rfc5322

