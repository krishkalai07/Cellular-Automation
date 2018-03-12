package ElementaryCellularAutomation;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;


public class Automator {
    private HashMap<String, Boolean> grid_leveling = new HashMap<>();
    private ArrayList<boolean[]> automation_grid = new ArrayList<>();

    public static String toLen8BinaryString(byte value) {
        StringBuilder s = new StringBuilder();

        for (; value > 0; value>>=1) {
            s.insert(0, value % 2 == 1 ? 1 : 0);
        }

        for (int i = s.length(); i < 8; i++) {
            s.insert(0, 0);
        }

        return s.toString();
    }

    public static String toLen3BinaryString(byte value) {
        StringBuilder s = new StringBuilder();

        for (; value > 0; value>>=1) {
            s.insert(0, value % 2 == 1 ? 1 : 0);
        }

        for (int i = s.length(); i < 3; i++) {
            s.insert(0, 0);
        }

        return s.toString();
    }

    public Automator(byte rule, int size) {
        boolean[] row1 = new boolean[size];
        row1[size/2 + 1] = true;
        this.automation_grid.add(row1);
        String s_rule = toLen8BinaryString(rule);

        for (byte i = 0; i < 8; i++) {
            this.grid_leveling.put(toLen3BinaryString(i), s_rule.charAt(s_rule.length() - i - 1) == '1');
        }
    }

    public void evolve() {
        boolean[] base = automation_grid.get(automation_grid.size() - 1);
        boolean[] new_base = new boolean[base.length];
        for (int i = 0; i < base.length; i++) {
            String current_triplet = "";
            if (i == 0) {
                current_triplet += 0 + "" + (base[i] ? 1 : 0) + "" + (base[i+1] ? 1 : 0);
            }
            else if (i == base.length - 1) {
                current_triplet += (base[i-1] ? 1 : 0) + "" + (base[i] ? 1 : 0) + "" + 0;
            }
            else {
                current_triplet += (base[i-1] ? 1 : 0) + "" + (base[i] ? 1 : 0) + "" + (base[i+1] ? 1 : 0);
            }

//            System.out.println(current_triplet);
            new_base[i] = grid_leveling.get(current_triplet);
        }

        automation_grid.add(new_base);
    }

    public void print_evolution_state() {
        for (int i = 0; i < this.automation_grid.size(); i++) {
            boolean[] current_row = this.automation_grid.get(i);
            for (int j = 0; j < current_row.length; j++) {
                System.out.print((current_row[j] ? '1' : '0') + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Automator test = new Automator((byte)22, 1440);

        for (int i = 0; i < 900; i++) {
            test.evolve();
        }

        int window_width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int window_height = Toolkit.getDefaultToolkit().getScreenSize().height;

        JFrame window = new JFrame("Magic Fractal");
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        OneDimensionalViewController content = new OneDimensionalViewController(test.automation_grid, new Dimension(1400, 900), Color.BLACK);
        window.setContentPane(content);
        window.setSize(window_width, window_height);
        window.setUndecorated(true);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setVisible(true);
    }
}