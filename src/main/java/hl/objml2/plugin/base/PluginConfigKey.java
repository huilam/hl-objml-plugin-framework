package hl.objml2.plugin.base;

public class PluginConfigKey {
	
	//
	protected String DEF_PLUGIN_PROP_FILENAME 	= "plugin.properties";
	protected String DEF_PLUGIN_PROPKEY_PREFIX 	= "plugin.";
	//
	protected String prop_filename 				= DEF_PLUGIN_PROP_FILENAME;
	protected String propkey_prefix 			= DEF_PLUGIN_PROPKEY_PREFIX;
	//
	public PluginConfigKey()
	{
	}
	//
	public String getProp_filename() {
		return prop_filename;
	}
	public void setProp_filename(String prop_filename) {
		this.prop_filename = prop_filename;
	}
	//
	public String getPropkey_prefix() {
		return propkey_prefix;
	}
	public void setPropkey_prefix(String propkey_prefix) {
		this.propkey_prefix = propkey_prefix;
	}
}