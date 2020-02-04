module com.averon.recon {

	requires com.avereon.xenon;
	opens com.avereon.recon.bundles;
	exports com.avereon.recon to com.avereon.xenon, com.avereon.venza;
	provides com.avereon.xenon.Mod with com.avereon.recon.Recon;

}