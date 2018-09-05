package InvoiceMaker;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class TextAreaRenderer implements TableCellRenderer {
    JTextArea textArea;

    public TextAreaRenderer() {
        textArea = new JTextArea();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        textArea.setText((String)value);
        if (isSelected) {
            textArea.setBackground(Color.CYAN);
        } else {
            textArea.setBackground(Color.WHITE);
        }
        return textArea;
    }
}
