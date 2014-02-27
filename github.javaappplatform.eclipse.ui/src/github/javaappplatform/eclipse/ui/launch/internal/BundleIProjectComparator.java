package github.javaappplatform.eclipse.ui.launch.internal;

import java.util.Comparator;

import org.eclipse.core.resources.IProject;
import org.osgi.framework.Bundle;

public final class BundleIProjectComparator implements Comparator<Object>
{
	@Override
	public int compare(Object o1, Object o2)
	{
		if (o1 instanceof Bundle && o2 instanceof Bundle)
			return ((Bundle) o1).getSymbolicName().compareTo(((Bundle) o2).getSymbolicName());
		if (o1 instanceof IProject && o2 instanceof IProject)
			return ((IProject) o1).getName().compareTo(((IProject) o2).getName());
		if (o1 instanceof IProject)
		{
			Object h = o1;
			o1 = o2;
			o2 = h;
		}
		int t = ((Bundle) o1).getSymbolicName().compareTo(((IProject) o2).getName());
		if (t == 0)
			return -1;
		return t;
	}
}