package src.cards;

import java.io.Serializable;

/**
 * Trieda implementujuca kartu hry.
 *
 * @author Jakub Handzus (xhandz00@stud.fit.vutbr.cz)
 * @author Martina Grzybowska (xgrzyb00@stud.fit.vutbr.cz)
 */
public class KlondikeCard implements Card, Serializable {
    private Color color;
    private int value;
    private boolean faceUp;

    /**
     * KONSTRUKTOR
     * Vytvori kartu s danou farbou, hodnotou a informaciou o otoceni karty.
     * @param color Farba karty.
     * @param value Hodnota karty (rozsah od 1-13).
     * @param faceUp True ak je otocena krata licom, False ak je otocena rubom.
     */
    public KlondikeCard(Card.Color color, int value, boolean faceUp) {
        this.color = color;
        this.value = value;
        this.faceUp = faceUp;
    }

    /**
     * Vrati farbu karty.
     * @return Farba karty.
     */
    public Card.Color color() {
        return this.color;
    }

    /**
     * Porovna hodnoty karty so zadanou kartou.
     * @param card Karta, s ktorou porovnavame aktualnu kartu.
     * @return Vrati rozdiel hodnot kariet. Ak je aktualna karta vacsej hodnoty, je rozdiel kladny,
     * ak porovnavana karta vacsia, rozdiel je zaporny.
     */
    public int compareValue(Card card) {
        KlondikeCard tmpCard = (KlondikeCard) card;
        if (this.value == tmpCard.value()) {
            return 0;
        }
        else if (this.value > tmpCard.value()) {
            return (this.value - tmpCard.value());
        }
        else {
            return -(tmpCard.value() - this.value);
        }
    }

    /**
     * Zisti, ci je karta otocena licom nahor.
     * @return True ak je karta otocena licom, inak false.
     */
    public boolean isTurnedFaceUp() {
        return this.faceUp;
    }

    /**
     * Testuje, ci ma karta podobnu farbu (cervena/cierna) ako zadana karta.
     * @param card Karta na porovnanie.
     * @return Informacia o zhode farieb.
     */
    public boolean similarColorTo(Card card){
        KlondikeCard tmpCard = (KlondikeCard) card;
        if (tmpCard.color() == Color.CLUBS || tmpCard.color() == Color.SPADES) {
            return (this.color() == Color.CLUBS || this.color() == Color.SPADES);
        }
        else {
            return (this.color() == Color.DIAMONDS || this.color() == Color.HEARTS);
        }
    }

    /**
     * Otoci kartu licom nahor. Pokial uz je takto otocena, nespravi nic.
     * @return Informacia o tom, ci doslo k otoceniu karty.
     */
    public boolean turnFaceUp() {
        if (!isTurnedFaceUp()) {
            this.faceUp = true;
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Otoci kartu licom nadol. Pokial uz je takto otocena, nespravi nic.
     * @return Informacia o tom, ci doslo k otoceniu karty.
     */
    public boolean turnFaceDown() {
        if (isTurnedFaceUp()) {
            this.faceUp = false;
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * @return Hodnota karty.
     */
    public int value() {
        return this.value;
    }

    /**
     * Porovna, ci sa aktualna karta zhoduje so zadanou.
     * @param o Zadana karta.
     * @return Informacia o zhode.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KlondikeCard that = (KlondikeCard) o;

        if (value != that.value) {
            return false;
        }
        if (faceUp != that.faceUp) {
            return false;
        }
        return color == that.color;
    }

    /**
     * Vygeneruje jedinecnu ciselnu hodnotu karty
     * na zaklade vsetkych atributov.
     * @return Jedinecna ciselna hodnota karty.
     */
    @Override
    public int hashCode() {
        int result = color != null ? color.hashCode() : 0;
        result = 31 * result + value;
        result = 31 * result + (faceUp ? 1 : 0);
        return result;
    }

    /**
     * @return Retazec reprezentujuci hodnotu a farbu karty.
     */
    public String toString(){
        if (value > 1 && value < 11) {
            return this.value + "(" + this.color() + ")";
        }
        else if (value >= 11 && value <= 13 || value == 1) {
            String meno = "";
            switch (value) {
                case 1:
                    meno = "A";
                    break;
                case 11:
                    meno = "J";
                    break;
                case 12:
                    meno = "Q";
                    break;
                case 13:
                    meno = "K";
                    break;
            }

            return meno + "(" + this.color() + ")";
        }
        else {
            throw new RuntimeException("Karta nie je v rozsahu");
        }
    }
}
