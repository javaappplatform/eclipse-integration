package github.javaappplatform.eclipse.project.builder;

import github.javaappplatform.eclipse.Activator;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

/**
 * TODO javadoc
 * @author funsheep
 */
public class JAPBuilder
{

	public static final void scheduleNewBuild(final IJavaProject project)
	{
		final Job job = new Job("Scan project "+project.getProject().getName()+" for .ext files.")
		{
			
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				try
				{
					Nature nat = (Nature) project.getProject().getNature(Nature.NATURE_ID);
					if (nat != null)
					{
						Set<IFile> found = (new JAPBuilder()).visit(project);
						nat.reset(found.toArray(new IFile[found.size()]));
					}
					return Status.OK_STATUS;
				}
				catch (CoreException e)
				{
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Could not refresh .japsettings.", e);
				}
			}
		};
		job.setPriority(Job.BUILD);
		job.schedule();
	}
	
	public Set<IFile> visit(IJavaProject project) throws JavaModelException
	{
		HashSet<IFile> found = new HashSet<>();
		for (IPackageFragmentRoot root : project.getAllPackageFragmentRoots())
		{
			if (root.getKind() == IPackageFragmentRoot.K_SOURCE)
			{
				for (IJavaElement element : root.getChildren())
					if (element.getElementType() == IJavaElement.PACKAGE_FRAGMENT)
						this.handlePackageFragment((IPackageFragment) element, found);
			}
		}
		return found;
	}
	
	private void handlePackageFragment(IPackageFragment element, HashSet<IFile> found) throws JavaModelException
	{
		for (Object o : element.getNonJavaResources())
			if (o instanceof IFile)
				found.add((IFile) o);
		for (IJavaElement child : element.getChildren())
			if (child.getElementType() == IJavaElement.PACKAGE_FRAGMENT)
				this.handlePackageFragment((IPackageFragment) child, found);
	}

}
