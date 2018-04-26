package src.gui;
import src.board.BoardSet;
import src.cards.*;
import src.commands.*;
import src.game.GameLogic;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

/**
 * Trieda implementujuca funkcionalitu grafickeho uzivatelskeho
 * rozhrania pre jednu hru, vytvara a riadi vsetky jej zakladne objekty a udalosti.
 *
 * @author Jakub Handzus (xhandz00@stud.fit.vutbr.cz)
 * @author Martina Grzybowska (xgrzyb00@stud.fit.vutbr.cz)
 */
public class GameGui implements MouseListener {

    private GameLogic currentGame;
    private JPanel container;

    private CommandInvoker switcher;
    private Card cardToPick;
    private TableauPile tableauToPick;
    private WastePile wasteToPick;
    private boolean firstClick;

    private ArrayList<JLayeredPane> tableauPanel;
    private ArrayList<ArrayList<JLabel>> tableauLabel;
    private ArrayList<JLabel> foundationLabel;
    private JLabel wasteLabel;
    private JLabel stockLabel;
    private JPanel gameMenu;
    private ArrayList<JButton> gameMenuButtons;

    private JPanel gameArea;
    private JPanel tableauArea;
    private JPanel pilesArea;

    private int cardX;
    private int cardY;

    private File userDir;

    /**
     * KONSTRUKTOR
     * Vytvori invoker prikazov a naplni zakladne atributy.
     * @param currentGame Logika hry, ktora ma byt vykreslena.
     * @param container Panel, ktory sluzi ako hlavny kontainer hry.
     */
    public GameGui(GameLogic currentGame, JPanel container) {

        this.switcher = new CommandInvoker();
        this.currentGame = currentGame;
        this.container = container;
        String userDirLocation = System.getProperty("user.dir");
        userDir = new File(userDirLocation);
    }

    /**
     * Nastavi a vykresli hlavny kontainer hry, vybera rozmery podla momentalneho poctu hier.
     * @param gameNumber Logika hry, ktora ma byt vykreslena.
     */
    public void setContainer(int gameNumber) {

        pilesArea = new JPanel();
        pilesArea.setLayout(new BoxLayout(pilesArea, BoxLayout.LINE_AXIS));
        pilesArea.setBackground(null);

        if (gameNumber == 1) {

            this.cardX = 85;
            this.cardY = 125;

            createGameArea(1060, 650);
            createLocalMenu(13);
            pilesArea.add(createWasteStockArea(15, 16, 610, 90));
            createTableauArea(820, 610, 17, 22);
            pilesArea.add(tableauArea);
            pilesArea.add(setFoundationArea(90, 610, 15, 16));
        }
        else {
            this.cardX = 58;
            this.cardY = 90;

            createGameArea(750, 433);
            createLocalMenu(9);
            pilesArea.add(createWasteStockArea(10, 8, 407, 60));
            createTableauArea(547, 407, 10, 14);
            pilesArea.add(tableauArea);
            pilesArea.add(setFoundationArea(60, 407, 10, 8));
        }

        gameArea.add(pilesArea);
        container.add(gameArea);

        drawMeLikeOneOfYourFrenchGirls();
    }
    /**
     * Resetuje hlavny kontainer a vsetky zoznamy, s ktorymi pracuje
     */
    public void clearContainer() {

        tableauPanel.clear();
        tableauLabel.clear();
        foundationLabel.clear();
        tableauArea.removeAll();

        pilesArea.removeAll();
        gameArea.remove(pilesArea);
        container.remove(gameArea);

        container.validate();
        container.repaint();
    }

