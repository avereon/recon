package com.avereon.recon;

import com.avereon.data.Node;
import com.avereon.util.Log;
import com.avereon.util.ThreadUtil;
import javafx.scene.shape.Shape;

import java.io.IOException;
import java.net.*;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class NetworkDevice extends Node {

	private static final System.Logger log = Log.get();

	private static final String ID = "id";

	private static final String TYPE = "type";

	private static final String NAME = "name";

	private static final String HOST = "host";

	private static final String IPV4 = "ipv4";

	private static final String IPV6 = "ipv6";

	private static final String EXPECTED = "expected";

	private static final String RESPONSE = "response";

	private static final String GROUP = "group";

	private static final String ROOT = "root";

	private static final String CONNECTOR = "connector";

	private long maxUpdateRate = 2500;

	private long lastUpdateTime;

	public NetworkDevice() {
		definePrimaryKey( ID );
		defineNaturalKey( TYPE, NAME );
		addModifyingKeys( ID, TYPE, NAME, HOST, GROUP, EXPECTED );
		setId( UUID.randomUUID().toString() );
		setGroup( "default" );
		setExpected( DeviceResponse.ONLINE );
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

	public String getGroup() {
		return getValue( GROUP );
	}

	public NetworkDevice setGroup( String group ) {
		setValue( GROUP, group );
		return this;
	}

	public NetworkDevice setConnector( Shape connector ) {
		setValue( CONNECTOR, connector );
		return this;
	}

	public Shape getConnector() {
		return getValue( CONNECTOR );
	}

	public NetworkDevice getDevice( String id ) {
		return getValue( id );
	}

	public void addDevice( NetworkDevice device ) {
		addModifyingKeys( device.getId() );
		setValue( device.getId(), device );
	}

	public void removeDevice( NetworkDevice device ) {
		setValue( device.getId(), null );
		removeModifyingKeys( device.getId() );
	}

	public void updateStatus( int retryCount, int retryDelay, TimeUnit retryUnit ) {
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

			if( !isRoot() && ((NetworkDevice)getParent()).getResponse() != DeviceResponse.ONLINE ) {
				setResponse( DeviceResponse.UNKNOWN );
			} else if( isReachable( 7, 22, 443, 3389 ) ) {
				setResponse( DeviceResponse.ONLINE );
			} else {
				setResponse( DeviceResponse.OFFLINE );
			}
		} catch( IOException exception ) {
			log.log( Log.DEBUG, exception );
			return false;
		} finally {
			lastUpdateTime = System.currentTimeMillis();
		}

		return true;
	}

	public Collection<NetworkDevice> getDevices() {
		return getValues( NetworkDevice.class );
	}

	public int getLevel() {
		int level = 0;
		Node node = getParent();
		while( !(node instanceof NetworkGraph) ) {
			level++;
			node = node.getParent();
		}
		return level;
	}

	public boolean isRoot() {
		Boolean isRoot = getValue( ROOT );
		if( isRoot != null ) return isRoot;

		Node parent = getParent();
		isRoot = parent instanceof NetworkGraph;
		setValue( ROOT, isRoot );
		return isRoot;
	}

	/**
	 * Walk the network device tree starting with this device. This device is
	 * processed by the consumer before child devices are processed.
	 *
	 * @param consumer The consumer to process each device
	 */
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
				log.log( Log.INFO, this + " connection timeout" );
			} catch( ConnectException exception ) {
				log.log( Log.INFO, this + " " + exception.getMessage().toLowerCase() );
			}
		}
		return false;
	}

}
