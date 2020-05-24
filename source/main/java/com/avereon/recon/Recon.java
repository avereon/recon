package com.avereon.recon;

import com.avereon.util.Log;
import com.avereon.xenon.Mod;
import com.avereon.xenon.ToolRegistration;

public class Recon extends Mod {

	private static final System.Logger log = Log.get();

	private NetworkGraphAssetType networkGraphAssetType;

	@Override
	public void register() {}

	@Override
	public void startup() {
		registerIcon( "recon", new ReconIcon() );
		registerAssetType( networkGraphAssetType = new NetworkGraphAssetType( this ) );
		registerTool( networkGraphAssetType, new ToolRegistration( this, ReconTool.class ) );

		registerAction( this.rb(), "runpause" );
	}

	@Override
	public void shutdown() {
		unregisterAction( "runpause" );

		unregisterTool( networkGraphAssetType, ReconTool.class );
		unregisterAssetType( networkGraphAssetType );
		unregisterIcon( "recon", new ReconIcon() );
	}

	@Override
	public void unregister() {}

}
