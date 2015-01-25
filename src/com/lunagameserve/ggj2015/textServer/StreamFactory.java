package com.lunagameserve.ggj2015.textServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Six on 1/24/2015.
 */
public class StreamFactory {

    /**
     * Creates an instance of a {@link Stream}.
     * @return
     */
    public static Stream createStream() {
        return new TextServer();
    }

    private static class StdinStream implements Stream {
        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }

        @Override
        public void write(String str) {
            System.out.println(str);
        }

        @Override
        public String read(int timeout) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                /* We need to include a header so we know how to route information */
                return reader.readLine();
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        public int linesAvailable() {
            return 1; /*Sure, it's available no matter what.*/
        }
    }
}
