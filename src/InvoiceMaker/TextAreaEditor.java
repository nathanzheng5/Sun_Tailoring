package InvoiceMaker;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.EventObject;

public class TextAreaEditor implements TableCellEditor {

    protected EventListenerList listenerList = new EventListenerList();
    private final JScrollPane scrollPane;
    private JTextArea textArea = new JTextArea();

    public TextAreaEditor() {
        scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        textArea.setLineWrap(true);
        textArea.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));

    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        textArea.setFont(table.getFont());
        textArea.setText(value != null ? value.toString() : "");
        return scrollPane;
    }

    @Override
    public Object getCellEditorValue() {
        return textArea.getText();
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean stopCellEditing() {
        fireEditingStopped();
        return true;
    }

    @Override
    public void cancelCellEditing() {
        fireEditingCanceled();
    }

    @Override
    public void addCellEditorListener(CellEditorListener l) {
        listenerList.add(CellEditorListener.class, l);
    }

    @Override
    public void removeCellEditorListener(CellEditorListener l) {
        listenerList.remove(CellEditorListener.class, l);
    }

    protected void fireEditingStopped() {
        Object[] listeners = listenerList.getListenerList();
        for (Object listener : listeners) {
            if (listener instanceof CellEditorListener) {
                ((CellEditorListener) listener).editingStopped(new ChangeEvent(this));
            }
        }
    }

    protected void fireEditingCanceled() {
        Object[] listeners = listenerList.getListenerList();
        for (Object listener : listeners) {
            if (listener instanceof CellEditorListener) {
                ((CellEditorListener) listener).editingCanceled(new ChangeEvent(this));
            }
        }
    }

}
