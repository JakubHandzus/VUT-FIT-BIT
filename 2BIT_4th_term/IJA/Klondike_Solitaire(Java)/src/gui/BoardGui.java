package src.gui;


import src.board.BoardSet;
import src.game.GameLogic;

import java.awt.*;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

/**
 * Trieda implementujuca funkcionalitu hlavneho grafickeho uzivatelskeho
 * rozhrania, vytvara a riadi vsetky zakladne objekty, pracuje s
 * triedou implementujucou rozhranie jednej hry.
 *
 * @author Jakub Handzus (xhandz00@stud.fit.vutbr.cz)
 * @author Martina Grzybowska (xgrzyb00@stud.fit.vutbr.cz)
 */
public class BoardGui implements MouseListener {

    private JFrame mainFrame;
    private JButton newGameButton;
    private JButton loadGameButton;
    private JPanel gamesContent;

    private int gameNumber;
    private JPanel[] gameSlots;
    private GameLogic[] gameEngines;
    private GameGui[] gameGuis;
    private JButton[] quitButtons;

    private JLabel[] emptyPics;
    private JLabel[] notePics;

    private File userDir;

    /**
     * KONSTRUKTOR
     * Inicializuje polia hier, slotov pre hry a hernych rozhrani,
     * vytvori zakladny ram obsahujuci vsetky objekty.
     */
    public BoardGui() {

        this.gameNumber = 0;
        this.gameEngines = new GameLogic[5];
        this.quitButtons = new JButton[5];
        this.gameSlots = new JPanel[5];
        this.gameGuis = new GameGui[5];
        this.notePics = new JLabel[2];
        this.emptyPics = new JLabel[5];
        String userDirLocation = System.getProperty("user.dir");
        userDir = new File(userDirLocation);

        this.mainFrame = new JFrame("Klondike Solitaire");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setPreferredSize(new Dimension(1060,700));

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.PAGE_AXIS));
        mainContainer.setBackground(new Color(37, 76, 38));

        JPanel mainMenu = new JPanel();
        mainMenu.setOpaque(true);
        mainMenu.setMaximumSize(new Dimension(1600,20));
        mainMenu.setLayout(new BoxLayout(mainMenu, BoxLayout.LINE_AXIS));
        mainMenu.add(Box.createHorizontalGlue());

        this.newGameButton = new JButton("NEW GAME");
        newGameButton.addMouseListener(this);
        newGameButton.setFont(this.newGameButton.getFont().deriveFont(12.0f));

        this.loadGameButton = new JButton("LOAD GAME");
        loadGameButton.setFont(this.loadGameButton.getFont().deriveFont(12.0f));
        loadGameButton.addMouseListener(this);

        mainMenu.add(this.newGameButton);
        mainMenu.add(this.loadGameButton);
        mainMenu.add(Box.createHorizontalGlue());

        this.gamesContent = new JPanel(new GridLayout(1,1,0,0));
        gamesContent.setBackground(new Color(46, 113, 64));

         this.notePics[0] = new JLabel(new ImageIcon(BoardGui.class.getResource("/welcome.png")));
        gamesContent.add(notePics[0]);

        for (int index = 1; index <= 4; index++) {
            emptyPics[index] = new JLabel(new ImageIcon(BoardGui.class.getResource("/emptyslot.png")));
        }

        mainContainer.add(mainMenu);
        mainContainer.add(gamesContent);
        mainFrame.add(mainContainer);

        mainFrame.setResizable(false);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }
    /**
     * Nacita vybranu hru, vytvori pre nu rozhranie a naplni
     * polia hier, rozhrani, slotov a tlacidiel na prisluchajucich indexoch.
     * @param fileName Hra, ktora ma byt nacitana.
     */
    public void setLoadGame(String fileName) {

        if (gameNumber < 4) {

            try {

                int firstNull = 0;
                for (int index = 1; index <= 4; index++) {
                    if (gameEngines[index] == null) {
                        firstNull = index;
                        break;
                    }
                }

                gameEngines[firstNull] = (BoardSet.load(fileName));

                if (gameNumber == 0) {
                    gamesContent.remove(notePics[0]);
                    gamesContent.setBackground(new Color(49, 53, 51));
                }

                gameNumber++;

                if (gameNumber == 2) { //druha hra
                    changeBoardToMultiple();
                    gameSlots[firstNull].remove(emptyPics[firstNull]);
                }
                else if (this.gameNumber == 1){
                    gameSlots[firstNull] = new JPanel();
                    gameSlots[firstNull].setOpaque(true);
                    gameSlots[firstNull].setLayout(new GridLayout(1, 1, 0, 0));
                    gamesContent.add(gameSlots[firstNull]);
                }
                else {
                    gameSlots[firstNull].remove(emptyPics[firstNull]);
                }

                gameGuis[firstNull] = new GameGui(gameEngines[firstNull], gameSlots[firstNull]);
                gameGuis[firstNull].setCommands(gameEngines[firstNull].loadCommands());
                gameGuis[firstNull].setContainer(gameNumber);


                quitButtons[firstNull] = gameGuis[firstNull].delegateCancelButton();
                quitButtons[firstNull].addMouseListener(this);

                mainFrame.validate();
                mainFrame.repaint();

            }

            catch (Exception e) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Unable to load",
                        "File error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        mainFrame.pack();
    }
    /**
     * Vytvori novu hru, jej rozhranie a naplni
     * polia hier, rozhrani, slotov a tlacidiel na prisluchajucich indexoch.
     */
    public void setNewGame() {

        if (gameNumber == 0) {
            gamesContent.remove(notePics[0]);
            gamesContent.setBackground(new Color(49, 53, 51));
        }

        if (gameNumber < 4) {

            gameNumber++;

            int firstNull = 0;
            for (int index = 1; index <= 4; index++) {
                if (gameEngines[index] == null) {
                    firstNull = index;
                    break;
                }
            }

            if (gameNumber == 2) {
                changeBoardToMultiple();
                gameSlots[firstNull].remove(emptyPics[firstNull]);
            }
            else if (gameNumber == 1) {
                gameSlots[firstNull] = new JPanel();
                gameSlots[firstNull].setOpaque(true);
                gameSlots[firstNull].setLayout(new GridLayout(1, 1, 0, 0));
                gamesContent.add(gameSlots[firstNull]);
            }
            else {
                gameSlots[firstNull].remove(emptyPics[firstNull]);
            }

            gameEngines[firstNull] = new GameLogic();

            gameGuis[firstNull] = new GameGui(gameEngines[firstNull], gameSlots[firstNull]);
            gameGuis[firstNull].setContainer(gameNumber);


            quitButtons[firstNull] = gameGuis[firstNull].delegateCancelButton();
            quitButtons[firstNull].addMouseListener(this);

            mainFrame.validate();
            mainFrame.repaint();
        }

        mainFrame.pack();
    }
    /**
     * Pozmeni rozmery hlavneho rozhrania tak, aby ponalo styri
     * rozhrania jednotlivych hier naraz, vytvori nove panely
     * pre nenaplnene sloty hier a prekresli zakladny ram.
     */
    public void changeBoardToMultiple() {

        int notNull = 0;
        for (int index = 1; index <= 4 ; index++) {
            if (gameEngines[index] != null) {
                notNull = index;
                break;
            }
        }

        gamesContent.setLayout(new GridLayout(2,2,2,2));
        mainFrame.setPreferredSize(new Dimension(1600,960));

        gameGuis[notNull].clearContainer();
        gameGuis[notNull].setContainer(this.gameNumber);
        quitButtons[notNull] = gameGuis[notNull].delegateCancelButton();
        quitButtons[notNull].addMouseListener(this);

        for (int index = 1; index <= 4; index++) {
            if (gameSlots[index] == null) {
                gameSlots[index] = new JPanel();
                gameSlots[index].setBackground(new Color(49, 53, 51));
                gameSlots[index].add(emptyPics[index]);
                gameSlots[index].setOpaque(true);
                gameSlots[index].setLayout(new GridLayout(1,1,0,0));
                gamesContent.add(this.gameSlots[index]);
            }
        }

        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
    }
    /**
     * Zmensi rozmery hlavneho rozhrania tak, aby obsahovalo rozhranie len pre
     * jednu hru, zmaze prazdne sloty a prekresli hlavny ram.
     */
    public void changeBoardToOne() {

        gamesContent.setLayout(new GridLayout(1,1,0,0));
        mainFrame.setPreferredSize(new Dimension(1060,700));

        for (int index = 1; index <= 4; index++) {

            if (gameEngines[index] != null) {
                gameGuis[index].clearContainer();
                gameGuis[index].setContainer(this.gameNumber);
                quitButtons[index] = gameGuis[index].delegateCancelButton();
                quitButtons[index].addMouseListener(this);
            }
            else {
                gamesContent.remove(this.gameSlots[index]);
                gameSlots[index] = null;
                quitButtons[index] = null;
            }
        }

        mainFrame.validate();
        mainFrame.repaint();
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        if (e.getComponent() == loadGameButton) {

            final JFileChooser fc = new JFileChooser(userDir + "/examples");
            fc.setDialogTitle("Load");
            int returnVal = fc.showOpenDialog(mainFrame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                setLoadGame(file.getPath());
            } 
            return;
        }
        else if (e.getComponent() == newGameButton) {
            setNewGame();
            return;
        }
        else {
            for(int index = 1; index <= 4; index++) {
                if (e.getComponent() == quitButtons[index]) {

                    gameNumber--;
                    gamesContent.remove(gameSlots[index]);
                    quitButtons[index] = null;
                    gameEngines[index] = null;
                    gameSlots[index] = null;
                    gameGuis[index] = null;

                    mainFrame.validate();
                    mainFrame.repaint();

                    if (gameNumber == 0) {
                        gamesContent.add(notePics[0]);
                        gamesContent.setBackground(new Color(46, 113, 64));
                        return;
                    }

                    gameSlots[index] = new JPanel();
                    gameSlots[index].setBackground(new Color(49, 53, 51));
                    gameSlots[index].setLayout(new GridLayout(1,1,0,0));
                    gameSlots[index].add(emptyPics[index]);
                    gamesContent.add(gameSlots[index]);

                    if (gameNumber == 1) {
                        changeBoardToOne();
                    }

                    mainFrame.validate();
                    mainFrame.repaint();

                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
    /**
     * Metoda main vytvara graficke uzivatelske rozhranie
     * @param args Arugmenty programu
     */
    public static void main(String[] args) {
        new BoardGui();
    }

}
