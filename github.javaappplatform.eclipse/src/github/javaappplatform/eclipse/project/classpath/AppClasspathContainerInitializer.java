package github.javaappplatform.eclipse.project.classpath;

import github.javaappplatform.eclipse.project.builder.Nature;

import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * Initializes the Classpath Container on load.
 * @author funsheep
 */
public class AppClasspathContainerInitializer extends ClasspathContainerInitializer
{
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(IPath containerPath, IJavaProject project) throws CoreException
	{
		try
		{
			JavaCore.setClasspathContainer(containerPath, new IJavaProject[] { project }, new IClasspathContainer[] { new AppClasspathContainer(containerPath) }, null);
			if (!project.getProject().hasNature(Nature.NATURE_ID))
			{
				IProjectDescription description = project.getProject().getDescription();
				String[] natures = description.getNatureIds();
				String[] newNatures = new String[natures.length + 1];
				System.arraycopy(natures, 0, newNatures, 0, natures.length);
				newNatures[natures.length] = Nature.NATURE_ID;
				description.setNatureIds(newNatures);
				project.getProject().setDescription(description, null);
			}
		} catch (Exception e1)
		{
			throw new CoreException(new Status(IStatus.ERROR, "github.javaappplatform.eclipse.project.builder", "Could not resolve Application Platform classpath.", e1));
		}
	}

}
