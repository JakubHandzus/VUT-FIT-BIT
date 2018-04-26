package src.cards;

import java.io.Serializable;
import java.util.Collections;
import java.util.Stack;

/**
 * Trieda implementujuca pracovny balicek kariet.
 * Rozsiruje triedu balicku kariet.
 *
 * @author Jakub Handzus (xhandz00@stud.fit.vutbr.cz)
 * @author Martina Grzybowska (xgrzyb00@stud.fit.vutbr.cz)
 */
public class TableauPile extends WastePile implements Tableau, Serializable {

    /**
     * Metoda odoberie zo zasobniku sekvenciu kariet od zadanej karty
     * az po vrchol zasobnika. Pokial je hladana karta na vrchole,
     * bude v sekvencii iba jedna karta.
     * @param card Hladana karta.
     * @return Balicek kariet obsahujuci odobranu sekvenciu. Pokial hladana
     * karta v zasobniku nieje, vrati null.
     */
    @Override
    public Tableau pop(Card card) {
        Stack<Card> tmpPile = new Stack();
        boolean isFound = false;

        while (!this.isEmpty()) {
            Card tmpCard = this.pile.pop();
            tmpPile.push(tmpCard);

            if (card.equals(tmpCard)) {
                isFound = true;
                break;
            }
        }
        Collections.reverse(tmpPile);

        if (isFound) {
            TableauPile tmpPack = new TableauPile();
            tmpPack.pile.addAll(tmpPile);
            return tmpPack;
        }
        else {
            this.pile = tmpPile;
            return null;
        }
    }

    /**
     * Vlozi karty zo balicka "pile" na vrchol aktualneho pracovneho balicka.
     * Karty vklada v rovnakom poradi, ako su uvedene v balicku "pile".
     * @param pile Balicek vkladanych kariet.
     * @return Uspesnost akcie.
     */
    @Override
    public boolean put(Tableau pile) {
        TableauPile tmpPile = (TableauPile) pile;

        if (tmpPile != null && !tmpPile.isEmpty()) {
            if (this.isEmpty()) {
                if (tmpPile.get(0).value() == 13) {
                    this.pile.addAll(tmpPile.pile);
                    return true;
                }
            }
            else if (this.get().compareValue(tmpPile.get(0)) == 1) {
                if (!this.get().similarColorTo(tmpPile.get(0))) {
                    this.pile.addAll(tmpPile.pile);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Vkladana karta sa vlozi na vrchol aktualneho pracovneho balicka.
     * @param card Vkladana karta.
     * @return Uspesnost akcie.
     */
    @Override
    public boolean put(Card card) {

        if (card != null) {
            if (this.isEmpty()) {
                if (card.value() == 13) {
                    super.put(card);
                    return true;
                }
            } else if (this.get().compareValue(card) == 1) {
                if (!this.get().similarColorTo(card)) {
                    super.put(card);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Podobna ako funkcia Tableau pop(Card), s rodielom ze sa nemeni aktualny
     * pracovny balicek kariet.
     * @param card Hladana karta.
     * @return Vrati novy pracovny balicek kariet, ktory obsahuje sekvenciu kariet,
     * az po hladanu kartu.
     */
    @Override
    public Tableau getPile(Card card) {

        if (!this.isEmpty()) {
            TableauPile tmpPile = new TableauPile();
            for (int index = this.size() - 1; index >= 0; index--) {
                tmpPile.origPut(this.get(index));
                if (this.get(index).equals(card)) {
                    Collections.reverse(tmpPile.pile);
                    return tmpPile;
                }
            }
        }
        return null;
    }

    /**
     * Prida balicek do aktualneho pracovneho balicka.
     * @param stack Pridavany balicek.
     */
    @Override
    public void returnPile(Tableau stack) {
        TableauPile tmpPile = (TableauPile) stack;
        this.pile.addAll(tmpPile.pile);
    }

    /**
     * Vklada kartu na vrchol aktualneho balicka. Nepozera sa na podmienky vkladania.
     * @param card Vkladana karta.
     */
    public void origPut(Card card) {
        super.put(card);
    }
}
