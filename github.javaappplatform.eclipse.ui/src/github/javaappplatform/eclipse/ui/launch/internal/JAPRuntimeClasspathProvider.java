package github.javaappplatform.eclipse.ui.launch.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.StandardClasspathProvider;

/**
 * TODO javadoc
 * @author funsheep
 */
public class JAPRuntimeClasspathProvider extends StandardClasspathProvider
{
	
	public static final String ID = "github.javaappplatform.eclipse.ui.launch.internal.JAPRuntimeClasspathProvider";


	/**
	 * {@inheritDoc}
	 */
	@Override
	public IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration configuration) throws CoreException
	{
		final Set<IProject> includedProjects = PluginTools.computeIncludedProjects(configuration);
		ArrayList<IRuntimeClasspathEntry> entries = new ArrayList<>();

		for (IProject proj : includedProjects)
		{
			if (proj.hasNature(JavaCore.NATURE_ID))
				entries.addAll(Arrays.asList(JavaRuntime.computeUnresolvedRuntimeClasspath(JavaCore.create(proj))));
		}
		entries.add(JavaRuntime.computeJREEntry(configuration));
		entries.add(JavaRuntime.newVariableRuntimeClasspathEntry(new Path("JAP_LIB")));
			
		System.err.println(entries);
		return entries.toArray(new IRuntimeClasspathEntry[entries.size()]);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public IRuntimeClasspathEntry[] resolveClasspath(IRuntimeClasspathEntry[] entries, ILaunchConfiguration configuration) throws CoreException
	{
		IRuntimeClasspathEntry[] resolved = super.resolveClasspath(entries, configuration);
		System.out.println("  " + Arrays.toString(entries));
		System.out.println("  " + Arrays.toString(resolved));
		return resolved;
	}

}
