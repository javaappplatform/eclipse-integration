package github.javaappplatform.eclipse.project.classpath;

import github.javaappplatform.eclipse.util.Tools;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Bundle;


public class AppClasspathContainer implements IClasspathContainer
{
	
	public static final String ID = "github.javaappplatform.eclipse.project.classpath.AppClasspathContainer";

	private final IPath containerPath;
	private final Bundle[] plugins;
	private final IClasspathEntry[] entries;

	
	public AppClasspathContainer(IPath containerPath) throws IOException
	{
		this.containerPath = containerPath;
		this.plugins = collectAvailablePlugins(this.containerPath);
		try
		{
			List<IClasspathEntry> resolvedEntries = new LinkedList<IClasspathEntry>();
			for (Bundle plugin : this.plugins)
				for (URL url : Tools.getIncludedJars(plugin))
				{
					IPath path = convertToPath(url);
					resolvedEntries.add(JavaCore.newLibraryEntry(path, resolveSourceAttachment(path), null));
				}
			this.entries = resolvedEntries.toArray(new IClasspathEntry[resolvedEntries.size()]);
		} catch (CoreException | URISyntaxException e)
		{
			throw new IOException("Could not extract .jar files", e);
		}
	}
	
	
	public Bundle[] getUsedPlugins()
	{
		return this.plugins;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public IClasspathEntry[] getClasspathEntries()
	{
		return this.entries;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription()
	{
		return "Java Application Platform Library";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getKind()
	{
		return IClasspathContainer.K_APPLICATION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IPath getPath()
	{
		return this.containerPath;
	}

	
	private static Bundle[] collectAvailablePlugins(IPath containerPath)
	{
		if (containerPath.segmentCount() > 1)
		{
			Bundle[] bundles = new Bundle[containerPath.segmentCount()-1];
			for (int i = 0; i < containerPath.segmentCount()-1; i++)
				bundles[i] = Platform.getBundle(containerPath.segment(i+1));
			return bundles;
		}
		return Tools.searchForInstalledPlugins();
	}
	
	private static final IPath resolveSourceAttachment(IPath jar) throws CoreException
	{
		if (jar.segmentCount() > 1)
		{
			//here we have a third-party library
			//try to find src through name scheme
			if (jar.segment(jar.segmentCount() - 2).equals("3rd"))
			{
				String filename = jar.lastSegment().substring(0, jar.lastSegment().lastIndexOf('.'));
				filename = filename + "-src.zip";
				IPath src = jar.removeLastSegments(1).append(filename);
				
				IFileStore fileStore = EFS.getLocalFileSystem().getStore(src);
				if (!fileStore.fetchInfo().isDirectory() && fileStore.fetchInfo().exists())
				{
					return src;
				}
			}
			//here we have our own compiled library - source is found within
			else if (jar.segment(jar.segmentCount() - 2).equals("lib"))
			{
				return jar;
			}
		}
		return null;
	}
	
	private static final IPath convertToPath(URL url) throws IOException, CoreException, URISyntaxException
	{
		final URI fromuri = URIUtil.toURI(FileLocator.resolve(url));
		final IFileStore fromstore = EFS.getStore(fromuri);
		final File file = fromstore.toLocalFile(0, null);
		return Path.fromOSString(file.getCanonicalPath());
	}

}