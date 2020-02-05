package com.avereon.recon;

import org.junit.jupiter.api.Test;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class NetworkGraphTest {

	@Test
	void testNetworkGraph() throws Exception {
		NetworkGraph graph = new NetworkGraph();
		assertThat( graph.getRootDevice().getName(), is( InetAddress.getLocalHost().getHostName() ) );
		assertThat( graph.getRootDevice().getIpv6Address(), is( Inet6Address.getLocalHost().getHostAddress() ) );
		assertThat( graph.getRootDevice().getIpv4Address(), is( Inet4Address.getLocalHost().getHostAddress() ) );
	}

}
