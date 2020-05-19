package com.avereon.recon;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.TextAlignment;

public class NetworkGroupView extends BorderPane {

	private final Label name;

	public NetworkGroupView( String groupName ) {
		getStyleClass().addAll( "network-group-view" );
		this.name = new Label( groupName );
		this.name.setViewOrder( -1 );
		this.name.setTextAlignment( TextAlignment.CENTER );
		prefWidthProperty().bind( this.name.prefWidthProperty() );
		prefHeightProperty().bind( this.name.heightProperty() );
		setAlignment( this.name, Pos.CENTER );
		setTop( this.name );
	}

//	public String getName() {
//		return name.getText();
//	}

}
