package com.avereon.recon;

import com.avereon.xenon.Action;
import javafx.event.ActionEvent;

class RunPauseAction extends Action {

	private ReconTool tool;

	RunPauseAction( ReconTool tool ) {
		super( tool.getProgram() );
		this.tool = tool;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void handle( ActionEvent event ) {
		if( tool.isRunning() ) {
			tool.stop();
		} else {
			tool.start();
		}
	}

}
