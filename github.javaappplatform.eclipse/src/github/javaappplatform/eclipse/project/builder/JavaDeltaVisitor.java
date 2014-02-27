package github.javaappplatform.eclipse.project.builder;

import java.util.Arrays;
import java.util.LinkedList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;

public class JavaDeltaVisitor implements IElementChangedListener
{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void elementChanged(ElementChangedEvent event)
	{
		JavaDeltaVisitor visitor = new JavaDeltaVisitor();
		LinkedList<IJavaElementDelta> toVisit = new LinkedList<>();
		toVisit.add(event.getDelta());
		while (!toVisit.isEmpty())
		{
			IJavaElementDelta delta = toVisit.removeFirst();
			if (visitor.visit(delta))
				toVisit.addAll(Arrays.asList(delta.getAffectedChildren()));
		}
	}

	private boolean visit(final IJavaElementDelta delta)
	{
		IJavaElement elem = delta.getElement(); 
		if (elem.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT)
		{
			JAPBuilder.scheduleNewBuild(elem.getJavaProject());
		}
		else if (elem.getElementType() == IJavaElement.PACKAGE_FRAGMENT)
		{
			handleContentDelta(delta);
		}
		else if (elem.getElementType() == IJavaElement.JAVA_PROJECT && getNature(delta) == null)
			return false;
		return hasAllFlags(delta, IJavaElementDelta.F_CHILDREN);
	}
	
	private void handleContentDelta(IJavaElementDelta delta)
	{
		if (hasAtLeastFlag(delta, IJavaElementDelta.F_CONTENT) && delta.getResourceDeltas() != null)
		{
			for (IResourceDelta child : delta.getResourceDeltas())
			{
				IResource resource = child.getResource();
				if (resource.getType() == IResource.FILE)
				{
					try
					{
						switch (child.getKind())
						{
							case IResourceDelta.ADDED:
								Nature nat = getNature(delta);
								if (nat != null)
									nat.addExtension(resource);
							break;
							case IResourceDelta.REMOVED:
								nat = getNature(delta);
								if (nat != null)
									nat.removeExtension(resource);
								break;
						}
					}
					catch (CoreException e)
					{
						//ignore
					}
				}
			}
		}
	}

	
	private static final boolean hasAllFlags(IJavaElementDelta delta, int flags)
	{
		return (delta.getFlags() & flags) == flags;
	}

	private static final boolean hasAtLeastFlag(IJavaElementDelta delta, int flags)
	{
		return (delta.getFlags() & flags) != 0;
	}

	private static final Nature getNature(IJavaElementDelta delta)
	{
		try
		{
			return (Nature) getProject(delta).getNature(Nature.NATURE_ID);
		}
		catch (CoreException e)
		{
			//ignore
		}
		return null;
	}

	private static final IProject getProject(IJavaElementDelta delta)
	{
		return delta.getElement().getJavaProject().getProject();
	}

}