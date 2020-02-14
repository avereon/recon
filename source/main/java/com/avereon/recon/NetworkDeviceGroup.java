package com.avereon.recon;

import com.avereon.data.Node;

import java.util.UUID;

public class NetworkDeviceGroup extends Node {

	public static final NetworkDeviceGroup DEFAULT = new NetworkDeviceGroup( "" );

	private static final String ID = "id";

	private static final String NAME = "name";

	public NetworkDeviceGroup(String name ) {
		if( name == null ) throw new NullPointerException( "Name cannot be null" );
		definePrimaryKey( ID );
		defineNaturalKey( NAME );

		setId( UUID.randomUUID() );
		setName( name );
	}

	public UUID getId() {
		return getValue( ID );
	}

	public NetworkDeviceGroup setId( UUID id ) {
		setValue( ID, id );
		return this;
	}

	public String getName() {
		return getValue( NAME );
	}

	public NetworkDeviceGroup setName( String name ) {
		setValue( NAME, name );
		return this;
	}

}
