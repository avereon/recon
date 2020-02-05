package com.avereon.recon;

import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.workpane.ToolException;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

public class ReconTool extends ProgramTool {

	private NetworkGraphPane networkGraphPane;

	public ReconTool( ProgramProduct product, Asset asset ) {
		super( product, asset );

		setGraphic( product.getProgram().getIconLibrary().getIcon( "recon" ) );

		getChildren().addAll( networkGraphPane = new NetworkGraphPane() );
	}

	@Override
	protected void assetReady( OpenAssetRequest request ) throws ToolException {
		networkGraphPane.getChildren().add( new NetworkDeviceNode( getGraph().getRootDevice() ) );
	}

	private NetworkGraph getGraph() {
		return getAsset().getModel();
	}

	private class NetworkGraphPane extends Pane {}

	private class NetworkDeviceNode extends VBox {

		private NetworkDevice device;

		private Shape shape;

		private Label name;

		private Label address;

		public NetworkDeviceNode( NetworkDevice device ) {
			this.device = device;

			this.shape = new Circle( 50, Color.GREEN );
			this.name = new Label( device.getName() );
			this.address = new Label( device.getAddress() );

			setAlignment( Pos.CENTER );
			getChildren().addAll( shape, name, address );
		}

		NetworkDevice getDevice() {
			return device;
		}

	}

}
