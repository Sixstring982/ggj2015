package com.lunagameserve.ggj2015.textServer;

/**
 * Created by six on 1/23/15.
 */
public interface Stream {

    public void start();

    public void stop();

    public void write(String str);

    /**
     * Reads a line from this {@link Stream}.
     * @param timeout The timeout period, in milliseconds, that reading from this
     *                {@link Stream} will block the calling thread before returning.
     * @return the {@link String} read from this {@link Stream}, or {@code null}
     *         if the timeout elapses.
     */
    public String read(int timeout);

    public int linesAvailable();
}
