package github.javaappplatform.eclipse.project.builder;

import github.javaappplatform.commons.events.TalkerStub;
import github.javaappplatform.commons.util.StringID;
import github.javaappplatform.eclipse.Activator;
import github.javaappplatform.eclipse.util.Tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Provides an API to manage the extensions found within the classpath of the project.
 * All found extensions are saved to a project specific file (i.e. .appext) that can be exported with
 * a jar. This file is then used by the eclipse LaunchConfiguration to easily provide a list with
 * extensions that can be loaded on platform start.
 * 
 * @author funsheep
 */
public class Nature extends TalkerStub implements IProjectNature
{

	/** ID of this project nature */
	public static final String NATURE_ID = "github.javaappplatform.eclipse.project.builder.Nature";

	public static final int EVENT_EXTENSION_ADDED = StringID.id("EVENT_EXTENSION_ADDED");
	public static final int EVENT_EXTENSION_REMOVED = StringID.id("EVENT_EXTENSION_REMOVED");
	public static final int EVENT_EXTENSIONS_RELOADED = StringID.id("EVENT_EXTENSIONS_RELOADED");

	static final String FILE_NAME = ".extension_db";
	

	private final HashSet<IFile> extensions = new HashSet<>();
	private IProject project;
	private WriteJAPSettingsJob writeJob;

	

	public Set<IProject> dependencies()
	{
		IJavaProject javaProject = JavaCore.create(this.project);
		HashSet<IProject> workspacePlugins = new HashSet<>(Arrays.asList(Tools.searchForWorkspacePlugins()));
		try
		{
			HashSet<String> requirements = new HashSet<>(Arrays.asList(javaProject.getRequiredProjectNames()));
			Iterator<IProject> it = workspacePlugins.iterator();
			while (it.hasNext())
			{
				if (!requirements.contains(it.next().getName()))
					it.remove();
			}
			return workspacePlugins;
		}
		catch (JavaModelException e)
		{
			return Collections.emptySet();
		}
	}

	public synchronized Set<IFile> extensions()
	{
		return Collections.unmodifiableSet(this.extensions);
	}
	
	public synchronized Set<String> extensionNames()
	{
		HashSet<String> names = new HashSet<>(this.extensions.size());
		for (IFile res : this.extensions)
			names.add(this.toExtension(res));
		return names;
	}
	
	
	void addExtension(String extension) throws CoreException
	{
		this.addExtension(this.toFile(extension));
	}
	
	synchronized void addExtension(IResource resource) throws CoreException
	{
		if (resource != null && resource.getType() == IResource.FILE && this.isValidExtension(resource) && this.extensions.add((IFile) resource))
		{
			this.writeExtsToFile();
			this.postEvent(EVENT_EXTENSION_ADDED, resource);
		}
	}

	void removeExtension(String extension) throws CoreException
	{
		this.removeExtension(this.toFile(extension));
	}
	
	synchronized void removeExtension(IResource resource) throws CoreException
	{
		if (resource != null && this.extensions.remove(resource))
		{
			this.writeExtsToFile();
			this.postEvent(EVENT_EXTENSION_REMOVED, resource);
		}
	}
	
	synchronized void reset(IFile... exts) throws CoreException
	{
		this.extensions.clear();
		this.extensions.addAll(Arrays.asList(exts));
		this.validateExtensions();
		this.writeExtsToFile();
		this.postEvent(EVENT_EXTENSIONS_RELOADED, (Object[]) this.extensions.toArray(new IFile[this.extensions.size()]));
	}

