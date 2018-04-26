package src.cards;

/**
 * Rozhranie reprezentujuce kartu a jej farby.
 *
 * @author Jakub Handzus (xhandz00@stud.fit.vutbr.cz)
 * @author Martina Grzybowska (xgrzyb00@stud.fit.vutbr.cz)
 */
public interface Card {
    Card.Color color();
    int compareValue(Card c);
    boolean isTurnedFaceUp();
    boolean similarColorTo(Card c);
    boolean turnFaceUp();
    boolean turnFaceDown();
    int value();
    @Override
    boolean equals(Object o);
    @Override
    int hashCode();
    String toString();


    enum Color {

        CLUBS("C"),
        DIAMONDS("D"),
        HEARTS("H"),
        SPADES("S");

        private String charColor;

        /**
         * Nastavi farbu podla vstupneho parametra.
         * @param charColor Farba karty.
         */
        Color(String charColor) {
            this.charColor = charColor;
        }

        /**
         * Vrati pismeno reprezentujuce farbu.
         * @return Farba reprezentovana pismenom.
         */
        @Override
        public String toString() {
            return this.charColor;
        }
    }
}
