package src.commands;

import java.util.ArrayList;
import java.util.List;

/**
 * Trieda sluziaca na invokaciu, riadenie, a ukladanie objektov reprezentujucich
 * zakladne prikazy riadiace hru.
 *
 * @author Jakub Handzus (xhandz00@stud.fit.vutbr.cz)
 * @author Martina Grzybowska (xgrzyb00@stud.fit.vutbr.cz)
 */

public class CommandInvoker {
    private List<Command> commands = new ArrayList<>();

    /**
     * Ulozi prikaz do zoznamu prikazov a vykona prikaz.
     * Pokial sa prikaz nevykonal (vysledok je -1), vymaze ho zo zoznamu.
     * @param cmd Prikaz na vykonanie.
     * @return Bodovy zisk prikazu.
     */
    public int execute(Command cmd) {
        commands.add(0, cmd);
        cmd.execute();
        if (cmd.getResult() == -1) {
            commands.remove(0);
            return 0;
        }
        else {
            return cmd.getResult();
        }
    }

    /**
     * Pokial sa nachadza nejaku prikaz v zozname prikazov, vykona nad poslednym
     * prikazom v zozname metodu undo() a vymaze ho zo zoznamu.
     * @return Opacny bodovy zisk ako pri vykonani prikazu.
     */
    public int undo() {
        if (commands.size() > 0) {
            Command cmd = commands.remove(0);
            cmd.undo();
            return -cmd.getResult();
        }
        return 0;
    }

    /**
     * @return Pocet prikazov v zozname.
     */
    public int getSize() {
        return commands.size();
    }

    /**
     * @return Zoznam prikazov.
     */
    public List<Command> getCommands() {
        return this.commands;
    }

    /**
     * Nahradi zoznam prikazov aktualneho zoznamu zadanym zoznamom.
     * @param commands Zoznam prikazov, ktory sa nahraje do aktualneho zoznamu.
     */
    public void setCommands(List<Command> commands) {
        this.commands = commands;
    }
}
