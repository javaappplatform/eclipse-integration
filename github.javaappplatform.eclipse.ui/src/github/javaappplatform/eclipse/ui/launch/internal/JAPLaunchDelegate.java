package github.javaappplatform.eclipse.ui.launch.internal;

import github.javaappplatform.eclipse.ui.launch.ILaunchAPI;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

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
		Set<String> extensions = PluginTools.computeIncludedExtensions(configuration);
		
		boolean first = true;
		for (String ext : extensions)
		{
			if (first)
			{
				sb.append("-extensions ");
				first = false;
			}
			else
				sb.append(':');
			sb.append(ext);
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
