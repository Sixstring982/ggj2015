package com.lunagameserve.ggj2015.textServer;

/**
 * Created by six on 1/23/15.
 */
public interface Stream {

    public void start();

    public void stop();

    public void write(String str);

    public String read();

    public int linesAvailable();
}
