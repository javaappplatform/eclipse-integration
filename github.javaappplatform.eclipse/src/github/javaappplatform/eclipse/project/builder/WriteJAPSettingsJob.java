package github.javaappplatform.eclipse.project.builder;

import github.javaappplatform.eclipse.Activator;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * TODO javadoc
 * @author funsheep
 */
public class WriteJAPSettingsJob extends Job
{

	private final IProject project;
	private String towrite;


	/**
	 * @param name
	 */
	public WriteJAPSettingsJob(IProject project)
	{
		super("Write "+project.getName()+"/"+Nature.FILE_NAME);
		this.setPriority(Job.SHORT);
		this.project = project;
	}

	public void schedule(String toWrite)
	{
		synchronized(this)
		{
			this.towrite = toWrite;
		}
		this.schedule();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor)
	{
		String content;
		synchronized(this)
		{
			content = this.towrite;
		}
		try
		{
			InputStream stream = new ByteArrayInputStream(content.getBytes("UTF-8"));
			IFile file = this.project.getFile(Nature.FILE_NAME);
			if (!file.exists())
			{
				file.create(stream, true, null);
				file.setCharset("UTF-8", null);
			}
			else
				file.setContents(stream, true, false, null);
			return Status.OK_STATUS;
		}
		catch (UnsupportedEncodingException | CoreException e)
		{
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Could not write "+this.project.getName()+"/"+Nature.FILE_NAME, e);
		}
	}

}
