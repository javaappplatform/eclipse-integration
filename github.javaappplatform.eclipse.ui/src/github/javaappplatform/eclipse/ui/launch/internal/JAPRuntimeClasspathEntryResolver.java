package github.javaappplatform.eclipse.ui.launch.internal;

import github.javaappplatform.eclipse.ui.IPlugin;
import github.javaappplatform.eclipse.util.Tools;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver2;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.osgi.framework.Bundle;

/**
 * TODO javadoc
 * @author funsheep
 */
public class JAPRuntimeClasspathEntryResolver implements IRuntimeClasspathEntryResolver2
{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, ILaunchConfiguration configuration) throws CoreException
	{
		try
		{
			final Set<Bundle> includedBundles = PluginTools.computeIncludedBundles(configuration);
			IRuntimeClasspathEntry[] resolved = new IRuntimeClasspathEntry[includedBundles.size()];
			int i = 0;
			for (Bundle plugin : includedBundles)
				for (URL url : Tools.getIncludedJars(plugin))
				{
					IPath path = Tools.convertToPath(url);
					resolved[i++] = JavaRuntime.newArchiveRuntimeClasspathEntry(path);
				}
			System.err.println(Arrays.toString(resolved));
			return resolved;
		}
		catch (IOException | URISyntaxException e)
		{
			throw new CoreException(new Status(IStatus.ERROR, IPlugin.PLUGIN_ID, "Could not resolve classpath entry: " + entry, e));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, IJavaProject project) throws CoreException
	{
		return new IRuntimeClasspathEntry[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IVMInstall resolveVMInstall(IClasspathEntry entry) throws CoreException
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isVMInstallReference(IClasspathEntry entry)
	{
		return false;
	}

}
