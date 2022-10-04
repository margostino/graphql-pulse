package org.gaussian.graphql.pulse.configuration;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import lombok.Builder;

@Builder
public class PulseConfig {

    private final ClusterConfig clusterConfig;
    private final boolean micrometerEnabled;

    public Config getClusterConfig() {
        Config config = new Config();
        config.setClusterName(clusterConfig.getName());
        //config.getCPSubsystemConfig().setCPMemberCount(0);
        config.getMapConfig("pulse").setBackupCount(clusterConfig.getBackupCount());

        NetworkConfig network = config.getNetworkConfig();
        network.setPort(clusterConfig.getNetworkConfig().getPort())
                .setPortCount(clusterConfig.getNetworkConfig().getPortCount())
                .setPortAutoIncrement(clusterConfig.getNetworkConfig().isPortAutoIncrement());

        JoinConfig join = network.getJoin();
        join.getMulticastConfig().setEnabled(clusterConfig.getNetworkConfig().isMultiCastEnabled());

        return config;
    }
}
