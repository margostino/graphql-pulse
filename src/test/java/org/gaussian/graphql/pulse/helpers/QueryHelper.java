package org.gaussian.graphql.pulse.helpers;

import com.google.common.io.Resources;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class QueryHelper {

    public static String resource(String filename) {
        try {
            return Resources.toString(Resources.getResource(filename), UTF_8).trim();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
