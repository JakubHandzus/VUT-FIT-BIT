package src.board;


import src.cards.*;
import src.game.GameLogic;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Trieda implementujuca operacie nad hrou: Ulozenie hry, nacitanie hry,
 * vytvorenie karty, balicka kariet a vsetych objektov v hre.
 *
 * @author Jakub Handzus (xhandz00@stud.fit.vutbr.cz)
 * @author Martina Grzybowska (xgrzyb00@stud.fit.vutbr.cz)
 */
public final class BoardSet {

    /**
     * Ulozi hru ako objekt.
     * @param data Objekt, implementujuci rozhranie Serializable.
     * @param filename Nazov suboru, do ktoreho sa ulozi objekt.
     * @throws Exception Pri zlyhani zapisu.
     */
    public static void save(Serializable data, String filename) throws Exception {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(Paths.get(filename)))){
            oos.writeObject(data);
            oos.close();
        }
    }

    /**
     * Nacita hru a vrati ju ako objekt.
     * @param filename Subor, v ktorom sa nachadza ulozena hra.
     * @return Objekt reprezentujuci logiku hry.
     * @throws Exception Pri neplatnom/neexistujucom subore.
     */
    public static GameLogic load(String filename) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(Paths.get(filename)))){
            GameLogic loadGame = (GameLogic) ois.readObject();
            ois.close();
            return loadGame;
        }
    }

    /**
     * Vytvori karu otocenu licom nadol.
     * @param color Farba karty.
     * @param value Hodnota karty.
     * @return Karta s danymi hodnotami. Null ak je karta mimo hodnoty.
     */
    public static Card createCard(Card.Color color, int value) {
        if (value > 0 && value <= 13) {
            return new KlondikeCard(color, value, false);
        }
        else {
            return null;
        }
    }

    /**
     * Vytvori zamiesany balicek kariet s 52 kartami.
     * @return Balicek kariet.
     */
    public static Waste createStockPile() {
        WastePile tmpPile = new WastePile();

        for (Card.Color color : Card.Color.values()) {
            for (int value = 1; value <= 13; value++) {
                tmpPile.put(createCard(color, value));
            }
        }

        tmpPile.shuffle();
        return tmpPile;
    }

    /**
     * Vytvori novy odkladaci balicek kariet.
     * @return Odkladaci balicek kariet.
     */
    public static Waste createWastePile() {
        return new WastePile();
    }

    /**
     * Vytvori novy cielovy balicek danej farby.
     * @param color Farba cieloveho balicka.
     * @return Cielovy balicek kariet.
     */
    public static Waste createFoundationPile(Card.Color color) {
        return new FoundationPile(color);
    }

    /**
     * Vytvori novy pracovny balicek.
     * @return Pracovny balicek kariet.
     */
    public static Tableau createTableauPile() {
        return new TableauPile();
    }

}
