package org.gaussian.graphql.pulse.helpers;

import com.google.common.io.Resources;

import java.io.IOException;
import java.nio.charset.Charset;

public class QueryHelper {

    public static String resource(String filename, Charset charset) {
        try {
            return Resources.toString(Resources.getResource(filename), charset).trim();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
