package github.javaappplatform.eclipse.ui.launch.internal;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathProvider;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * TODO javadoc
 * @author funsheep
 */
public class JAPRuntimeClasspathProvider implements IRuntimeClasspathProvider
{
	
//	public static final String ID = "de.d3fact.SRE.launch.internal.SRERuntimeClasspathProvider";


	/**
	 * {@inheritDoc}
	 */
	@Override
	public IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration configuration) throws CoreException
	{
		IJavaProject proj = JavaRuntime.getJavaProject(configuration);
		IRuntimeClasspathEntry jreEntry = JavaRuntime.computeJREEntry(configuration);
		if (proj == null) {
			//no project - use default libraries
			if (jreEntry == null) {
				return new IRuntimeClasspathEntry[0];
			}
			return new IRuntimeClasspathEntry[]{jreEntry};				
		}
		
		LinkedHashSet<IRuntimeClasspathEntry> entrySet = new LinkedHashSet<IRuntimeClasspathEntry>(10);
		getDefaultClasspath(proj, jreEntry, entrySet);
		// recover persisted classpath
		return entrySet.toArray(new IRuntimeClasspathEntry[entrySet.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public IRuntimeClasspathEntry[] resolveClasspath(IRuntimeClasspathEntry[] entries, ILaunchConfiguration configuration) throws CoreException
	{
		// use an ordered set to avoid duplicates
		Set all = new LinkedHashSet(entries.length);
		for (int i = 0; i < entries.length; i++) {
			IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveRuntimeClasspathEntry(entries[i], configuration);
			for (int j = 0; j < resolved.length; j++) {
				all.add(resolved[j]);
			}
		}
		for (Object object : all)
		{
			IRuntimeClasspathEntry entry = (IRuntimeClasspathEntry) object;
			System.out.println("[" + entry.getType() + "]" + entry.getLocation());
		}
		return (IRuntimeClasspathEntry[])all.toArray(new IRuntimeClasspathEntry[all.size()]);
	}


	protected void getDefaultClasspath(final IJavaProject project, final IRuntimeClasspathEntry jreEntry, final LinkedHashSet<IRuntimeClasspathEntry> set) throws CoreException
	{
		IRuntimeClasspathEntry[] entries = JavaRuntime.computeUnresolvedRuntimeClasspath(project);
		// replace project JRE with config's JRE
		IRuntimeClasspathEntry projEntry = JavaRuntime.computeJREEntry(project);
		for (int i = 0; i < entries.length; i++)
		{
			IRuntimeClasspathEntry entry = entries[i];
			if (jreEntry != null && !jreEntry.equals(projEntry) && entry.equals(projEntry))
			{
				entry = jreEntry;
			}
			if (set.add(entry) && entry.getType() == IRuntimeClasspathEntry.PROJECT)
				getDefaultClasspath(entry.getJavaProject(), jreEntry, set);
		}
	}

}
