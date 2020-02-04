package com.avereon.recon;

import com.avereon.product.Product;
import com.avereon.xenon.asset.AssetType;

public class ReconAssetType extends AssetType {

	private String mediaType = "application/vnd.avereon.recon";

	public ReconAssetType( Product product ) {
		super( product, "recon" );
	}

	@Override
	public String getKey() {
		return mediaType;
	}

}
