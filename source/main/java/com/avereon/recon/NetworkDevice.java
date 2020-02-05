package com.avereon.recon;

import com.avereon.data.Node;

import java.util.UUID;

public class NetworkDevice extends Node {

	private static final String ID = "id";

	private static final String NAME = "name";

	private static final String TYPE = "type";

	private static final String IPV4 = "ipv4";

	private static final String IPV6 = "ipv6";

	public NetworkDevice() {
		definePrimaryKey( ID );
		defineNaturalKey( TYPE, NAME );
		setId( UUID.randomUUID().toString() );
	}

	public String getId() {
		return getValue(ID);
	}

	public NetworkDevice setId( String id ) {
		setValue( ID, id );
		return this;
	}

	public String getName() {
		return getValue( NAME );
	}

	public NetworkDevice setName( String name ) {
		setValue( NAME, name );
		return this;
	}

	public String getAddress() {
		return getValue( IPV6, getValue( IPV4 ) );
	}

	public String getIpv6Address() {
		return getValue( IPV6 );
	}

	public NetworkDevice setIpv6Address( String address ) {
		setValue( IPV6, address );
		return this;
	}

	public String getIpv4Address() {
		return getValue( IPV4 );
	}

	public NetworkDevice setIpv4Address( String address ) {
		setValue( IPV4, address );
		return this;
	}

	public NetworkDevice get( String id ) {
		return getValue( id );
	}

	public NetworkDevice add( NetworkDevice device ) {
		setValue( device.getId(), device );
		return this;
	}

}
