import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class OneDimensionalViewController extends JPanel {
    ArrayList<boolean[]> automation_grid = new ArrayList<>();
    Dimension panel_size;
    Color filled_color;

    public OneDimensionalViewController(ArrayList<boolean[]> automation_grid, Dimension panel_size, Color filled_color) {
        setBackground(Color.WHITE);
        setSize(panel_size.width + 200, panel_size.height);

        this.automation_grid = automation_grid;
        this.panel_size = panel_size;
        this.filled_color = filled_color;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(filled_color);

        for (int i = 0; i < this.automation_grid.size(); i++) {
            boolean[] current_row = this.automation_grid.get(i);
            for (int j = 0; j < current_row.length; j++) {
                if (current_row[j]) {
                    g.setColor(filled_color);
                }
                else {
                    g.setColor(Color.WHITE);
                }
                g.drawRect(j, i, 1, 1);
            }
        }
    }
}
