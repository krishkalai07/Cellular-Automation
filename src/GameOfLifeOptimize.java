import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import static java.awt.event.KeyEvent.*;
import static java.lang.Integer.parseInt;

@SuppressWarnings("ALL")
public class GameOfLifeOptimize extends JPanel implements ActionListener, KeyListener, MouseListener {
    Set<Point> active_cells = new HashSet<>();
    private int width;
    private int height;
    private int grid_split;
    private boolean toroid_mode;

    private Timer timer;

    private int count_surrounding_population_non_modular(int x, int y) {
        int count = 0;

        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                // Check to prevent OutOfBoundsException
                if (i < 0 || j < 0 || i >= width || j >= height) {
                    continue;
                }
                if (i == x && j == y) {
                    continue;
                }
                if (active_cells.contains(new Point(i,j))) {
                    count++;
                }
            }
        }

        return count;
    }

    private int count_surrounding_population_modular(int x, int y) {
        int count = 0;

        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if (i == x && j == y) {
                    continue;
                }
                if (active_cells.contains(new Point((i + width) % width, (j + height) % height))) {
                    count++;
                }
            }
        }

        return count;
    }

    public GameOfLifeOptimize(int width, int height, Set<Point> active_cells) {
        setBackground(Color.BLACK);
        addMouseListener(this);
        addKeyListener(this);
        setFocusable(true);
        requestFocus();

        this.timer = new Timer(100, this);
        this.timer.start();

        this.grid_split = 20;

        this.active_cells = active_cells;
        this.width = width/grid_split;
        this.height = height/grid_split;
        this.toroid_mode = false;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawGridLines(g);
        drawGrid(g);

        g.setColor(Color.WHITE);
        //g.drawString("Toroid mode: " + ((this.toroid_mode) ? "ON" : "OFF"), 10, 10);
    }

    private void drawGridLines(Graphics g) {
        g.setColor(Color.GRAY);

        for (int i = 0; i <= width*grid_split; i++) {
            g.fillRect(i-1, 0, 2, height*grid_split);
        }

        for (int i = 0; i <= height*grid_split; i++) {
            g.fillRect(0, i-1, width*grid_split, 2);
        }
    }

    private void drawGrid(Graphics g) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (active_cells.contains(new Point(i,j))) {
                    g.setColor(Color.WHITE);
                }
                else {
                    g.setColor(Color.BLACK);
                }
                g.fillRect(grid_split*i, grid_split*j, grid_split-1, grid_split-1);
            }
        }
    }

    private Set<Point> evolve() {
        Set<Point> new_active_cell = new HashSet<>();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int surround;
                if (toroid_mode) {
                    surround = count_surrounding_population_modular(i, j);
                }
                else {
                    surround = count_surrounding_population_non_modular(i, j);
                }

                if (active_cells.contains(new Point(i,j))) {
                    if (surround == 2 || surround == 3) {
                        new_active_cell.add(new Point(i, j));
                    }
                }
                else {
                    if (surround == 3) {
                        new_active_cell.add(new Point(i, j));
                    }
                }
            }
        }

        return new_active_cell;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        active_cells = evolve();
        repaint();
    }

    public static void main(String[] args) {


        int window_width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int window_height = Toolkit.getDefaultToolkit().getScreenSize().height;

        JFrame window = new JFrame();
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        GameOfLifeOptimize content = new GameOfLifeOptimize(1440, 900, new HashSet<>());
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
     * SPACE toggles the timer running or stopped.
     * S saves the current grid to the file named "save.txt"
     * L loads the grid saved in "save.txt" and stops the timer
     * C clears the grid and stops the timer
     * M toggles modular/toroid mode (cells at the edge of the grid wrap around
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
                active_cells = read_save_file();
                repaint();
                break;

            case VK_C:
                timer.stop();
                clear_grid();
                repaint();
                break;

            case VK_M:
                this.toroid_mode = !this.toroid_mode;
                break;
        }

    }

    private void write_to_file() {
        PrintWriter pw;
        try {
            pw = new PrintWriter(new FileOutputStream("save_3.txt"));
        } catch (IOException e) {
            return;
        }

        for (Point p:active_cells) {
            pw.println(p.x + " " + p.y);
        }

        pw.close();
    }

    private Set<Point> read_save_file() {
        Scanner sc;
        Set<Point> new_active_cells = new HashSet<>();
        try {
            sc = new Scanner(new FileInputStream("save_3.txt"));
        } catch (IOException e) {
            return null;
        }

        for (int i = 0; sc.hasNextLine(); i++) {
            String[] tokens = sc.nextLine().split("[ ]");
            for (int j = 0; j < tokens.length; j++) {
                new_active_cells.add(new Point(parseInt(tokens[0]), parseInt(tokens[1])));
            }
        }

        return new_active_cells;
    }

    private void clear_grid() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                active_cells.clear();
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
        int tile_x = e.getX()/grid_split;
        int tile_y = e.getY()/grid_split;

        if (active_cells.contains(new Point(tile_x, tile_y))) {
            active_cells.remove(new Point(tile_x, tile_y));
        }
        else {
            active_cells.add(new Point(tile_x, tile_y));
        }
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
