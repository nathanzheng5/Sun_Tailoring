package InvoiceMaker;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;

public class JTextAreaOutputStream extends OutputStream {

    private final JTextArea destination;

    public JTextAreaOutputStream(JTextArea destination) {
        assert destination != null;
        this.destination = destination;
    }

    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {
        final String text = new String (buffer, offset, length);
        SwingUtilities.invokeLater(new Runnable ()
            {
                @Override
                public void run()
                {
                    destination.append (text);
                }
            });
    }

    @Override
    public void write(int b) throws IOException {
        write (new byte [] {(byte)b}, 0, 1);
    }
}
