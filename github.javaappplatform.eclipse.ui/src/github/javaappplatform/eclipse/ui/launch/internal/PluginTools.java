package github.javaappplatform.eclipse.ui.launch.internal;

import github.javaappplatform.eclipse.ui.launch.ILaunchAPI;
import github.javaappplatform.eclipse.util.Tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.osgi.framework.Bundle;

public class PluginTools
{

	public static final Set<String> getExcludedExtensions(ILaunchConfiguration configuration)
	{
		if (configuration == null)
			return Collections.<String>emptySet();
		return getExcluded(configuration, ILaunchAPI.ATTR_EXCLUDED_EXTENSIONS);
	}

	public static final Set<String> computeIncludedExtensions(ILaunchConfiguration configuration)
	{
		Set<String> excludedExtensions = getExcludedExtensions(configuration);

		HashSet<String> extensions = new HashSet<>();
		for (Object plugin : computeIncludedPlugins(configuration))
		{
			for (String ext : Tools.extensionsFor(plugin))
			{
				if (!excludedExtensions.contains(ext))
					extensions.add(ext);
			}
		}
		return extensions;
	}
	

	public static final Set<String> getExcludedPlugins(ILaunchConfiguration configuration)
	{
		if (configuration == null)
			return Collections.<String>emptySet();
		return getExcluded(configuration, ILaunchAPI.ATTR_EXCLUDED_PLUGINS);
	}

	public static final Set<Object> computeIncludedPlugins(ILaunchConfiguration configuration)
	{
		HashSet<Object> plugins = new HashSet<Object>(computeIncludedBundles(configuration));
		plugins.addAll(computeIncludedProjects(configuration));
		return plugins;
	}

	public static final Set<Bundle> computeIncludedBundles(ILaunchConfiguration configuration)
	{
		Set<String> excludedPlugins = getExcludedPlugins(configuration);
		ArrayList<Bundle> list = new ArrayList<>(Arrays.asList(Tools.searchForInstalledPlugins()));

		Set<Bundle> plugins = new HashSet<>();
		for (Bundle o : list)
			if (!excludedPlugins.contains(getNameFor(o)))
				plugins.add(o);
		return plugins;
	}
	
	public static final Set<IProject> computeIncludedProjects(ILaunchConfiguration configuration)
	{
		Set<String> excludedPlugins = getExcludedPlugins(configuration);
		ArrayList<IProject> list = new ArrayList<>(Arrays.asList(Tools.searchForWorkspacePlugins()));

		Set<IProject> plugins = new HashSet<>();
		for (IProject o : list)
			if (!excludedPlugins.contains(getNameFor(o)))
				plugins.add(o);
		return plugins;
	}
	
	private static final String getNameFor(Object o)
	{
		if (o instanceof Bundle)
			return ((Bundle) o).getSymbolicName();
		else if (o instanceof IProject)
			return ((IProject) o).getName();
		return String.valueOf(o);
	}

	@SuppressWarnings("unchecked")
	public static final Set<String> getExcluded(ILaunchConfiguration config, String attrID)
	{
		Set<String> excluded = Collections.<String>emptySet();
		try
		{
			excluded = config.getAttribute(attrID, Collections.EMPTY_SET);
		}
		catch (CoreException e)
		{
			//ignore
		}
		return excluded;
	}


	private PluginTools()
	{
		//no instance
	}

}
