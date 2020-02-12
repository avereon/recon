package com.avereon.recon;

import com.avereon.data.Node;
import com.avereon.util.Log;
import com.avereon.util.ThreadUtil;

import java.io.IOException;
import java.net.*;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class NetworkDevice extends Node {

	private static final System.Logger log = Log.log();

	private static final String ID = "id";

	private static final String NAME = "name";

	private static final String TYPE = "type";

	private static final String HOST = "host";

	private static final String IPV4 = "ipv4";

	private static final String IPV6 = "ipv6";

	public static final String REQUEST = "request";

	public static final String EXPECTED = "expected";

	public static final String RESPONSE = "response";

	private long maxUpdateRate = 2500;

	private long lastUpdateTime;

	public NetworkDevice() {
		definePrimaryKey( ID );
		defineNaturalKey( TYPE, NAME );
		setId( UUID.randomUUID().toString() );
		setRequest( DeviceRequest.RUNNING );
		setExpected( DeviceResponse.OFF );
		setResponse( DeviceResponse.UNKNOWN );
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

	public String getHost() {
		return getValue( HOST );
	}

	public NetworkDevice setHost( String host ) {
		setValue( HOST, host );
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

	public void addDevice( NetworkDevice device ) {
		setValue( device.getId(), device );
	}

	public void removeDevice( NetworkDevice device ) {
		setValue( device.getId(), null );
	}

	public void updateStatus( int retryCount, int retryDelay, TimeUnit retryUnit) {
		int count = 0;
		while( count < retryCount && !updateStatus() ) {
			ThreadUtil.pause( retryDelay, retryUnit );
			count++;
		}
	}

	public boolean updateStatus() {
		if( System.currentTimeMillis() - lastUpdateTime < maxUpdateRate ) return true;

		try {
			setIpv6Address( Inet6Address.getByName( getHost() ).getHostAddress() );
			setIpv6Address( Inet4Address.getByName( getHost() ).getHostAddress() );

			if( isReachable( 7, 22, 443 ) ) {
				setResponse( DeviceResponse.ONLINE );
			} else {
				if( getExpected() == DeviceResponse.OFF ) {
					setResponse( DeviceResponse.OFF );
				} else {
					setResponse( DeviceResponse.OFFLINE );
				}
			}
		} catch( IOException exception ) {
			log.log( Log.DEBUG, exception );
			return false;
		} finally {
			//log.log( Log.INFO, getName() + " is " + getResponse() + "!" );
			lastUpdateTime = System.currentTimeMillis();
		}

		return true;
	}

	public Collection<NetworkDevice> getDevices() {
		return getValues( NetworkDevice.class );
	}

	public void walk( Consumer<NetworkDevice> consumer ) {
		consumer.accept( this );
		getDevices().forEach( d -> d.walk( consumer ) );
	}

	private boolean isReachable( int... ports ) throws IOException {
		int timeout = 200;
		InetAddress address = InetAddress.getByName( getHost() );

		try {
			if( address.isReachable( timeout ) ) return true;
		} catch( IOException exception ) {
			if( ports.length == 0 ) throw exception;
		}

		for( int port : ports ) {
			try( Socket socket = new Socket() ) {
				socket.connect( new InetSocketAddress( address, port ), timeout );
				return true;
			} catch( SocketTimeoutException exception ) {
				// Intentionally ignore exception
			}
		}
		return false;
	}

}
