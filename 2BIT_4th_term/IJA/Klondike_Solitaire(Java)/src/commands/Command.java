package src.commands;

/**
 * Rozhranie reprezentujuce operacie nad prikazom.
 *
 * @author Jakub Handzus (xhandz00@stud.fit.vutbr.cz)
 * @author Martina Grzybowska (xgrzyb00@stud.fit.vutbr.cz)
 */
public interface Command {
    /**
     * Metoda na vykonanie prikazu.
     */
    void execute();

    /**
     * Metoda na vratenie zmien, ake boli pred prikazom.
     */
    void undo();

    /**
     * @return Vrati bodovy zisk prikazu. V pripade neuspesneho vykonania
     * vracia metoda -1.
     */
    int getResult();
}
