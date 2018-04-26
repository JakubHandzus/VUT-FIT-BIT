package src.commands;

import src.cards.Card;
import src.cards.FoundationPile;
import src.cards.TableauPile;

import java.io.Serializable;

/**
 * Trieda reprezentujuca prikaz, kedy sa karta presuva z pracovneho balicka
 * kariet do cieloveho balicka.
 *
 * @author Jakub Handzus (xhandz00@stud.fit.vutbr.cz)
 * @author Martina Grzybowska (xgrzyb00@stud.fit.vutbr.cz)
 */
public class TableauToFoundation implements Command, Serializable {

    private TableauPile tableau;
    private FoundationPile foundation;
    private Card indentedCard;
    private int score;
    private boolean wasTurned;

    /**
     * KONSTRUKTOR
     * @param tableau Jeden z pracovnych balickov.
     * @param foundation Jeden z cielovych balickov.
     * @param indentedCard Karta na ktoru bolo kliknute z pracovneho balicka.
     */
    public TableauToFoundation(TableauPile tableau, FoundationPile foundation, Card indentedCard) {
        this.tableau = tableau;
        this.foundation = foundation;
        this.indentedCard = indentedCard;
        this.score = -1;
        this.wasTurned = false;
    }

    public void execute() {
        if (tableau.get() == indentedCard){
            if (foundation.put(tableau.get())) {
                tableau.pop();
                this.score = 10;
                if (!tableau.isEmpty()) {
                    if (!tableau.get().isTurnedFaceUp()) {
                        tableau.get().turnFaceUp();
                        this.score += 5;
                        this.wasTurned = true;
                    }
                }
            }
        }
    }

    public void undo() {
        if (this.wasTurned) {
            tableau.get().turnFaceDown();
            this.wasTurned = false;
        }
        tableau.origPut(foundation.pop());
    }

    @Override
    public int getResult() {
        return this.score;
    }
}
