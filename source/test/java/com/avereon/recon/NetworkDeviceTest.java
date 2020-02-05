package com.avereon.recon;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class NetworkDeviceTest {

	@Test
	void testNetworkDeviceProperties() {
		NetworkDevice device = new NetworkDevice().setName( "device" ).setIpv4Address( "0.0.0.0" ).setIpv6Address( "::" );

		assertThat( device.getName(), is( "device" ) );
		assertThat( device.getIpv6Address(), is( "::" ) );
		assertThat( device.getIpv4Address(), is( "0.0.0.0" ) );
	}

}
