package com.avereon.recon;

import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetType;

public class NetworkGraphAssetType extends AssetType {

	public NetworkGraphAssetType( ProgramProduct product ) {
		super( product, "recon" );
		setDefaultCodec( new NetworkGraphCodec( product ) );
	}

	@Override
	public String getKey() {
		return getDefaultCodec().getKey();
	}

	@Override
	public boolean assetInit( Program program, Asset asset ) {
		asset.setModel( new NetworkGraph() );
		return true;
	}

}
