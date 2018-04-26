package src.commands;

import src.cards.FoundationPile;
import src.cards.WastePile;

import java.io.Serializable;

/**
 * Trieda reprezentujuca prikaz, kedy sa karta presuva z odkladacieho balicka
 * kariet do cieloveho balicka.
 *
 * @author Jakub Handzus (xhandz00@stud.fit.vutbr.cz)
 * @author Martina Grzybowska (xgrzyb00@stud.fit.vutbr.cz)
 */
public class WasteToFoundation implements Command, Serializable {
    private WastePile waste;
    private FoundationPile foundation;
    private int score;

    /**
     * KONSTRUKTOR
     * @param waste Okladaci balicek.
     * @param foundation Cielovy balicek.
     */
    public WasteToFoundation(WastePile waste, FoundationPile foundation) {
        this.waste = waste;
        this.foundation = foundation;
        this.score = -1;
    }

    public void execute() {
        if (foundation.put(waste.get())) {
            waste.pop();
            this.score = 10;
        }
    }

    public void undo() {
        if (waste.put(foundation.get())) {
            foundation.pop();
        }
    }

    public int getResult() {
        return this.score;
    }
}
