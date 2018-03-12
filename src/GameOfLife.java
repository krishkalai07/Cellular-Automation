import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import static java.awt.event.KeyEvent.*;
import static java.lang.Integer.parseInt;

public class GameOfLife extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
    private Set<Point> active_cells = new HashSet<>();
    private int width;
    private int height;
    private int grid_split;
    private boolean toroid_mode;

    private Timer timer;

    private int mouse_down_x;
    private int mouse_down_y;
    private int offset_x;
    private int offset_y;

    private int count_surrounding_population(int x, int y) {
        int count = 0;

        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                // Check to prevent OutOfBoundsException in non-toroid mode.
                if (!toroid_mode && (i < 0 || j < 0 || i >= width || j >= height)) {
                    continue;
                }
                if (i == x && j == y) {
                    continue;
                }
                if (toroid_mode ? active_cells.contains(new Point((i + width) % width, (j + height) % height)) : active_cells.contains(new Point(i,j))) {
                    count++;
                }
            }
        }

        return count;
    }

    public GameOfLife(int width, int height, Set<Point> active_cells) {
        setBackground(Color.BLACK);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);
        setFocusable(true);
        requestFocus();

        this.timer = new Timer(50, this);
        this.timer.start();

        this.grid_split = 20;

        this.active_cells = active_cells;
        this.width = width;
        this.height = height;
        this.toroid_mode = true;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        //if (grid_split > 3) {
            drawGridLines(g);
        //}
        drawGrid(g);

        //g.drawString("Toroid mode: " + ((this.toroid_mode) ? "ON" : "OFF"), 10, 10);
    }

    private void drawGridLines(Graphics g) {
        g.setColor(Color.GRAY);

        for (int i = 0; i <= width; i++) {
            int start = offset_y > 0 ? offset_y : 0;
            int end = offset_y > 0 ? height*grid_split : height*grid_split + offset_y;
            g.fillRect(((i*grid_split)-1) + offset_x, start, 2, end);
        }

        for (int i = 0; i <= height; i++) {
            int start = offset_x > 0 ? offset_x : 0;
            int end = offset_x > 0 ? width*grid_split : width*grid_split + offset_x;
            g.fillRect(start, (i*grid_split-1) + offset_y, end, 2);
        }
    }

    private void drawGrid(Graphics g) {
        g.setColor(Color.WHITE);
        for (Point p:active_cells) {
            g.fillRect(grid_split*p.x + offset_x, grid_split*p.y + offset_y, grid_split-1, grid_split-1);
        }
    }

    private Set<Point> evolve() {
        Set<Point> new_active_cell = new HashSet<>();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int surround = count_surrounding_population(i, j);

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
            pw = new PrintWriter(new FileOutputStream("puffer_boat.txt"));
        } catch (IOException e) {
            return;
        }

        pw.println(width + " " + height);
        for (Point p:active_cells) {
            pw.println(p.x + " " + p.y);
        }

        pw.close();
    }

    private Set<Point> read_save_file() {
        Scanner sc;
        Set<Point> new_active_cells = new HashSet<>();
        try {
            sc = new Scanner(new FileInputStream("puffer_boat.txt"));
        } catch (IOException e) {
            return null;
        }


        String[] tokens = sc.nextLine().split("[ ]");
        width = parseInt(tokens[0]);
        height = parseInt(tokens[1]);
        while (sc.hasNextLine()) {
            tokens = sc.nextLine().split("[ ]");
            new_active_cells.add(new Point(parseInt(tokens[0]), parseInt(tokens[1])));
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
        int tile_x = (e.getX() - offset_x)/grid_split;
        int tile_y = (e.getY() - offset_y)/grid_split;

        if (active_cells.contains(new Point(tile_x, tile_y))) {
            active_cells.remove(new Point(tile_x, tile_y));
        }
        else {
            active_cells.add(new Point(tile_x, tile_y));
        }
        repaint();
    }

    /**
     * Scrolling zooms at the center of the screen.
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        grid_split += e.getWheelRotation();
        repaint();
    }

    /**
     * Dragging the mouse will change the rendered region.
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        offset_x = e.getX() - mouse_down_x;
        offset_y = e.getY() - mouse_down_y;
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.mouse_down_x = e.getX() - offset_x;
        this.mouse_down_y = e.getY() - offset_y;
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
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    public static void main(String[] args) {
        int window_width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int window_height = Toolkit.getDefaultToolkit().getScreenSize().height;

        JFrame window = new JFrame();
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        GameOfLife content = new GameOfLife(800, 175, new HashSet<>());
        window.setContentPane(content);
        window.setSize(window_width, window_height);
        window.setUndecorated(true);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setVisible(true);


    }
}
