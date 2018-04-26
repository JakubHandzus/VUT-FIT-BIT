package src.cards;

/**
 * Trieda implementujuca cielovy balicek, ktora rozsiruje
 * triedu WastePile.
 *
 * @author Jakub Handzus (xhandz00@stud.fit.vutbr.cz)
 * @author Martina Grzybowska (xgrzyb00@stud.fit.vutbr.cz)
 */
public class FoundationPile extends WastePile {

    private Card.Color color;

    /**
     * KONSTRUKTOR
     * Nastavi farbu cieloveho balicka.
     * @param color Farba cieloveho balicka.
     */
    public FoundationPile(Card.Color color) {
        this.color = color;
    }

    /**
     * Polozi na vrch cieloveho balicka kartu, ktora je rovnakej
     * farby ako typ balicku a jej hodnota je o jedna vacsia, ako aktualna karta na vrchu.
     * @param card Karta, ktora sa prida na vrch balicka.
     * @return Uspesnost operacie.
     */
    @Override
    public boolean put(Card card) {
        if (card.color() == this.color && this.size() + 1 == card.value()) {
            this.pile.push(card);
            return true;
        }
        else {
            return false;
        }
    }
}
