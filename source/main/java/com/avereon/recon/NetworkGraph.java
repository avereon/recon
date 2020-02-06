package com.avereon.recon;

import com.avereon.data.Node;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

public class NetworkGraph extends Node {

	private static final String ID = "id";

	private static final String NAME = "name";

	private static final String ROOT = "root";

	public NetworkGraph() {
		definePrimaryKey( ID );
		defineNaturalKey( NAME );
		setId( UUID.randomUUID().toString() );

		try {
			InetAddress ipv6Host = Inet6Address.getLocalHost();
			InetAddress ipv4Host = Inet4Address.getLocalHost();
			setRootDevice( new NetworkDevice()
				.setName( ipv6Host.getHostName() )
				.setHost( ipv6Host.getHostName() )
				.setIpv6Address( ipv6Host.getHostAddress() )
				.setIpv4Address( ipv4Host.getHostAddress() ) );
		} catch( UnknownHostException e ) {
			e.printStackTrace();
		}

		NetworkDevice soderquistNet = new NetworkDevice().setName("Soderquist Ventures").setHost( "soderquist.net" );
		getRootDevice().addDevice( soderquistNet );
	}

	public String getId() {
		return getValue( ID );
	}

	public NetworkGraph setId( String id ) {
		setValue( ID, id );
		return this;
	}

	public String getName() {
		return getValue( NAME );
	}

	public NetworkGraph setName( String name ) {
		setValue( NAME, name );
		return this;
	}

	public NetworkDevice getRootDevice() {
		return getValue( ROOT );
	}

	public NetworkGraph setRootDevice( NetworkDevice root ) {
		setValue( ROOT, root );
		return this;
	}

}
