package org.gaussian.graphql.pulse;

import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryListener {

    private static final Logger LOG = LoggerFactory.getLogger(QueryListener.class);
    private static int eventsHandled;

    @Subscribe
    public void stringEvent(String event) {
        eventsHandled++;
        LOG.info("listening....");
    }
}
