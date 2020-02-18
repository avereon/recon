package com.avereon.recon;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.HashMap;
import java.util.Map;

public enum DeviceResponse {

	UNKNOWN( Color.GRAY ),
	OFFLINE( Color.BLACK ),
	ONLINE( Color.GREEN );

	private static Map<DeviceResponse, Map<DeviceResponse, Paint>> paints;

	private Paint paint;

	static {
		paints = new HashMap<>();
		register( UNKNOWN, UNKNOWN, Color.GRAY );
		register( UNKNOWN, OFFLINE, Color.YELLOW );
		register( UNKNOWN, ONLINE, Color.YELLOW );
		register( OFFLINE, UNKNOWN, Color.GRAY );
		register( OFFLINE, OFFLINE, Color.BLACK );
		register( OFFLINE, ONLINE, Color.YELLOW );
		register( ONLINE, UNKNOWN, Color.GRAY );
		register( ONLINE, OFFLINE, Color.RED );
		register( ONLINE, ONLINE, Color.GREEN );
	}

	DeviceResponse( Paint paint ) {
		this.paint = paint;
	}

	public Paint getPaint() {
		return paint;
	}

	public Paint getPaint( DeviceResponse expected ) {
		return paints.get( this ).get( expected );
	}

	private static void register( DeviceResponse expected, DeviceResponse actual, Paint paint ) {
		paints.computeIfAbsent( expected, ( k ) -> new HashMap<>() ).put( actual, paint );
	}

}
