package com.avereon.recon;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NetworkGraphTest {

	@Test
	void testNetworkGraph() {
		NetworkGraph graph = new NetworkGraph();
		assertThat( graph.getRootDevice().getName() ).isEqualTo( "This Computer" );
		assertThat( graph.getRootDevice().getIpv6Address() ).isNull();
		assertThat( graph.getRootDevice().getIpv4Address() ).isNull();
	}

}
