package github.javaappplatform.eclipse.ui.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.WorkingDirectoryBlock;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * TODO javadoc
 * 
 * @author funsheep
 */
public class WorkingDirBlock extends WorkingDirectoryBlock
{

	/**
	 * @param workingDirectoryAttribteName
	 */
	public WorkingDirBlock()
	{
		super(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, null);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setDefaultWorkingDir()
	{
		setDefaultWorkingDirectoryText("${workspace_loc}");  //$NON-NLS-1$//$NON-NLS-2$
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IProject getProject(ILaunchConfiguration configuration) throws CoreException
	{
		IJavaProject project = JavaRuntime.getJavaProject(configuration);
		return project == null ? null : project.getProject();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void log(CoreException e)
	{
		setErrorMessage(e.getMessage());
	}

}
