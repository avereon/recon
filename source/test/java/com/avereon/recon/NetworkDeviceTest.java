package com.avereon.recon;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NetworkDeviceTest {

	@Test
	void testNetworkDeviceProperties() {
		NetworkDevice device = new NetworkDevice().setName( "device" ).setIpv4Address( "0.0.0.0" ).setIpv6Address( "::" );

		assertThat( device.getName()).isEqualTo( "device"  );
		assertThat( device.getIpv6Address()).isEqualTo( "::"  );
		assertThat( device.getIpv4Address()).isEqualTo( "0.0.0.0"  );
		assertThat( device.getGroup()).isEqualTo( "default"  );
	}

}
