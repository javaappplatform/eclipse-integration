package github.javaappplatform.eclipse.util;

import github.javaappplatform.eclipse.Activator;
import github.javaappplatform.eclipse.project.builder.Nature;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.URIUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class Tools
{

	public static final IProject[] searchForWorkspacePlugins()
	{
		ArrayList<IProject> search = new ArrayList<>();
		try
		{
			for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects())
			{
				if (project.isOpen() && project.hasNature(Nature.NATURE_ID))
					search.add(project);
			}
		}
		catch (CoreException e)
		{
			//ignore
			e.printStackTrace();
		}
		return search.toArray(new IProject[search.size()]);
	}

	public static final Bundle[] searchForInstalledPlugins()
	{
		ArrayList<Bundle> search = new ArrayList<Bundle>();

		try
		{
			ServiceReference<?>[] refs = Activator.getContext().getAllServiceReferences(Object.class.getName(), "(AppPlugin=true)");
			if (refs != null)
			{
				for (ServiceReference<?> ref : refs)
				{
					search.add(ref.getBundle());
				}
			}
		} catch (InvalidSyntaxException e)
		{
			//does not happen
		}
		return search.toArray(new Bundle[search.size()]);
	}

	public static final Set<String> extensionsFor(Object o)
	{
		if (o instanceof IProject)
			return projectExtensions((IProject) o);
		return bundleExtensions((Bundle) o);
	}

	public static final Set<String> projectExtensions(IProject project)
	{
		try
		{
			if (!project.hasNature(Nature.NATURE_ID))
				return Collections.emptySet();
			return ((Nature) project.getNature(Nature.NATURE_ID)).extensionNames();
		}
		catch (CoreException e)
		{
			return Collections.emptySet();
		}
	}

	private static final HashMap<Bundle, Set<String>> FOUND_EXTS = new HashMap<>();
	public static synchronized final Set<String> bundleExtensions(Bundle bundle)
	{
		Set<String> exts = FOUND_EXTS.get(bundle);
		if (exts == null)
		{
			URL[] us = getIncludedJars(bundle);
			exts = new HashSet<>(us.length);
			for (URL u : us)
			{
				try
				{
				searchJar(u.openStream(), exts);
				}
				catch (IOException e)
				{
					return Collections.emptySet();
				}
			}
			FOUND_EXTS.put(bundle, exts);
		}
		return Collections.unmodifiableSet(exts);
	}

	private static final void searchJar(InputStream stream, Set<String> found)
	{
		try
		{
			ZipInputStream zip = new ZipInputStream(stream);
			ZipEntry entry = null;
			while ((entry = zip.getNextEntry()) != null)
			{
				if (entry.getName().endsWith(".ext"))
				{
					String extEntry = entry.getName().substring(0, entry.getName().length()-4).replace('/', '.');
					found.add(extEntry);
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	
	private static final HashMap<String, URL[]> FOUND_JARS = new HashMap<>();
	public static synchronized final URL[] getIncludedJars(Bundle b)
	{
		try
		{
			URL[] urls = FOUND_JARS.get(b.getSymbolicName());
			if (urls == null)
			{
				urls = collectJars(b);
				FOUND_JARS.put(b.getSymbolicName(), urls);
			}
			URL[] ret = new URL[urls.length];
			System.arraycopy(urls, 0, ret, 0, urls.length);
			return ret;
		}
		catch (IOException | CoreException | URISyntaxException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	private static final URL[] collectJars(Bundle bundle) throws IOException, CoreException, URISyntaxException
	{
		ArrayList<URL> paths = new ArrayList<URL>();
		if (bundle != null)
		{
			Enumeration<URL> eu = bundle.findEntries("/", "*.jar", true);
			while (eu.hasMoreElements())
			{
				paths.add(eu.nextElement());
			}
		}
		return paths.toArray(new URL[paths.size()]);
	}
	
	
	public static final IPath convertToPath(URL url) throws IOException, CoreException, URISyntaxException
	{
		final URI fromuri = URIUtil.toURI(FileLocator.resolve(url));
		final IFileStore fromstore = EFS.getStore(fromuri);
		final File file = fromstore.toLocalFile(0, null);
		return Path.fromOSString(file.getCanonicalPath());
	}


	private Tools()
	{
		//no instance
	}

}
