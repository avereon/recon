package com.avereon.recon;

import com.avereon.xenon.Module;
import com.avereon.xenon.ToolRegistration;
import lombok.CustomLog;

@CustomLog
public class Recon extends Module {

	private NetworkGraphAssetType networkGraphAssetType;

	@Override
	public void register() {}

	@Override
	public void startup() {
		registerIcon( "recon", new ReconIcon() );
		registerAssetType( networkGraphAssetType = new NetworkGraphAssetType( this ) );
		registerTool( networkGraphAssetType, new ToolRegistration( this, ReconTool.class ) );

		registerAction( this, "runpause" );
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
