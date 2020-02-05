package com.avereon.recon;

import com.avereon.product.Product;
import com.avereon.xenon.Program;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetType;

public class ReconAssetType extends AssetType {

	private static final String MEDIA_TYPE = "application/vnd.avereon.recon.network.graph";

	public ReconAssetType( Product product ) {
		super( product, "recon" );
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
