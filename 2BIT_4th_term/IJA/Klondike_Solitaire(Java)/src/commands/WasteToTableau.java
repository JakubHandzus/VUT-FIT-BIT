package src.commands;

import src.cards.TableauPile;
import src.cards.WastePile;

import java.io.Serializable;

/**
 * Trieda reprezentujuca prikaz, kedy sa karta presuva z odkladacieho balicka
 * kariet do jedneho z pracovnych balickov.
 *
 * @author Jakub Handzus (xhandz00@stud.fit.vutbr.cz)
 * @author Martina Grzybowska (xgrzyb00@stud.fit.vutbr.cz)
 */
public class WasteToTableau implements Command, Serializable {
    private WastePile waste;
    private TableauPile tableau;
    private int score;

    /**
     * KONSTRUKTOR
     * @param waste Odkladaci balicek.
     * @param tableau Jeden z pracovnych balickov.
     */
    public WasteToTableau(WastePile waste, TableauPile tableau) {
        this.waste = waste;
        this.tableau = tableau;
        this.score = -1;
    }

    public void execute() {
        if (tableau.put(waste.get())) {
            waste.pop();
            this.score = 5;
        }
    }

    public void undo() {
        waste.put(tableau.pop());
    }

    public int getResult() {
        return this.score;
    }

}
