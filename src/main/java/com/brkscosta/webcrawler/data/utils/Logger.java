package com.brkscosta.webcrawler.data.utils;

import com.google.inject.Singleton;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

@Singleton
public final class Logger {

    private static final String DEFAULT_LOGFILE = "logger.txt";
    private PrintStream printStream;

    /**
     * Constructs a new instance of Logger.
     */
    public Logger() {
        this(DEFAULT_LOGFILE);
    }

    /**
     * Constructs a new instance of Logger.
     * @param logFileName - the logger file name
     */
    public Logger(String logFileName) {
        connect(logFileName);
    }

    private void connect(String logFileName) {
        try {
            this.printStream = new PrintStream(new FileOutputStream(logFileName, false), true);
        } catch (FileNotFoundException ex) {
            System.err.println("Error initializing logger: " + ex.getMessage());
        }
    }

    /**
     * Log a message with the current date
     * @param message - the message
     */
    public void writeToLog(String message) {
        if (this.printStream != null) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            StackTraceElement caller = stackTrace[2];

            String fullClassName = caller.getClassName();
            String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            String methodName = caller.getMethodName();

            String timeStamp = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").format(new Date());
            String loggFormated = timeStamp + " - " + className + "::" + methodName + ": " + message;

            printStream.println(loggFormated);
            System.out.println(loggFormated);
        } else {
            System.err.println("Logger is not initialized.");
        }
    }
}
