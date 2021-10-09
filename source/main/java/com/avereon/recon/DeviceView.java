package com.avereon.recon;

import com.avereon.data.NodeEvent;
import com.avereon.log.LazyEval;
import com.avereon.zarra.javafx.Fx;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import lombok.CustomLog;

@CustomLog
class DeviceView extends StackPane {

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
		expected.getStyleClass().add( "network-device-expected" );
		expected.setFocusTraversable( true );

		actual = new Circle( ACTUAL_STATE_SIZE, DeviceResponse.UNKNOWN.getPaint() );
		actual.setMouseTransparent( true );

		details = new DeviceDetailView( device );

		setAlignment( Pos.CENTER );
		getChildren().addAll( expected, actual );

		device.register( NodeEvent.ANY, e -> Fx.run( this::updateState ) );

		expected.addEventHandler( MouseEvent.MOUSE_PRESSED, e -> {
			//log.log( Log.WARN, "mouse pressed" );
			if( e.getClickCount() >= 2 ) {
				toggleTheDetails();
			} else {
				expected.requestFocus();
			}
		} );
		expected.addEventFilter( KeyEvent.KEY_PRESSED, e -> {
			log.atWarn().log( "%s pressed", LazyEval.of( e::getCode ) );
			switch( e.getCode() ) {
				case INSERT -> {
					if( e.isShiftDown() ) {
						getDevice().addDevice( new NetworkDevice().setName( "New Device" ).setHost( "<host>" ) );
					} else {
						NetworkDevice peer = getDevice();
						NetworkDevice parent = peer.getParent();
						parent.addDevice( new NetworkDevice().setGroup( peer.getGroup() ).setName( "New Device" ).setHost( "<host>" ) );
					}
				}
				case DELETE -> {
					com.avereon.data.Node parent = getDevice().getParent();
					if( parent != null && !(parent instanceof NetworkGraph) ) ((NetworkDevice)parent).removeDevice( device );
				}
				case DIGIT0 -> getDevice().setExpected( DeviceResponse.OFFLINE );
				case DIGIT1 -> getDevice().setExpected( DeviceResponse.ONLINE );
			}
		} );

		// This one sets the correct initial location
		boundsInParentProperty().addListener( ( v, o, n ) -> updateDetailsViewLocation() );
		// This one sets the correct location as the view moves around
		localToSceneTransformProperty().addListener( ( v, o, n ) -> updateDetailsViewLocation() );

		updateState();
	}

	private void updateDetailsViewLocation() {
		if( getParent() == null || details.getParent() == null ) return;
		Bounds bounds = getBoundsInParent();
		Point2D point = details.getParent().sceneToLocal( getParent().localToScene( bounds.getMaxX(), bounds.getMinY() ) );
		details.relocate( point.getX(), point.getY() );
	}

	private void toggleTheDetails() {
		details.setVisible( !details.isVisible() );
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