    /**
     * Vykresli labely reprezentujuce karty a meniace sa prvky hry iterovanim cez zoznamy jednotlivych
     * komponent kontaineru a balickov hernej logiky.
     */
    public void drawMeLikeOneOfYourFrenchGirls() {

        gameMenuButtons.get(5).setText("SCORE: " + currentGame.getScore());
        gameMenuButtons.get(6).setText("MOVES: " + switcher.getSize());

        stockLabel.setBackground(new Color(37, 76, 38));
        if (currentGame.getUpperCard(4) == null) {
            stockLabel.setIcon(new ImageIcon(new ImageIcon(GameGui.class.getResource("/emptysw.png")).getImage().getScaledInstance(cardX, cardY - 2, Image.SCALE_DEFAULT)));
        }
        else {
            stockLabel.setIcon(new ImageIcon(new ImageIcon(GameGui.class.getResource("/back.png")).getImage().getScaledInstance(cardX, cardY, Image.SCALE_DEFAULT)));
        }

        wasteLabel.setBackground(new Color(37, 76, 38));
        if (currentGame.getUpperCard(5) == null) {
            wasteLabel.setBackground(new Color(37, 76, 38));
            wasteLabel.setIcon(new ImageIcon(new ImageIcon(GameGui.class.getResource("/emptysw.png")).getImage().getScaledInstance(cardX, cardY - 2, Image.SCALE_DEFAULT)));
        }
        else {
            Card tmpCard = currentGame.getUpperCard(5);
            wasteLabel.setIcon(new ImageIcon(new ImageIcon(GameGui.class.getResource("/" + tmpCard.toString() + ".png")).getImage().getScaledInstance(cardX, cardY, Image.SCALE_DEFAULT)));
        }

        for (int index = 0; index < 4; index++) {
            foundationLabel.get(index).setBackground(new Color(37, 76, 38));
            if (currentGame.getUpperCard(index) == null) {
                String tmpCard = "";
                if (index == 0){
                    tmpCard = "foundC";
                }
                else if (index == 1) {
                    tmpCard = "foundD";
                }
                else if (index == 2) {
                    tmpCard = "foundH";
                }
                else if (index == 3) {
                    tmpCard = "foundS";
                }

                foundationLabel.get(index).setIcon(new ImageIcon(new ImageIcon(GameGui.class.getResource("/" + tmpCard + ".png")).getImage().getScaledInstance(cardX, cardY - 2, Image.SCALE_DEFAULT)));
            }
            else {
                Card tmpCard = currentGame.getUpperCard(index);
                foundationLabel.get(index).setIcon(new ImageIcon(new ImageIcon(GameGui.class.getResource("/" + tmpCard.toString() + ".png")).getImage().getScaledInstance(cardX, cardY, Image.SCALE_DEFAULT)));
            }
        }

        if (currentGame.isVictory()) {
            for (int index = 0; index <= 3; index++) {
                gameMenu.remove(gameMenuButtons.get(index));
            }

            for (int row = 0; row < 7; row++) {
                tableauArea.remove(tableauPanel.get(row));
            }

            wasteLabel.removeMouseListener(this);
            stockLabel.removeMouseListener(this);

            for (int row = 0; row < 4; row++) {
                foundationLabel.get(row).removeMouseListener(this);
            }

            JLabel victory = new JLabel(new ImageIcon(GameGui.class.getResource("/victory.png")));

            tableauArea.add(victory);

        }
        else {
            for (int column = 0; column < 7; column++) {
                for (int index = 0; index < 13 + column; index++) {

                    Card tmpCard = currentGame.getTableauPile(column).get(index);

                    if (tmpCard == null) {
                        tableauLabel.get(column).get(index).setBackground(new Color(46, 113, 64));
                        tableauLabel.get(column).get(index).setIcon(new ImageIcon(new ImageIcon(GameGui.class.getResource("/emptytab.png")).getImage().getScaledInstance(cardX, cardY - 2, Image.SCALE_DEFAULT)));

                        if (index == 0 && currentGame.getTableauPile(column).isEmpty()) {
                            tableauLabel.get(column).get(index).setVisible(true);
                        }
                        else {
                            tableauLabel.get(column).get(index).setVisible(false);
                        }
                    }
                    else if (tmpCard.isTurnedFaceUp()) {
                        tableauLabel.get(column).get(index).setVisible(true);
                        tableauLabel.get(column).get(index).setIcon(new ImageIcon(new ImageIcon(GameGui.class.getResource("/" + tmpCard.toString() + ".png")).getImage().getScaledInstance(cardX, cardY - 2, Image.SCALE_DEFAULT)));
                    }
                    else {
                        tableauLabel.get(column).get(index).setVisible(true);
                        tableauLabel.get(column).get(index).setIcon(new ImageIcon(new ImageIcon(GameGui.class.getResource("/back.png")).getImage().getScaledInstance(cardX, cardY, Image.SCALE_DEFAULT)));
                    }
                }
            }
        }

        this.container.validate();
        this.container.repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {

        if (e.getComponent().getParent() == gameMenu) {

            if (e.getComponent() == gameMenuButtons.get(0)) {

                final JFileChooser fc = new JFileChooser(userDir + "/examples");
                int returnVal = fc.showSaveDialog(container);

                if (returnVal == JFileChooser.APPROVE_OPTION) {

                    File file = fc.getSelectedFile();

                    try {
                        currentGame.saveCommands(switcher.getCommands());
                        BoardSet.save(currentGame, file.getPath());
                    }
                    catch (Exception e1) {
                        JOptionPane.showMessageDialog(container, "Unable to save", "File error", JOptionPane.ERROR_MESSAGE);
                    }
                }

            }
            else if (e.getComponent() == this.gameMenuButtons.get(1)) {
                currentGame.editScore(switcher.undo());
            }
            else if (e.getComponent() == gameMenuButtons.get(2)) {
                JOptionPane.showMessageDialog(container, hint(), "Hint", JOptionPane.PLAIN_MESSAGE);

            }
            else if (e.getComponent() == gameMenuButtons.get(3)) {
                while (switcher.getSize() != 0) {
                    currentGame.editScore(switcher.undo());
                }
            }

            drawMeLikeOneOfYourFrenchGirls();
            return;

        }
        else if (e.getComponent().getParent().getParent() == tableauArea) {
            for (int column = 0; column < 7; column++) {
                if (e.getComponent().getParent() == tableauPanel.get(column)) {
                    if (e.getComponent() != tableauLabel.get(column).get(0)) {
                        e.getComponent().setSize(new Dimension(cardX + 10,cardY + 10));
                    }
                    if (!firstClick) {
                        tableauToPick = matchTableauIndex(column);
                        for (int index = 0; index < tableauLabel.get(column).size(); index++) {
                            if (e.getComponent() == tableauLabel.get(column).get(index)) {
                                cardToPick = tableauToPick.get(index);
                                if (cardToPick == null) {
                                    cardToPick = tableauToPick.get();
                                }
                            }
                        }
                        firstClick = true;
                        return;
                    }
                    else {
                        if (tableauToPick != null) {
                            Command TtoT = new TableauToTableau(tableauToPick, matchTableauIndex(column), cardToPick);
                            handleCommand(TtoT);
                            return;
                        }
                        else if (this.wasteToPick != null) {
                            Command WtoT = new WasteToTableau(wasteToPick, matchTableauIndex(column));
                            handleCommand(WtoT);
                            return;
                        }
                    }
                    break;
                }
            }
        }
        else if (e.getComponent() == stockLabel) {
            Command StoW = new StockToWaste(currentGame.getStockPile(), currentGame.getWastePile());
            handleCommand(StoW);
            return;
        }
        else if (e.getComponent() == wasteLabel) {
            if (!firstClick) {
                wasteToPick = currentGame.getWastePile();
                firstClick = true;
                drawMeLikeOneOfYourFrenchGirls();
                return;
            } else {
                drawMeLikeOneOfYourFrenchGirls();
                clearAttributes();
                return;
            }
        }
        else {
            for (int index = 0; index < 4; index++) {
                if (e.getComponent() == foundationLabel.get(index)) {
                    if (!firstClick) {
                        drawMeLikeOneOfYourFrenchGirls();
                        clearAttributes();
                        return;
                    } else if (tableauToPick != null) {
                        Command TtoF = new TableauToFoundation(tableauToPick, matchFoundationIndex(index), cardToPick);
                        handleCommand(TtoF);
                        return;

                    } else if (wasteToPick != null) {
                        Command WtoF = new WasteToFoundation(wasteToPick, matchFoundationIndex(index));
                        handleCommand(WtoF);
                        return;
                    }
                }
            }

        }

        drawMeLikeOneOfYourFrenchGirls();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getComponent() instanceof JLabel) {
            e.getComponent().setSize(new Dimension(cardX, cardY));
        }
    }
    /**
     * Napoveda s aktualnymi moznymi tahmi, Medota prehladava mozne tahy najprv
     * z vrcholu pracujucich balickov do cielovych, potom z odkladacieho balicka
     * na cielovy a nakoniec medzi vsetky mozne tahy medzi pracujucimi balickami kariet.
     * Pokial nenajde ziaden tah, pozrie sa, ci sa da pretocit karata z hlavneho balicka kariet.
     * @return Retazec moznych tahov, ktore su oddelene novymi riadkami.
     */
    private String hint() {

        String result = "";
        for (int column = 0; column < 7; column++) {
            for (int fPile = 0; fPile < 4; fPile++) {
                if (currentGame.getTableauPile(column).get() != null) {
                    Command TtoF = new TableauToFoundation(currentGame.getTableauPile(column), currentGame.getFoundationPile(fPile), currentGame.getTableauPile(column).get());
                    switcher.execute(TtoF);
                    if (TtoF.getResult() != -1) {
                        switcher.undo();
                        result += "From tableau " + (column + 1) + ": " + currentGame.getTableauPile(column).get().toString() + " to foundation (score gain: " + TtoF.getResult() + ")\n";
                    }
                }
            }
        }

        if (currentGame.getUpperCard(5) != null) {
            for (int fPile = 0; fPile < 4; fPile++) {
                Command WtoF = new WasteToFoundation(currentGame.getWastePile(), currentGame.getFoundationPile(fPile));
                switcher.execute(WtoF);
                if (WtoF.getResult() != -1) {
                    switcher.undo();
                    result += "From waste: " + currentGame.getUpperCard(5).toString() + " to foundation (score gain: " + WtoF.getResult() + ")\n";
                }
            }
        }

        if (currentGame.getUpperCard(5) != null) {
            for (int column = 0; column < 7; column++) {
                Command WtoT = new WasteToTableau(currentGame.getWastePile(), currentGame.getTableauPile(column));
                switcher.execute(WtoT);
                if (WtoT.getResult() != -1) {
                    switcher.undo();
                    result += "From waste: " + currentGame.getUpperCard(5).toString() + " to tableau: " + (column + 1) + " (score gain: " + WtoT.getResult() + ")\n";
                }
            }
        }

        for (int fromColumn = 0; fromColumn < 7; fromColumn++) {
            for (int index = 0; index < currentGame.getTableauPile(fromColumn).size(); index++) {
                if (currentGame.getTableauPile(fromColumn).get() != null) {
                    for (int toColumn = 0; toColumn < 7; toColumn++) {
                        Command TtoT = new TableauToTableau(currentGame.getTableauPile(fromColumn), currentGame.getTableauPile(toColumn), currentGame.getTableauPile(fromColumn).get(index));
                        switcher.execute(TtoT);
                        if (TtoT.getResult() != -1) {
                            switcher.undo();
                            result += "From tableau " + (fromColumn + 1) + ": " + currentGame.getTableauPile(fromColumn).get(index).toString() + " to tableau: " + (toColumn + 1) + " (score gain: " + TtoT.getResult() + ")\n";
                        }
                    }
                }
            }
        }

        if (result == "" && (!currentGame.getStockPile().isEmpty() || !currentGame.getWastePile().isEmpty())) {
            result += "Click on stock pile";
        }
        if (result == "") {
            result += "No moves available";
        }

        return result;
    }

