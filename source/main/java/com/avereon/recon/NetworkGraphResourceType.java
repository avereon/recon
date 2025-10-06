package com.avereon.recon;

import com.avereon.xenon.Xenon;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.ResourceType;

public class NetworkGraphResourceType extends ResourceType {

	public NetworkGraphResourceType( XenonProgramProduct product ) {
		super( product, "recon" );
		setDefaultCodec( new NetworkGraphCodec( product ) );
	}

	@Override
	public String getKey() {
		return getDefaultCodec().getKey();
	}

	@Override
	public boolean assetOpen( Xenon program, Asset asset ) {
		asset.setModel( new NetworkGraph() );
		return true;
	}

}
