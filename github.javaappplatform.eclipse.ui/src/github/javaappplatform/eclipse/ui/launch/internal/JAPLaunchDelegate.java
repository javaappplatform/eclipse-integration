package github.javaappplatform.eclipse.ui.launch.internal;

import github.javaappplatform.eclipse.ui.launch.ExtensionsTab;
import github.javaappplatform.eclipse.ui.launch.ILaunchAPI;
import github.javaappplatform.eclipse.util.Tools;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.osgi.framework.Bundle;

/**
 * TODO javadoc
 * @author funsheep
 */
public class JAPLaunchDelegate extends JavaLaunchDelegate
{


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getProgramArguments(ILaunchConfiguration configuration) throws CoreException
	{
		StringBuilder sb = new StringBuilder(90);
		putStringAttribute(ILaunchAPI.ATTR_LOG_LEVEL, configuration, sb);
		putInternalAttribute(ILaunchAPI.ATTR_PA, configuration, sb);
		
		/******************** Add extensions parameter - should go somewhere else - *************/
		Set<String> excludedPlugins = ExtensionsTab.getExcluded(configuration, ILaunchAPI.ATTR_EXCLUDED_PLUGINS);
		Set<String> excludedExtensions = ExtensionsTab.getExcluded(configuration, ILaunchAPI.ATTR_EXCLUDED_EXTENSIONS);
		boolean first = true;
		for (Bundle bundle : Tools.searchForInstalledPlugins())
		{
			if (excludedPlugins.contains(bundle.getSymbolicName()))
				continue;
			
			for (String ext : Tools.bundleExtensions(bundle))
			{
				if (excludedExtensions.contains(ext))
					continue;
				
				if (first)
				{
					sb.append("-extensions ");
					sb.append(' ');
					first = false;
				}
				else
					sb.append(':');
				sb.append(ext);
			}
		}
		for (IProject project : Tools.searchForWorkspacePlugins())
		{
			if (excludedPlugins.contains(project.getName()))
				continue;
			
			for (String ext : Tools.projectExtensions(project))
			{
				if (excludedExtensions.contains(ext))
					continue;
				
				if (first)
				{
					sb.append("-extensions ");
					sb.append(' ');
					first = false;
				}
				else
					sb.append(':');
				sb.append(ext);
			}
		}

		return VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(sb.toString());
	}
	
	private static final void putInternalAttribute(String attribute, ILaunchConfiguration config, StringBuilder sb)
	{
		try
		{
			String value = config.getAttribute(attribute, (String) null);
			if (value != null && value.length() > 0)
			{
				sb.append(value);
				sb.append(' ');
			}
		} catch (CoreException e)
		{
			//do nothing
		}
	}

	private static final boolean putStringAttribute(String attribute, ILaunchConfiguration config, StringBuilder sb)
	{
		try
		{
			String value = config.getAttribute(attribute, (String) null);
			if (value != null && value.length() > 0)
			{
				sb.append(attribute);
				sb.append(' ');
				sb.append(value);
				sb.append(' ');
				return true;
			}
		} catch (CoreException e)
		{
			//do nothing
		}
		return false;
	}

//	private static final void putSwitch(String attribute, ILaunchConfiguration config, StringBuilder sb)
//	{
//		try
//		{
//			boolean value = config.getAttribute(attribute, false);
//			if (value)
//			{
//				sb.append(attribute);
//				sb.append(' ');
//			}
//		} catch (CoreException e)
//		{
//			//do nothing
//		}
//	}

}
