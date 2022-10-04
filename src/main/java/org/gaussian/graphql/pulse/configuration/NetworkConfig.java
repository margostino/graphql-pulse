package org.gaussian.graphql.pulse.configuration;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NetworkConfig {

    private final int port;
    private final int portCount;
    private final boolean portAutoIncrement;
    private final boolean multiCastEnabled;
}