	synchronized void loadFromFile(IProgressMonitor monitor) throws CoreException
	{
		if (monitor == null)
			monitor = new NullProgressMonitor();
		try
		{
			this.extensions.clear();
			IFile file = this.project.getFile(FILE_NAME);
			if (file.exists())
			{
				monitor.beginTask("Load Application Plugin List.", IProgressMonitor.UNKNOWN);
				InputStream stream = file.getContents(true);
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream, file.getCharset()));
				String line = reader.readLine();
				while (line != null)
				{
					this.extensions.add(this.toFile(line));
					line = reader.readLine();
				}
				this.validateExtensions();
				this.postEvent(EVENT_EXTENSIONS_RELOADED, (Object[]) this.extensions.toArray(new IFile[this.extensions.size()]));
				monitor.done();
			}
			else
			{
				JAPBuilder.scheduleNewBuild(JavaCore.create(this.project));
			}
		}
		catch (IOException e)
		{
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Could not read .jap file content.", e));
		}
	}
	
	private void writeExtsToFile() throws CoreException
	{
//		this.validateExtensions();
		StringBuilder sb = new StringBuilder();
		for (String ext : this.extensionNames())
		{
			sb.append(ext);
			sb.append('\n');
		}
		this.writeJob.schedule(sb.toString());
	}

	private void validateExtensions() throws CoreException
	{
		Iterator<IFile> extit = this.extensions.iterator();
		while (extit.hasNext())
		{
			if (!this.isValidExtension(extit.next()))
				extit.remove();
		}
	}

	public boolean isValidExtension(String extension)
	{
		return this.toFile(extension) != null;
	}

	public boolean isValidExtension(IResource resource)
	{
		return resource.getType() == IResource.FILE && this.toExtension((IFile) resource) != null;
	}
	
	public String toExtension(IFile resource)
	{
		if (resource.getType() != IResource.FILE)
			return null;
		if (!resource.getName().toLowerCase().endsWith("ext"))
			return null;
		
		IJavaProject javaProject = JavaCore.create(this.project);
		if (javaProject.exists())
		{
			try
			{
				IPackageFragment frag = javaProject.findPackageFragment(resource.getParent().getFullPath());
				if (frag != null && frag.exists())
					return (frag.getElementName().trim().length() == 0 ? "" : frag.getElementName() + ".") + resource.getName().substring(0, resource.getName().length()-4);
			}
			catch (JavaModelException e)
			{
				//ignore
			}
		}
		return null;
	}
	
	public IFile toFile(String extension)
	{
		IJavaProject javaProject = JavaCore.create(this.project);
		if (javaProject.exists())
		{
			String[] ext = separateExtension(extension);
			IPath path = new Path(ext[0].replace('.', '/'));
			try
			{
				IJavaElement elem = javaProject.findElement(path);
				if (elem instanceof IPackageFragment && elem.exists())
				{
					IPackageFragment frag = (IPackageFragment) elem;
					Object[] nonres = frag.getNonJavaResources();
					for (Object o : nonres)
					{
						if (o instanceof IFile)
						{
							IFile file = (IFile) o;
							if (file.getName().equalsIgnoreCase(ext[1]+".ext"))
								return file;
						}
					}
				}
			}
			catch (JavaModelException e)
			{
				//ignore
			}
		}
		return null;
	}
	
	private static final String[] separateExtension(String extension)
	{
		if (extension.endsWith(".ext"))
			extension = extension.substring(0, extension.length()-4);
		final int splitindex = extension.lastIndexOf('.');
		if (splitindex == -1)
			return new String[] { "", extension };
		return new String[] { extension.substring(0, splitindex), extension.substring(splitindex+1) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure() throws CoreException
	{
		//configure builder (if needed)
		IProjectDescription desc = this.project.getDescription();
		ICommand[] commands = desc.getBuildSpec();

		for (int i = 0; i < commands.length; ++i)
		{
			if (commands[i].getBuilderName().equals(JSONCheckBuilder.BUILDER_ID))
			{
				return;
			}
		}

		ICommand[] newCommands = new ICommand[commands.length + 1];
		System.arraycopy(commands, 0, newCommands, 0, commands.length);
		ICommand command = desc.newCommand();
		command.setBuilderName(JSONCheckBuilder.BUILDER_ID);
		newCommands[newCommands.length - 1] = command;
		desc.setBuildSpec(newCommands);
		this.project.setDescription(desc, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deconfigure() throws CoreException
	{
		//remove .jap file
		try
		{
			this.project.getFile(FILE_NAME).delete(true, null);
		}
		catch (CoreException e)
		{
			//there is no need to report this
		}
		
		//deconfigure builder (if needed)
		IProjectDescription description = getProject().getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i)
		{
			if (commands[i].getBuilderName().equals(JSONCheckBuilder.BUILDER_ID))
			{
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
				description.setBuildSpec(newCommands);
				this.project.setDescription(description, null);
				return;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IProject getProject()
	{
		return this.project;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setProject(IProject project)
	{
		this.project = project;
		this.writeJob = new WriteJAPSettingsJob(this.project);
		//load extensions (if available)
		try
		{
			this.loadFromFile(null);
		}
		catch (CoreException e)
		{
			//ignore
		}
	}

}
