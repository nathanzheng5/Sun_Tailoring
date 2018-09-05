package InvoiceMaker;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class StConfig {

    private static final String CONFIG_FILE_PATH = "./Settings/config.properties";

    private static Properties theInstance;

    public static Properties getInstance() {
        if (theInstance == null) {
            theInstance = new Properties();
            try (FileReader reader = new FileReader(CONFIG_FILE_PATH)) {
                theInstance.load(reader);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return theInstance;
    }

}
