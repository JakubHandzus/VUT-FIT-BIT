package src.commands;

import src.cards.Card;
import src.cards.TableauPile;

import java.io.Serializable;

/**
 * Trieda reprezentujuca prikaz, kedy sa karta presuva z pracovneho balicka
 * kariet do ineho pracovneho balicka.
 *
 * @author Jakub Handzus (xhandz00@stud.fit.vutbr.cz)
 * @author Martina Grzybowska (xgrzyb00@stud.fit.vutbr.cz)
 */
public class TableauToTableau implements Command, Serializable {
    private TableauPile source;
    private TableauPile dest;
    private Card indentedCard;
    private int score;
    private boolean wasTurned;

    /**
     * KONSTRUKTOR
     * @param source Zdrojovy balicek z pracovnych balickov.
     * @param dest Cielovy balicek z pracovnych balickov.
     * @param indentedCard Karta, na ktoru bolo kliknute.
     */
    public TableauToTableau(TableauPile source, TableauPile dest, Card indentedCard) {
        this.source = source;
        this.dest = dest;
        this.indentedCard = indentedCard;
        this.score = -1;
        this.wasTurned = false;
    }

    public void execute() {
        if (indentedCard != null && indentedCard.isTurnedFaceUp()) {
            if (dest.put(source.getPile(indentedCard))) {
                this.score = 0;
                source.pop(indentedCard);
                if (!source.isEmpty()) {
                    if (source.get().turnFaceUp()) {
                        this.score += 5;
                        this.wasTurned = true;
                    }
                }
            }
        }
    }

    public void undo() {
        if (this.wasTurned) {
            source.get().turnFaceDown();
        }
        source.returnPile(dest.pop(indentedCard));
    }

    public int getResult() {
        return this.score;
    }
}
