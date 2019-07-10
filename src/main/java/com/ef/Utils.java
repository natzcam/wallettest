package com.ef;

import java.io.*;
import java.nio.charset.Charset;

class Utils {
    public static String streamToString(InputStream is) throws IOException {
        StringBuilder txt = new StringBuilder();
        try (
                Reader reader = new BufferedReader(new InputStreamReader
                        (is, Charset.forName("UTF-8")))) {
            int c;
            while ((c = reader.read()) != -1) {
                txt.append((char) c);
            }
        }
        return txt.toString();
    }
}