    /**
     * Vytvori obalovaci kontainer hry
     * @param width Sirka hernej plochy.
     * @param height Vyska hernej plochy.
     */
    public void createGameArea(int width, int height) {

        gameArea = new JPanel();
        gameArea.setOpaque(true);
        gameArea.setLayout(new BoxLayout(gameArea, BoxLayout.PAGE_AXIS));
        gameArea.setBackground(new Color(49, 53, 51));
        gameArea.setMaximumSize(new Dimension(width, height));
    }

    /**
     * Vytvori lokalne menu hry, naplni zoznam tlacidiel menu
     * @param height Vyska nemenitelnej oddelovacej plochy.
     */
    public void createLocalMenu(int height) {

        gameMenu = new JPanel();
        gameMenu.setLayout(new BoxLayout(gameMenu, BoxLayout.LINE_AXIS));
        gameMenu.setBackground(null);
        gameMenuButtons = new ArrayList<>();

        String buttonName = "";
        for (int button = 0; button < 7; button++) {

            switch (button) {
                case 0:
                    buttonName = "SAVE";
                    break;
                case 1:
                    buttonName = "UNDO";
                    break;
                case 2:
                    buttonName = "HINT";
                    break;
                case 3:
                    buttonName = "RESET";
                    break;
                case 4:
                    buttonName = "QUIT";
                    break;
                case 5:
                    buttonName = "SCORE: " + currentGame.getScore();
                    break;
                case 6:
                    buttonName = "MOVES: " + switcher.getSize();
                    break;
            }

            gameMenuButtons.add(new JButton(buttonName));
            gameMenuButtons.get(button).setFont(gameMenuButtons.get(button).getFont().deriveFont(12.0f));

            if (button == 5 || button == 6) {
                gameMenuButtons.get(button).setOpaque(false);
                gameMenuButtons.get(button).setContentAreaFilled(false);
                gameMenuButtons.get(button).setBorderPainted(false);
                gameMenuButtons.get(button).setForeground(Color.white);
            }
            else if (button != 4) {
                gameMenuButtons.get(button).addMouseListener(this);
            }

            gameMenu.add(gameMenuButtons.get(button));
        }

        gameArea.add(Box.createRigidArea(new Dimension(0, height)));
        gameArea.add(gameMenu);
        gameArea.add(Box.createRigidArea(new Dimension(0, height)));
    }
    /**
     * Vytvori lavy obsahovy panel, hlavny balicek a odkladaci balicek
     * @param raWidth Sirka nemenitelnej oddelovacej plochy.
     * @param raHeight Vyska nemenitelnej oddelovacej plochy.
     * @param height Vyska celeho laveho obsahoveho panela.
     * @param width Sirka celeho laveho obsahoveho panela.
     * @return Panel obsahujuci vsetky potrebne komponenty
     */
    public JPanel createWasteStockArea(int raWidth, int raHeight, int height, int width) {

        JPanel leftsidecon = new JPanel();
        leftsidecon.setLayout(new BoxLayout(leftsidecon, BoxLayout.LINE_AXIS));
        leftsidecon.add(Box.createRigidArea(new Dimension(raWidth, height)));

        JPanel leftside = new JPanel();
        leftside.setLayout(new BoxLayout(leftside, BoxLayout.PAGE_AXIS));
        leftside.setMaximumSize(new Dimension(width, height));
        leftside.setBackground(new Color(37, 76, 38));
        leftsidecon.setBackground(new Color(37, 76, 38));
        stockLabel = new JLabel();
        stockLabel.setBackground(new Color(46, 113, 64));
        stockLabel.setOpaque(true);
        stockLabel.setMaximumSize(new Dimension(cardX, cardY));
        stockLabel.addMouseListener(this);

        wasteLabel = new JLabel();
        wasteLabel.setBackground(new Color(46, 113, 64));
        wasteLabel.setOpaque(true);
        wasteLabel.setMaximumSize(new Dimension(cardX, cardY));
        wasteLabel.addMouseListener(this);

        leftside.add(Box.createRigidArea(new Dimension(0, 20)));
        leftside.add(stockLabel);
        leftside.add(Box.createRigidArea(new Dimension(0, raHeight)));
        leftside.add(wasteLabel);

        leftsidecon.add(leftside);
        leftsidecon.add(Box.createRigidArea(new Dimension(raWidth, height)));

        return leftsidecon;
    }
    /**
     * Vytvori stredny panel, obsahujuci panely s lablami.
     * Reprezentuje oblast pracovnych balickov.
     * @param width Sirka celeho laveho obsahoveho panela.
     * @param height Vyska celeho laveho obsahoveho panela.
     * @param cWidth Sirka nemenitelnej oddelovacej plochy.
     * @param cHeight Vyska nemenitelnej oddelovacej plochy.
     */
    public void createTableauArea(int width, int height, int cWidth, int cHeight) {

        tableauArea = new JPanel();
        tableauArea.setOpaque(true);
        tableauArea.setLayout(new GridLayout(1, 7, 0, 0));
        tableauArea.setMaximumSize(new Dimension(width, height));
        tableauArea.setBackground(new Color(46, 113, 64));

        tableauPanel = new ArrayList<>();
        tableauLabel = new ArrayList<ArrayList<JLabel>>();

        for (int column = 0; column < 7; column++) {
            tableauPanel.add(new JLayeredPane());
            tableauPanel.get(column).setBackground(null);
            tableauPanel.get(column).setOpaque(true);

            for (int index = 0; index < 13 + column; index++) {
                tableauLabel.add(new ArrayList<JLabel>());
                tableauLabel.get(column).add(new JLabel());
                tableauLabel.get(column).get(index).setBackground(null);
                tableauLabel.get(column).get(index).setBounds(cWidth, (index * cHeight) + 20, cardX, cardY);
                tableauLabel.get(column).get(index).addMouseListener(this);
                tableauPanel.get(column).add(tableauLabel.get(column).get(index), new Integer(index));
            }
            tableauArea.add(tableauPanel.get(column));
        }
    }
    /**
     * Vytvori pravy panel obsahujuci cielove balicky
     * @param width Sirka celeho praveho obsahoveho panela.
     * @param height Vyska celeho praveho obsahoveho panela.
     * @param raWidth Sirka nemenitelnej oddelovacej plochy.
     * @param raHeight Vyska nemenitelnej oddelovacej plochy.
     * @return Panel obsahujuci vsetky potrebne komponenty
     */
    public JPanel setFoundationArea(int width, int height, int raWidth, int raHeight) {

        JPanel foundContainer = new JPanel();
        foundContainer.setLayout(new BoxLayout(foundContainer, BoxLayout.LINE_AXIS));
        foundContainer.add(Box.createRigidArea(new Dimension(raWidth + 2, height)));

        JPanel foundationArea = new JPanel();
        foundationArea.setOpaque(true);
        foundationArea.setLayout(new BoxLayout(foundationArea, BoxLayout.PAGE_AXIS));
        foundationArea.setBackground(new Color(37, 76, 38));
        foundationArea.setMaximumSize(new Dimension(width, height));

        foundationLabel = new ArrayList<JLabel>();
        foundationArea.add(Box.createRigidArea(new Dimension(0, 20)));

        for (int row = 0; row < 4; row++) {
            foundationLabel.add(new JLabel());
            foundationLabel.get(row).setBackground(new Color(46, 113, 64));
            foundationLabel.get(row).setOpaque(true);
            foundationLabel.get(row).setMaximumSize(new Dimension(cardX, cardY));
            foundationLabel.get(row).addMouseListener(this);
            foundationArea.add(foundationLabel.get(row));
            foundationArea.add(Box.createRigidArea(new Dimension(0, raHeight)));
        }

        foundContainer.add(foundationArea);
        foundContainer.setBackground(new Color(37, 76, 38));
        foundContainer.add(Box.createRigidArea(new Dimension(raWidth - 2, height)));

        return foundContainer;
    }

