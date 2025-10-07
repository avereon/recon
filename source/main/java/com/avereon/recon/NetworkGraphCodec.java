package com.avereon.recon;

import com.avereon.data.Node;
import com.avereon.product.Product;
import com.avereon.product.Rb;
import com.avereon.xenon.asset.Resource;
import com.avereon.xenon.asset.Codec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.CustomLog;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@CustomLog
public class NetworkGraphCodec extends Codec {

	static final String MEDIA_TYPE = "application/vnd.avereon.recon.network.graph";

	private final Product product;

	public NetworkGraphCodec( Product product ) {
		this.product = product;
		setDefaultExtension( "netgraph" );
		addSupported( Pattern.MEDIATYPE, MEDIA_TYPE );
	}

	@Override
	public String getKey() {
		return MEDIA_TYPE;
	}

	@Override
	public String getName() {
		return Rb.text( "asset", "codec-netgraph-name" );
	}

	@Override
	public boolean canLoad() {
		return true;
	}

	@Override
	public boolean canSave() {
		return true;
	}

	@Override
	public void load( Resource resource, InputStream input ) throws IOException {
		Map<String, NetworkDevice> devices = new HashMap<>();
		Map<String, String> parents = new HashMap<>();

		ObjectReader reader = new ObjectMapper().reader().forType( new TypeReference<Set<Map<String, String>>>() {} );
		Set<Map<String, String>> maps = reader.readValue( new InputStreamReader( input, StandardCharsets.UTF_8 ) );

		try {
			maps.forEach( v -> {
				String id = v.get( "id" );
				String group = v.get( "group" );
				String name = v.get( "name" );
				String host = v.get( "host" );
				String parent = v.get( "parent" );
				if( group == null ) group = "default";

				DeviceResponse expected;
				try {
					String exp = v.get( "expected" ).toUpperCase();
					if( "OFF".equals( exp ) ) exp = DeviceResponse.OFFLINE.name();
					expected = DeviceResponse.valueOf( exp );
				} catch( Throwable throwable ) {
					expected = DeviceResponse.UNKNOWN;
				}
				devices.put( id, new NetworkDevice().setId( id ).setGroup( group ).setName( name ).setHost( host ).setExpected( expected ) );
				parents.put( id, parent );
			} );

			NetworkDevice root = null;

			for( String id : parents.keySet() ) {
				String parentId = parents.get( id );
				NetworkDevice device = devices.get( id );
				if( "null".equals( parentId ) ) {
					root = device;
				} else {
					devices.get( parentId ).addDevice( device );
				}
			}

			if( root != null ) ((NetworkGraph)resource.getModel()).setRootDevice( root );
		} catch( Throwable throwable ) {
			throw new IOException( "Error loading asset", throwable );
		}
	}

	@Override
	public void save( Resource resource, OutputStream output ) throws IOException {
		NetworkGraph graph = resource.getModel();
		NetworkDevice root = graph.getRootDevice();

		Set<Map<String, String>> deviceMaps = new HashSet<>();
		root.walk( d -> {
			Node p = d.getParent();
			NetworkDevice parent = null;
			if( p instanceof NetworkDevice ) parent = (NetworkDevice)p;

			Map<String, String> map = new HashMap<>();
			map.put( "id", d.getId() );
			map.put( "group", d.getGroup() );
			map.put( "name", d.getName() );
			map.put( "host", d.getHost() );
			map.put( "expected", d.getExpected().name() );
			map.put( "parent", parent == null ? "null" : parent.getId() );
			deviceMaps.add( map );
		} );

		ObjectWriter writer = new ObjectMapper().writer( new DefaultPrettyPrinter() );
		writer.writeValue( new OutputStreamWriter( output, StandardCharsets.UTF_8 ), deviceMaps );
	}

}
