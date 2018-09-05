package InvoiceMaker;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.*;

public class StLogger {
    private static final String CLASS_NAME = "StLogger";
    private static Logger logger = Logger.getLogger(CLASS_NAME);
    private static FileHandler fh = null;
    private static final SimpleDateFormat fileNameDateFormatter = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");

    public static void startApplicationLogging() {
        try {
            LogManager logManager = LogManager.getLogManager();

            fh = new FileHandler("./logs/InvoiceMaker." + fileNameDateFormatter.format(Calendar.getInstance().getTime()) + ".log");
            fh.setFormatter(new SimpleFormatter());
            Logger.getLogger("").addHandler(fh);
            PrintStream stdout = System.out;
            PrintStream stderr = System.err;

            LoggingOutputStream los;

            logger = Logger.getLogger("stdout");
            los = new LoggingOutputStream(logger, StdOutErrLevel.STDOUT);
            System.setOut(new PrintStream(los, true));

            logger = Logger.getLogger("stderr");
            los = new LoggingOutputStream(logger, StdOutErrLevel.STDERR);
            System.setErr(new PrintStream(los, true));

            logger.info("started application logging");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void close() {
        if (fh != null) {
            fh.close();
        }
    }

    public static void logAction(String msg) {
        logger.logp(Level.INFO, CLASS_NAME, "action", msg);
    }

    public static void logDetailedAction(String msg) {
        logger.logp(Level.INFO, CLASS_NAME, "action", msg);
    }

}
