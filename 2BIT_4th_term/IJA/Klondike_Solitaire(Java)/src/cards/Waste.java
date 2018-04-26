package src.cards;

/**
 * Rozhranie reprezentujuce balicek kariet.
 *
 * @author Jakub Handzus (xhandz00@stud.fit.vutbr.cz)
 * @author Martina Grzybowska (xgrzyb00@stud.fit.vutbr.cz)
 */
public interface Waste {
    Card get();
    Card get(int index);
    boolean isEmpty();
    Card pop();
    boolean put(Card card);
    int size();
    void shuffle();
    String toString();
}
