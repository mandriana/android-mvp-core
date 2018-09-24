package com.mandria.android.mvp

import android.util.Log

/** Logger for the library **/
object MVPLogger {

    var SHOW_MVP_LOGS = false

    private const val VERBOSE = 2

    private const val DEBUG = 3

    private const val INFO = 4

    private const val WARN = 5

    private const val ERROR = 6

    /**
     * Logs if logs are enabled.
     *
     * @param tag   Tag.
     * @param msg   Message.
     * @param tr    Throwable.
     * @param level Log level
     * @return Android SDK logger return value.
     */
    private fun log(tag: String, msg: String, tr: Throwable?, level: Int): Int {
        if (SHOW_MVP_LOGS) {
            return when (level) {
                VERBOSE -> if (tr == null) Log.v(tag, msg) else Log.v(tag, msg, tr)
                DEBUG -> if (tr == null) Log.d(tag, msg) else Log.d(tag, msg, tr)
                INFO -> if (tr == null) Log.i(tag, msg) else Log.i(tag, msg, tr)
                WARN -> if (tr == null) Log.w(tag, msg) else Log.w(tag, msg, tr)
                ERROR -> if (tr == null) Log.e(tag, msg) else Log.e(tag, msg, tr)
                else -> 0
            }
        }
        return 0
    }

    /**
     * Logs a verbose message.
     *
     * @param tag Tag.
     * @param msg Message.
     * @return Android SDK logger return value.
     */
    fun v(tag: String, msg: String): Int = log(tag, msg, null, VERBOSE)

    /**
     * Logs a verbose message.
     *
     * @param tag Tag.
     * @param msg Message.
     * @param tr  Throwable.
     * @return Android SDK logger return value.
     */
    fun v(tag: String, msg: String, tr: Throwable): Int = log(tag, msg, tr, VERBOSE)

    /**
     * Logs a debug message.
     *
     * @param tag Tag.
     * @param msg Message.
     * @return Android SDK logger return value.
     */
    fun d(tag: String, msg: String): Int = log(tag, msg, null, DEBUG)

    /**
     * Logs a debug message.
     *
     * @param tag Tag.
     * @param msg Message.
     * @param tr  Throwable.
     * @return Android SDK logger return value.
     */
    fun d(tag: String, msg: String, tr: Throwable): Int = log(tag, msg, tr, DEBUG)

    /**
     * Logs an info message.
     *
     * @param tag Tag.
     * @param msg Message.
     * @return Android SDK logger return value.
     */
    fun i(tag: String, msg: String): Int = log(tag, msg, null, INFO)

    /**
     * Logs an info message.
     *
     * @param tag Tag.
     * @param msg Message.
     * @param tr  Throwable.
     * @return Android SDK logger return value.
     */
    fun i(tag: String, msg: String, tr: Throwable): Int = log(tag, msg, tr, INFO)

    /**
     * Logs a warn message.
     *
     * @param tag Tag.
     * @param msg Message.
     * @return Android SDK logger return value.
     */
    fun w(tag: String, msg: String): Int = log(tag, msg, null, WARN)

    /**
     * Logs a warn message.
     *
     * @param tag Tag.
     * @param msg Message.
     * @param tr  Throwable.
     * @return Android SDK logger return value.
     */
    fun w(tag: String, msg: String, tr: Throwable): Int = log(tag, msg, tr, WARN)

    /**
     * Logs a error message.
     *
     * @param tag Tag.
     * @param msg Message.
     * @return Android SDK logger return value.
     */
    fun e(tag: String, msg: String): Int = log(tag, msg, null, ERROR)

    /**
     * Logs a error message.
     *
     * @param tag Tag.
     * @param msg Message.
     * @param tr  Throwable.
     * @return Android SDK logger return value.
     */
    fun e(tag: String, msg: String, tr: Throwable): Int = log(tag, msg, tr, ERROR)
}