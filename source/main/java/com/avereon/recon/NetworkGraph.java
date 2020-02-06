package com.avereon.recon;

import com.avereon.data.Node;

import java.net.InetAddress;
import java.util.UUID;

public class NetworkGraph extends Node {

	private static final String ID = "id";

	private static final String NAME = "name";

	private static final String ROOT = "root";

	public NetworkGraph() {
		definePrimaryKey( ID );
		defineNaturalKey( NAME );
		setId( UUID.randomUUID().toString() );
		setRootDevice( new NetworkDevice()
			.setName( "This Computer" )
			.setHost( InetAddress.getLoopbackAddress().getHostName() )
			.setExpected( DeviceResponse.ONLINE ) );

		NetworkDevice soderquistNet = new NetworkDevice().setName( "Soderquist Ventures" ).setHost( "soderquist.net" ).setExpected( DeviceResponse.ONLINE );
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
