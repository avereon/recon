package com.avereon.recon;

import com.avereon.util.Log;
import com.avereon.xenon.Mod;
import com.avereon.xenon.ToolRegistration;

public class Recon extends Mod {

	private static final System.Logger log = Log.log();

	private ReconAssetType reconAssetType;

	@Override
	public void register() {
		registerIcon( "recon", ReconIcon.class );
		registerAssetType( reconAssetType = new ReconAssetType( this ) );
		registerTool( reconAssetType, new ToolRegistration( this, ReconTool.class ) );

		registerAction( this.rb(), "runpause" );
	}

	@Override
	public void startup() {}

	@Override
	public void shutdown() {}

	@Override
	public void unregister() {
		unregisterAction( "runpause" );

		unregisterTool( reconAssetType, ReconTool.class );
		unregisterAssetType( reconAssetType );
		unregisterIcon( "recon", ReconIcon.class );
	}

}
