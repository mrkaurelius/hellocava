package dev.kumru;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

public class JavaLoggingApp {

    static {
        Configurator.setLevel(dev.kumru.JavaLoggingApp.class, Level.TRACE);
    }

    protected static Logger logger = LogManager.getLogger(dev.kumru.JavaLoggingApp.class);

    public static void main(String[] args) {
        logger.trace("Trace ettigimle kaldi");
        logger.info("Merhaba yalan dunya!");
    }
}
