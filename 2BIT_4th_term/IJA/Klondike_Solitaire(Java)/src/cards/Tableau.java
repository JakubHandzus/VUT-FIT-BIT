package src.cards;

/**
 * Rozhranie reprezentujuce pracovny balicek kariet.
 *
 * @author Jakub Handzus (xhandz00@stud.fit.vutbr.cz)
 * @author Martina Grzybowska (xgrzyb00@stud.fit.vutbr.cz)
 */
public interface Tableau extends Waste {
    Tableau pop(Card card);
    boolean put(Tableau stack);
    Tableau getPile(Card card);
    void returnPile(Tableau stack);
    void origPut(Card card);
}
