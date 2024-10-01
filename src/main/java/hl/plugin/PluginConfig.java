package hl.plugin;

public class PluginConfig {
	
	//
	protected String DEF_PLUGIN_PROP_FILENAME 	= "plugin.properties";
	protected String DEF_PLUGIN_PROPKEY_PREFIX 	= "plugin.";
	//
	protected String prop_filename 				= DEF_PLUGIN_PROP_FILENAME;
	protected String propkey_prefix 			= DEF_PLUGIN_PROPKEY_PREFIX;	
	//
	public PluginConfig()
	{
	}
	
	protected void setDefaultPropKeyPrefix(String aDefPropKeyPrefix)
	{
		this.DEF_PLUGIN_PROPKEY_PREFIX = aDefPropKeyPrefix;
	}
	
	protected void setDefaultPluginPropFileName(String aDefFaileName)
	{
		this.DEF_PLUGIN_PROP_FILENAME = aDefFaileName;
	}
	//
	public void reset()
	{
		this.prop_filename 		= DEF_PLUGIN_PROP_FILENAME;
		this.propkey_prefix 	= DEF_PLUGIN_PROPKEY_PREFIX;
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