package org.gaussian.graphql.pulse.configuration;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClusterConfig {

    private final String name;
    private final int backupCount;
    private final NetworkConfig networkConfig;
}
