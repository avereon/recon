package com.avereon.recon;

import com.avereon.data.Node;

public class NetworkDeviceGroup extends Node {

	public static final NetworkDeviceGroup DEFAULT = new NetworkDeviceGroup( "" );

	private static final String NAME = "name";

	public NetworkDeviceGroup(String name ) {
		if( name == null ) throw new NullPointerException( "Name cannot be null" );
		setName( name );
	}

	public String getName() {
		return getValue( NAME );
	}

	public NetworkDeviceGroup setName( String name ) {
		setValue( NAME, name );
		return this;
	}

}
