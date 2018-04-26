package src.game;

import src.board.BoardSet;
import src.cards.*;
import src.commands.Command;

import java.io.Serializable;
import java.util.List;


/**
 * Trieda implementujuca hernu logiku.
 *
 * @author Jakub Handzus (xhandz00@stud.fit.vutbr.cz)
 * @author Martina Grzybowska (xgrzyb00@stud.fit.vutbr.cz)
 */
public class GameLogic implements Serializable {

    private int score;
    private FoundationPile[] foundationPiles;
    private TableauPile[] tableauPiles;
    private WastePile stockPile;
    private WastePile wastePile;
    private List<Command> commands;

    /**
     * KONSTRUKTOR
     * Vytvori novy objekt hry s kartami vo vsetkych hracich balickov.
     */
    public GameLogic() {

        foundationPiles = new FoundationPile[4];
        tableauPiles = new TableauPile[7];

        this.stockPile = (WastePile) BoardSet.createStockPile();
        this.wastePile = (WastePile) BoardSet.createWastePile();

        int index = 0;
        for (Card.Color color : Card.Color.values()) {
            this.foundationPiles[index] = (FoundationPile) BoardSet.createFoundationPile(color);
            index++;
        }

        for (index = 0; index < 7; index++) {
            this.tableauPiles[index] = (TableauPile) BoardSet.createTableauPile();
            for (int i = 0; i < index + 1; i++) {
                this.tableauPiles[index].origPut(stockPile.pop());
                if (i == index) {
                    this.tableauPiles[index].get().turnFaceUp();
                }
            }
        }
    }

    /**
     * @return Vypis aktualnych pozicii kariet v hre.
     */
    @Override
    public String toString() {
        String tmpStr = "";
        tmpStr += "score: " + Integer.toString(score) + "\n";
        tmpStr += "wastePile:\n" + wastePile.toString();
        tmpStr += "stockPile:\n" + stockPile.toString();
        for (int i = 0; i < 4; i++) {
            tmpStr += "foundationPiles"+ Integer.toString(i) +":\n"+ foundationPiles[i].toString();
        }
        for (int i = 0; i < 7; i++) {
            tmpStr += "tableauPiles" + Integer.toString(i) + ":\n" + tableauPiles[i].toString();
        }
        return tmpStr;
    }

    /**
     * @return Skore hry.
     */
    public int getScore() {
        return this.score;
    }

    public void editScore(int gain) {
        this.score += gain;
        if (this.score < 0) {
            this.score = 0;
        }
    }

    /**
     * Zisti ci je hra dohrana.
     * @return Informacia o ukonceni hry.
     */
    public boolean isVictory() {
        for (int index = 0; index < 4; index++) {
            if (this.foundationPiles[index].get() != null && this.foundationPiles[index].get().value() == 13) {
                continue;
            }
            else {
                return false;
            }
        }
        return true;
    }

    /**
     * Ulozenie tahov hry.
     * @param commands Zoznam tahov hry.
     */
    public void saveCommands(List<Command> commands){
        this.commands = commands;
    }

    /**
     * Nacitanie tahov hry.
     * @return Zoznam tahov hry.
     */
    public List<Command> loadCommands() {
        return commands;
    }

    /**
     * Vrati konkretny pracujuci balicek kariet.
     * @param index Cislo pracujuceho balicka kariet.
     * @return Pracujuci balicek kariet.
     */
    public TableauPile getTableauPile(int index) {
        return this.tableauPiles[index];
    }

    /**
     * Vrati konkretny cielovy balicek kariet.
     * @param index Cislo cieloveho balicka kariet.
     * @return Cielovy balicek kariet.
     */
    public FoundationPile getFoundationPile(int index) {
        return this.foundationPiles[index];
    }

    /**
     * @return Tahaci balicek kariet.
     */
    public WastePile getStockPile() {
        return stockPile;
    }

    /**
     * @return Odkladaci balicek kariet.
     */
    public WastePile getWastePile() {
        return wastePile;
    }

    /**
     * Vrati vrchnu kartu z balicka uceneho indexom.
     * @param index Cislo balicka(0-3 su cielove balickky)
     *                           (4 Tahaci balicek kariet)
     *                           (5 Odkladaci balicek kariet).
     * @return Karta, ak nieje dany balicek prazdny, inak null.
     */
    public Card getUpperCard(int index) {

        switch (index) {
            case 0: case 1: case 2: case 3:
                return this.foundationPiles[index].get();
            case 4:
                return this.stockPile.get();
            case 5:
                return this.wastePile.get();
            default:
                return null;
        }
    }

}
