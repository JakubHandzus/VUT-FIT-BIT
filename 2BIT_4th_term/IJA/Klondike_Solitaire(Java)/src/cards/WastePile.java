package src.cards;

import java.io.Serializable;
import java.util.Collections;
import java.util.Stack;

/**
 * Trieda implementujuca balicek kariet.
 *
 * @author Jakub Handzus (xhandz00@stud.fit.vutbr.cz)
 * @author Martina Grzybowska (xgrzyb00@stud.fit.vutbr.cz)
 */
public class WastePile implements Waste, Serializable {
    protected Stack<Card> pile;

    /**
     * KONSTRUKTOR
     * Vytvori atribut (zasobnik kariet)
     */
    public WastePile() {
        this.pile = new Stack();
    }

    /**
     * Vrati kartu z vrcholu zasobnika (karta nadalej zostava na zasobniku).
     * Pokial je balicek prazdny, vracia null.
     * @return Katra z vrcholu zasobnika.
     */
    @Override
    public Card get() {
        if (!this.isEmpty()) {
            return this.pile.peek();
        }
        else {
            return null;
        }
    }

    /**
     * Vrati kartu na uvedenom indexe. Spodna karta je na indexe 0,
     * a vrchol je na indexe size()-1. Pokial je balicek prazny,
     * alebo index mimo rozsah, vracia null.
     * @param index Pozicia karty v balicku.
     * @return Karta na danom indexe.
     */
    @Override
    public Card get(int index) {
        if (!this.isEmpty()) {
            if (index < this.size() && index >= 0) {
                return this.pile.get(index);
            }
        }
        return null;
    }

    /**
     * Test, ci je balicek kariet prazdny.
     * @return True ak je balicek prazny.
     */
    @Override
    public boolean isEmpty() {
        return (this.pile.isEmpty());
    }

    /**
     * Odoberie kartu z vrcholu balicku, Ak je prazdny, vraci null.
     * @return Karta z vrcholu balicka.
     */
    @Override
    public Card pop() {
        if (!this.isEmpty()) {
            return this.pile.pop();
        }
        else {
            return null;
        }
    }

    /**
     * Vlozi kartu na vrchol balicka.
     * @param card Vkladana karta.
     * @return Uspesnost akcie.
     */
    @Override
    public boolean put(Card card) {
        this.pile.push(card);
        return true;
    }

    /**
     * @return Aktualny pocet kariet v balicku.
     */
    @Override
    public int size() {
        return this.pile.size();
    }

    /**
     * Zamiesa balicek kariet.
     */
    @Override
    public void shuffle() {
        Collections.shuffle(this.pile);
    }

    /**
     * @return Retazec reprezentujuci obsah kariet v balicku.
     */
    public String toString() {
        String tmpStr = "";
        for (int i = 0; i < this.size(); i++) {
            tmpStr += this.get(i).toString() + "\n";
        }
        return tmpStr;
    }
}
