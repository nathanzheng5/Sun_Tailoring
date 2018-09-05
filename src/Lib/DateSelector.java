package Lib;

import javax.swing.*;
import java.awt.event.ActionListener;

public interface DateSelector {

    JComponent getComponent();

    void addActionListener(ActionListener listener);

    void removeActionListener(ActionListener listener);

    String getFormattedDate();

    void setDate(long dateInMillis);

}
