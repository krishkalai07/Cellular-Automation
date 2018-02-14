import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import static java.awt.event.KeyEvent.*;

public class GameOfLife extends JPanel implements ActionListener, KeyListener, MouseListener {
    private boolean[][] grid;
    private int width;
    private int height;
    private int grid_split;

    private Timer timer;

    public int count_surrounding_population(int x, int y) {
        int count = 0;

        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                // Check to prevent OutOfBoundsException
                if (i < 0 || j < 0 || i >= grid.length || j >= grid[x].length) {
                    continue;
                }
                if (i == x && j == y) {
                    continue;
                }
                if (grid[i][j]) {
                    count++;
                }
            }
        }

        return count;
    }

    public GameOfLife(int width, int height, boolean[][] grid) {
        setBackground(Color.BLACK);
        addMouseListener(this);
        addKeyListener(this);
        setFocusable(true);
        requestFocus();

        this.timer = new Timer(100, this);
        this.timer.start();

        this.grid = grid;
        this.width = width;
        this.height = height;
        this.grid_split = 20;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawGridLines(g);
        drawGrid(g);
    }

    private void drawGridLines(Graphics g) {
        g.setColor(Color.GRAY);

        for (int i = 0; i <= width; i+= grid_split) {
            g.fillRect(i-1, 0, 2, height);
        }

        for (int i = 0; i <= height; i+= grid_split) {
            g.fillRect(0, i-1, width, 2);
        }
    }

    private void drawGrid(Graphics g) {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j]) {
                    g.setColor(Color.WHITE);
                }
                else {
                    g.setColor(Color.BLACK);
                }
                g.fillRect(grid_split*i, grid_split*j, grid_split-1, grid_split-1);
            }
        }
    }

    private boolean[][] evolve() {
        boolean[][] new_grid = new boolean[width/grid_split][height/grid_split];

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                int surround = count_surrounding_population(i, j);
                if (grid[i][j]) {
                    new_grid[i][j] = surround == 2 || surround == 3;
                }
                else {
                    new_grid[i][j] = surround == 3;
                }
            }
        }

        return new_grid;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        grid = evolve();
        repaint();
    }

    public static void main(String[] args) {
        boolean[][] grid = new boolean[1440/20][900/20];

        initialize_I_column(grid);

        int window_width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int window_height = Toolkit.getDefaultToolkit().getScreenSize().height;

        JFrame window = new JFrame();
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        GameOfLife content = new GameOfLife(1440, 900, grid);
        window.setContentPane(content);
        window.setSize(window_width, window_height);
        window.setUndecorated(true);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setVisible(true);
    }

    private static void initialize_I_column(boolean[][] grid) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 12; j++) {
                if (i == 0 || i == 2) {
                    grid[i+(grid.length/2)-1][j+(grid[0].length/2)-6] = j != 1 && j != 2 && j != 4 && j != 7 && j != 9 && j != 10;
                }
                else {
                    grid[i+(grid.length/2)-1][j+(grid[0].length/2)-6] = j != 4 && j != 7;
                }
            }
        }
    }

    /**
     * Pressing (actually releasing) a key performs various actions.
     * SPACE stops the timer, freezing the grid on the frame.
     * S saves the current grid to the file named "save.txt"
     * L loads the grid saved in "save.txt" and stops the timer
     * C clears the grid and stops the timer
     *
     * @param e Some KeyEvent which reads what button you press.
     */
    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case VK_SPACE:
                if (timer.isRunning()) {
                    timer.stop();
                }
                else {
                    timer.restart();
                }
                break;

            case VK_S:
                timer.stop();
                write_to_file();
                timer.restart();
                break;

            case VK_L:
                timer.stop();
                grid = read_save_file();
                repaint();
                break;

            case VK_C:
                timer.stop();
                clear_grid();
                repaint();
                break;
        }

    }

    private void write_to_file() {
        PrintWriter pw;
        try {
            pw = new PrintWriter(new FileOutputStream("save.txt"));
        } catch (IOException e) {
            return;
        }

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                pw.print(grid[i][j] ? '1' : '0');

                if (j != grid[i].length - 1) {
                    pw.print(' ');
                }
            }
            if (i != grid.length - 1) {
                pw.println();
            }
        }

        pw.close();
    }

    private boolean[][] read_save_file() {
        Scanner sc;
        boolean[][] new_grid = new boolean[width/grid_split][height/grid_split];
        try {
            sc = new Scanner(new FileInputStream("save.txt"));
        } catch (IOException e) {
            return null;
        }

        for (int i = 0; sc.hasNextLine(); i++) {
            String[] tokens = sc.nextLine().split("[ ]");
            for (int j = 0; j < tokens.length - 1; j++) {
                new_grid[i][j] = tokens[j].equals("1");
            }
        }

        return new_grid;
    }

    private void clear_grid() {
        for (int i = 0; i < width/grid_split; i++) {
            for (int j = 0; j < height/grid_split; j++) {
                grid[i][j] = false;
            }
        }
    }

    /**
     * Clicking on a tile toggles it between (populated and unpopulated)
     *
     * @param e Some KeyEvent which reads what you clicked with and where you clicked.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        int mouse_x = e.getX();
        int mouse_y = e.getY();

        grid[mouse_x/grid_split][mouse_y/grid_split] = !grid[mouse_x/grid_split][mouse_y/grid_split];
        repaint();
    }

    //
    // Series of required implemented functions that has no operations
    //

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

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
}
