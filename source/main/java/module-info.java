module com.avereon.recon {

	requires com.avereon.xenon;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	opens com.avereon.recon.bundles;
	exports com.avereon.recon to com.avereon.xenon, com.avereon.venza;
	provides com.avereon.xenon.Mod with com.avereon.recon.Recon;

}