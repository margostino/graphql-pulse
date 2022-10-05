package org.gaussian.graphql.pulse.app;

import com.google.common.io.Resources;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import org.gaussian.graphql.pulse.configuration.PulseConfig;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HeaderPrinter {

    private final static Logger log = LoggerFactory.getLogger("main");

    public static void printHeader(PulseConfig pulseConfig) {
        if (pulseConfig.isBannerEnabled()) {
            final String[] lines = header(pulseConfig.getBannerFilename()).split(System.lineSeparator());
            for (final String line : lines) {
                log.info(line);
            }
        }
    }

    private static String header(String filename) {
        try {
            final URL headerUrl = Resources.getResource(filename);
            return Resources.toString(headerUrl, StandardCharsets.UTF_8);
        } catch (final IOException | IllegalArgumentException e) {
            log.error("Unable to reader header file", e);
            return "";
        }
    }

}
