package com.mandria.android.mvp;

import android.util.Log;

/**
 * MVP logger.
 */
public final class MVPLogger {

    public static boolean SHOW_MVP_LOGS = false;

    private static final int VERBOSE = 2;

    private static final int DEBUG = 3;

    private static final int INFO = 4;

    private static final int WARN = 5;

    private static final int ERROR = 6;

    /**
     * Logs if logs are enabled.
     *
     * @param tag   Tag.
     * @param msg   Message.
     * @param tr    Throwable.
     * @param level Log level
     * @return Android SDK logger return value.
     */
    private static int log(String tag, String msg, Throwable tr, int level) {
        if (SHOW_MVP_LOGS) {
            switch (level) {
                case VERBOSE:
                    return tr == null ? Log.v(tag, msg) : Log.v(tag, msg, tr);
                case DEBUG:
                    return tr == null ? Log.d(tag, msg) : Log.d(tag, msg, tr);
                case INFO:
                    return tr == null ? Log.i(tag, msg) : Log.i(tag, msg, tr);
                case WARN:
                    return tr == null ? Log.w(tag, msg) : Log.w(tag, msg, tr);
                case ERROR:
                    return tr == null ? Log.e(tag, msg) : Log.e(tag, msg, tr);
                default:
                    return 0;
            }
        }
        return 0;
    }

    /**
     * Logs a verbose message.
     *
     * @param tag Tag.
     * @param msg Message.
     * @return Android SDK logger return value.
     */
    public static int v(String tag, String msg) {
        return log(tag, msg, null, VERBOSE);
    }

    /**
     * Logs a verbose message.
     *
     * @param tag Tag.
     * @param msg Message.
     * @param tr  Throwable.
     * @return Android SDK logger return value.
     */
    public static int v(String tag, String msg, Throwable tr) {
        return log(tag, msg, tr, VERBOSE);
    }

    /**
     * Logs a debug message.
     *
     * @param tag Tag.
     * @param msg Message.
     * @return Android SDK logger return value.
     */
    public static int d(String tag, String msg) {
        return log(tag, msg, null, DEBUG);
    }

    /**
     * Logs a debug message.
     *
     * @param tag Tag.
     * @param msg Message.
     * @param tr  Throwable.
     * @return Android SDK logger return value.
     */
    public static int d(String tag, String msg, Throwable tr) {
        return log(tag, msg, tr, DEBUG);
    }

    /**
     * Logs an info message.
     *
     * @param tag Tag.
     * @param msg Message.
     * @return Android SDK logger return value.
     */
    public static int i(String tag, String msg) {
        return log(tag, msg, null, INFO);
    }

    /**
     * Logs an info message.
     *
     * @param tag Tag.
     * @param msg Message.
     * @param tr  Throwable.
     * @return Android SDK logger return value.
     */
    public static int i(String tag, String msg, Throwable tr) {
        return log(tag, msg, tr, INFO);
    }

    /**
     * Logs a warn message.
     *
     * @param tag Tag.
     * @param msg Message.
     * @return Android SDK logger return value.
     */
    public static int w(String tag, String msg) {
        return log(tag, msg, null, WARN);
    }

    /**
     * Logs a warn message.
     *
     * @param tag Tag.
     * @param msg Message.
     * @param tr  Throwable.
     * @return Android SDK logger return value.
     */
    public static int w(String tag, String msg, Throwable tr) {
        return log(tag, msg, tr, WARN);
    }

    /**
     * Logs a error message.
     *
     * @param tag Tag.
     * @param msg Message.
     * @return Android SDK logger return value.
     */
    public static int e(String tag, String msg) {
        return log(tag, msg, null, ERROR);
    }

    /**
     * Logs a error message.
     *
     * @param tag Tag.
     * @param msg Message.
     * @param tr  Throwable.
     * @return Android SDK logger return value.
     */
    public static int e(String tag, String msg, Throwable tr) {
        return log(tag, msg, tr, ERROR);
    }
}
