package com.avereon.recon;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public enum DeviceResponse {

	UNKNOWN( Color.GRAY ),
	OFFLINE( Color.YELLOW ),
	ONLINE( Color.GREEN ),
	OFF( Color.BLACK );

	private Paint paint;

	DeviceResponse( Paint paint ) {
		this.paint = paint;
	}

	public Paint getPaint() {
		return paint;
	}

}
