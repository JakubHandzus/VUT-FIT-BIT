package src.commands;

import src.cards.Card;
import src.cards.WastePile;

import java.io.Serializable;

/**
 * Trieda reprezentujuca prikaz, kedy sa karta presuva z hlavneho balicka
 * kariet do okladacieho balicka.
 *
 * @author Jakub Handzus (xhandz00@stud.fit.vutbr.cz)
 * @author Martina Grzybowska (xgrzyb00@stud.fit.vutbr.cz)
 */
public class StockToWaste implements Command, Serializable {
    private WastePile stock;
    private WastePile waste;
    private int score;

    /**
     * KONSTRUKTOR
     * @param stock Hlavny balicek.
     * @param waste Odkladaci balicek.
     */
    public StockToWaste(WastePile stock, WastePile waste) {
        this.stock = stock;
        this.waste = waste;
        this.score = -1;
    }

    public void execute() {
        if (stock.isEmpty() && waste.isEmpty()) {
            return;
        }

        if (stock.isEmpty()) {
            while (!waste.isEmpty()) {
                Card tmpCard = waste.pop();
                tmpCard.turnFaceDown();
                stock.put(tmpCard);
            }
            this.score = -100;
        }
        else {
            Card tmpCard = stock.pop();
            tmpCard.turnFaceUp();
            waste.put(tmpCard);
            this.score = 0;
        }
    }

    public void undo() {

        if (waste.isEmpty()) {
            while (!stock.isEmpty()) {
                Card tmpCard = stock.pop();
                tmpCard.turnFaceUp();
                waste.put(tmpCard);
            }
        } else {
            Card tmpCard = waste.pop();
            tmpCard.turnFaceDown();
            stock.put(tmpCard);
        }
    }

    public int getResult() {
        return this.score;
    }
}
