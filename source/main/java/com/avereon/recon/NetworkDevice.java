package com.avereon.recon;

import com.avereon.data.Node;
import com.avereon.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;
import java.util.function.Consumer;

public class NetworkDevice extends Node {

	private static final System.Logger log = Log.log();

	private static final String ID = "id";

	private static final String NAME = "name";

	private static final String TYPE = "type";

	private static final String IPV4 = "ipv4";

	private static final String IPV6 = "ipv6";

	public static final String REQUEST = "request";

	public static final String EXPECTED = "expected";

	public static final String RESPONSE = "response";

	public NetworkDevice() {
		definePrimaryKey( ID );
		defineNaturalKey( TYPE, NAME );
		setId( UUID.randomUUID().toString() );
		setRequest( DeviceRequest.RUNNING );
		setExpected( DeviceResponse.OFF );
		setResponse( DeviceResponse.UNKNOWN );
	}

	public NetworkDevice getParent(){
		return getParent();
	}

	public String getId() {
		return getValue( ID );
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

	public DeviceRequest getRequest() {
		return getValue( REQUEST );
	}

	public NetworkDevice setRequest( DeviceRequest request ) {
		setValue( REQUEST, request );
		return this;
	}

	public DeviceResponse getExpected() {
		return getValue( EXPECTED );
	}

	public NetworkDevice setExpected( DeviceResponse expected ) {
		setValue( EXPECTED, expected );
		return this;
	}

	public DeviceResponse getResponse() {
		return getValue( RESPONSE );
	}

	public NetworkDevice setResponse( DeviceResponse response ) {
		setValue( RESPONSE, response );
		return this;
	}

	public NetworkDevice getDevice( String id ) {
		return getValue( id );
	}

	public NetworkDevice addDevice( NetworkDevice device ) {
		setValue( device.getId(), device );
		return this;
	}

	public void updateStatus( int timeout ) {
		try {
			log.log( Log.INFO, "Checking " + getName() + "..." );
			setResponse( DeviceResponse.ONLINE );
			InetAddress address = InetAddress.getByName( getName() );
			if( address.isReachable( timeout ) ) {
				setResponse( DeviceResponse.ONLINE );
			} else {
				if( getExpected() == DeviceResponse.OFF ) {
					setResponse( DeviceResponse.OFF );
				} else {
					setResponse( DeviceResponse.OFFLINE );
				}
			}
		} catch( IOException exception ) {
			log.log( Log.WARN, exception );
		}
	}

	public void walk( Consumer<NetworkDevice> consumer ) {
		consumer.accept( this );
		getValueKeys().forEach( k -> {
			Object value = getValue( k );
			if( value instanceof NetworkDevice ) ((NetworkDevice)value).walk( consumer );
		} );
	}

}
