package com.avereon.recon;

import com.avereon.data.Node;
import com.avereon.product.Product;
import com.avereon.util.Log;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.Codec;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class NetworkGraphCodec extends Codec {

	private static final String KEY = NetworkGraphAssetType.MEDIA_TYPE + "/netgraph";

	private static final System.Logger log = Log.log();

	private Product product;

	public NetworkGraphCodec( Product product) {
		this.product = product;
		setDefaultExtension( "netgraph" );
	}

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return product.rb().text( "asset", "codec-netgraph-name" );
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
	public void load( Asset asset, InputStream input ) throws IOException {

	}

	@Override
	public void save( Asset asset, OutputStream output ) throws IOException {
		PrintWriter writer = new PrintWriter( new OutputStreamWriter( output, StandardCharsets.UTF_8 ) );
		try {
			NetworkGraph graph = asset.getModel();
			graph.getRootDevice().walk( d -> {
				log.log( Log.WARN, "device.id=" + d.getId() );

				Node p = d.getParent();
				NetworkDevice parent = null;
				if( p instanceof NetworkDevice ) parent = (NetworkDevice)p;

				writer.print( d.getId() );
				writer.print( ", " );
				writer.print( d.getName() );
				writer.print( ", " );
				writer.print( d.getHost() );
				writer.print( ", " );
				writer.print( parent == null ? "null" : parent.getId() );
				writer.println();
			} );
		} catch( Throwable throwable ) {
			log.log( Log.ERROR, throwable );
		}
		writer.flush();
	}

}
