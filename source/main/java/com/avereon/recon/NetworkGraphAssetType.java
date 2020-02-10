package com.avereon.recon;

import com.avereon.product.Product;
import com.avereon.xenon.Program;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetType;

public class NetworkGraphAssetType extends AssetType {

	static final String MEDIA_TYPE = "application/vnd.avereon.recon.network.graph";

	public NetworkGraphAssetType( Product product ) {
		super( product, "recon" );
		setDefaultCodec( new NetworkGraphCodec( product ) );
	}

	@Override
	public String getKey() {
		return MEDIA_TYPE;
	}

	@Override
	public boolean assetInit( Program program, Asset asset ) {
		asset.setModel( new NetworkGraph() );
		return true;
	}

}