    /**
     * Spoji pracovny panel s pracovnym balickom na danom indexe.
     * @param index Index v zozname pracovnych balickov.
     * @return Balicek z daneho indexu.
     */
    public TableauPile matchTableauIndex(int index) {
        return currentGame.getTableauPile(index);

    }

    /**
     * Spoji cielovy panel s cielovym balickom na danom indexe.
     * @param index Index v zozname cielovych balickov.
     * @return Balicek z daneho indexu.
     */
    public FoundationPile matchFoundationIndex(int index) {
        return currentGame.getFoundationPile(index);
    }

    /**
     * Preda objekt reprezentujuci tlacidlo quit
     * @return Objekt reprezentujuci tlacidlo quit
     */
    public JButton delegateCancelButton() {
        return this.gameMenuButtons.get(4);
    }

    /**
     * Vycisti atributy objektu
     */
    private void clearAttributes() {
        firstClick = false;
        tableauToPick = null;
        cardToPick = null;
        wasteToPick = null;
    }

    /**
     * Vykona prikaz a podla navratovej hodnoty upravi skore hry.
     * Vykresli platno, a zavola medotu clearAttributes()
     * @param cmd Zadany prikaz na vykonanie
     */
    private void handleCommand(Command cmd) {
        currentGame.editScore(switcher.execute(cmd));
        drawMeLikeOneOfYourFrenchGirls();
        clearAttributes();
    }

    /**
     * Nastavi zoznam prikazov zo vsupneho parametra do CommandInvokera.
     * Vyuziva sa pri nacitavani hry zo subora.
     * @param commands Zoznam prikazov.
     */
    public void setCommands(java.util.List<Command> commands) {
        switcher.setCommands(commands);
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }
}
