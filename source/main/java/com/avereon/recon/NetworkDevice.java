package com.avereon.recon;

import com.avereon.data.Node;
import com.avereon.util.ThreadUtil;
import javafx.scene.shape.Shape;
import lombok.CustomLog;

import java.io.IOException;
import java.net.*;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@CustomLog
public class NetworkDevice extends Node {

	private static final String ID = "id";

	private static final String TYPE = "type";

	public static final String GROUP = "group";

	public static final String NAME = "name";

	public static final String HOST = "host";

	public static final String IPV4 = "ipv4";

	public static final String IPV6 = "ipv6";

	private static final String EXPECTED = "expected";

	private static final String RESPONSE = "response";

	private static final String ROOT = "root";

	private static final String CONNECTOR = "connector";

	private static final String GROUP_VIEW = "group-view";

	public static final String MESSAGE = "message";

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
		setMessage( DeviceResponse.UNKNOWN.name() );
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

	public String getMessage() {
		return getValue( MESSAGE );
	}

	public NetworkDevice setMessage( String message ) {
		setValue( MESSAGE, message );
		return this;
	}

	public GroupView getGroupView() {
		return getValue( GROUP_VIEW );
	}

	public NetworkDevice setGroupView( GroupView groupView ) {
		setValue( GROUP_VIEW, groupView );
		return this;
	}

	public Shape getConnector() {
		return getValue( CONNECTOR );
	}

	public NetworkDevice setConnector( Shape connector ) {
		setValue( CONNECTOR, connector );
		return this;
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
		if( System.currentTimeMillis() - lastUpdateTime < maxUpdateRate ) return;

		int count = 1;
		while( count <= retryCount && !updateStatus( retryCount, count ) ) {
			ThreadUtil.pause( retryDelay, retryUnit );
			count++;
		}
	}

	@Override
	public String toString() {
		return getGroup() + "/" + getName();
	}

	private boolean updateStatus( int attemptLimit, int attemptCount ) {
		try {
			if( !isRoot() && ((NetworkDevice)getParent()).getResponse() != DeviceResponse.ONLINE ) {
				setResponse( DeviceResponse.UNKNOWN );
				return true;
			} else {
				boolean online = isReachable( 7, 22, 443, 3389 );
				DeviceResponse response = online ? DeviceResponse.ONLINE : DeviceResponse.OFFLINE;

				if( online ) {
					try {
						setIpv6Address( Inet6Address.getByName( getHost() ).getHostAddress() );
						setIpv4Address( Inet4Address.getByName( getHost() ).getHostAddress() );
					} catch( UnknownHostException exception ) {
						// Intentionally ignore exception
					}
				}

				if( response == getExpected() ) {
					setResponse( response );
					setMessage( response.name() );
					return true;
				} else {
					if( attemptCount == attemptLimit ) setResponse( response );
				}
			}
		} catch( IOException exception ) {
			log.atDebug().withCause( exception ).log();
			return false;
		} finally {
			lastUpdateTime = System.currentTimeMillis();
		}

		return false;
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
		int timeout = 250;
		try {
			InetAddress address = InetAddress.getByName( getHost() );

			try {
				if( address.isReachable( timeout ) ) return true;
			} catch( IOException exception ) {
				if( ports.length == 0 ) {
					setMessage( exception.getMessage().toLowerCase() );
					throw exception;
				}
			}

			for( int port : ports ) {
				try( Socket socket = new Socket() ) {
					socket.connect( new InetSocketAddress( address, port ), timeout );
					setMessage( "connected" );
					return true;
				} catch( SocketTimeoutException exception ) {
					log.atDebug().log( "%s connection timeout", this );
					setMessage( "connection timeout" );
				} catch( ConnectException exception ) {
					log.atWarn().log( "%s %s", this, exception.getMessage().toLowerCase() );
					setMessage( exception.getMessage().toLowerCase() );
				}
			}
		} catch( UnknownHostException exception ) {
			log.atDebug().log( "%s unknown host", this );
			setMessage( "unknown host" );
		}

		return false;
	}

}
