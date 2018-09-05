package Lib;

import InvoiceMaker.StLogger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public abstract class SimpleAction extends AbstractAction {

    private final boolean popCompletionMsg;
    private final Component busyCursorComponent;

    public SimpleAction(boolean popCompletionMsg, Component busyCursorComponent) {
        this.popCompletionMsg = popCompletionMsg;
        this.busyCursorComponent = busyCursorComponent;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            busyCursorComponent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            action();
            onCompletion();
            busyCursorComponent.setCursor(Cursor.getDefaultCursor());

            String completionMsg = getCompletionMsg();
            if (completionMsg != null && !completionMsg.isEmpty()) {
                if (popCompletionMsg) {
                    GuiUtils.popMsg(completionMsg);
                }
                StLogger.logAction(completionMsg);
            }

        } catch (Exception error) {
            if (error instanceof NullPointerException ||
                    error instanceof IndexOutOfBoundsException) {
                error.printStackTrace();
                GuiUtils.popError("Unexpected error. Shut down and restart the program. Contact Nathan");
            }
            onError(error);
            busyCursorComponent.setCursor(Cursor.getDefaultCursor());

            String errorMsg = getErrorMsg(error);
            if (errorMsg != null && !errorMsg.isEmpty()) {
                GuiUtils.popError(errorMsg);
            }
        }
    }

    /**
     * Main action - this is executed on the event dispatch thread, so no long operations.
     *
     * @throws Exception when there is an error.
     */
    protected abstract void action() throws Exception;

    /**
     * What to do when action finishes.
     *
     * @throws Exception
     */
    protected void onCompletion() {}

    /**
     * What to do when action fails
     */
    protected void onError(Exception error) {}

    protected String getCompletionMsg() { return null; }

    protected String getErrorMsg(Exception error) { return null; }

}
