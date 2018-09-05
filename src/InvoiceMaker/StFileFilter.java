package InvoiceMaker;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class StFileFilter extends FileFilter {

    private final String extension;

    public StFileFilter(String extension) {
        this.extension = extension;
    }

    @Override
    public boolean accept(File file) {
        return file.isDirectory() || file.getName().toLowerCase().endsWith(extension);
    }

    @Override
    public String getDescription() {
        return "(" + extension + ")";
    }
}
