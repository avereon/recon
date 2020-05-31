package com.avereon.recon;

import com.avereon.data.NodeEvent;
import com.avereon.util.Log;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

class DeviceView extends StackPane {

	private static final System.Logger log = Log.get();

	static final double EXPECTED_STATE_SIZE = 10;

	private static final double ACTUAL_STATE_SIZE = 8;

	private final NetworkDevice device;

	private final Shape expected;

	private final Shape actual;

	private final DeviceDetailView details;

	public DeviceView( NetworkDevice device ) {
		this.device = device;

		getStyleClass().addAll( "network-device" );

		expected = new Circle( EXPECTED_STATE_SIZE, DeviceResponse.UNKNOWN.getPaint() );
		actual = new Circle( ACTUAL_STATE_SIZE, DeviceResponse.UNKNOWN.getPaint() );
		details = new DeviceDetailView( device );

		setAlignment( Pos.CENTER );
		getChildren().addAll( expected, actual );

		device.register( NodeEvent.ANY, e -> Platform.runLater( this::updateState ) );

		actual.setMouseTransparent( true );

		expected.setFocusTraversable( true );
		expected.addEventHandler( MouseEvent.MOUSE_PRESSED, e -> {
			if( e.getClickCount() >= 2 ) {
				toggleTheDetails();
			} else {
				expected.requestFocus();
			}
		} );
		expected.addEventHandler( KeyEvent.KEY_PRESSED, e -> {
			//log.log( Log.WARN, e.getCode() + " pressed" );
			switch( e.getCode() ) {
				case INSERT: {
					getDevice().addDevice( new NetworkDevice().setName( "New Device" ).setHost( "unknown" ) );
					break;
				}
				case DELETE: {
					com.avereon.data.Node parent = getDevice().getParent();
					if( parent != null && !(parent instanceof NetworkGraph) ) ((NetworkDevice)parent).removeDevice( device );
					break;
				}
				case DIGIT0: {
					getDevice().setExpected( DeviceResponse.OFFLINE );
					break;
				}
				case DIGIT1: {
					getDevice().setExpected( DeviceResponse.ONLINE );
					break;
				}
			}
		} );

		// NEXT Figure out how to get the details in the right spot
		localToSceneTransformProperty().addListener( ( v, o, n ) -> {
			if( details.getParent() == null ) return;
			Bounds deviceViewBounds = getLayoutBounds();
			log.log( Log.WARN, "DV bounds=" + deviceViewBounds );
			Point2D point = details.getParent().sceneToLocal( localToScene( deviceViewBounds.getMaxX(), deviceViewBounds.getCenterY() ) );
			details.relocate( point.getX(), point.getY() - 0.5 * details.getHeight() );
		} );

		updateState();
	}

	private void toggleTheDetails() {
		boolean visible = !details.isVisible();
		details.getParent().setVisible( visible );
		details.setVisible( visible );
		if( details.isVisible() ) details.requestFocus();
	}

	NetworkDevice getDevice() {
		return device;
	}

	DeviceDetailView getDetails() {
		return details;
	}

	private void updateState() {
		expected.setFill( getDevice().getExpected().getPaint() );
		actual.setFill( getDevice().getExpected().getPaint( getDevice().getResponse() ) );
	}

}
