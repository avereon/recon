package com.avereon.recon;

import com.avereon.util.Log;
import com.avereon.xenon.Mod;
import com.avereon.xenon.ToolRegistration;

public class Recon extends Mod {

	private static final System.Logger log = Log.log();

	private NetworkGraphAssetType networkGraphAssetType;

	@Override
	public void register() {
		registerIcon( "recon", ReconIcon.class );
		registerAssetType( networkGraphAssetType = new NetworkGraphAssetType( this ) );
		registerTool( networkGraphAssetType, new ToolRegistration( this, ReconTool.class ) );

		registerAction( this.rb(), "runpause" );
	}

	@Override
	public void startup() {}

	@Override
	public void shutdown() {}

	@Override
	public void unregister() {
		unregisterAction( "runpause" );

		unregisterTool( networkGraphAssetType, ReconTool.class );
		unregisterAssetType( networkGraphAssetType );
		unregisterIcon( "recon", ReconIcon.class );
	}

}
