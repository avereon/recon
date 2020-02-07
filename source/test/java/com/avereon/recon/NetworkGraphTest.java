package com.avereon.recon;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

public class NetworkGraphTest {

	@Test
	void testNetworkGraph() throws Exception {
		NetworkGraph graph = new NetworkGraph();
		assertThat( graph.getRootDevice().getName(), is( "This Computer" ) );
		assertNull( graph.getRootDevice().getIpv6Address() );
		assertNull( graph.getRootDevice().getIpv4Address() );
	}

}
