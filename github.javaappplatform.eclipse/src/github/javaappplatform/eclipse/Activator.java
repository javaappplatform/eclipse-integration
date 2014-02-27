package github.javaappplatform.eclipse;

import github.javaappplatform.eclipse.project.builder.JavaDeltaVisitor;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;


/**
 * TODO javadoc
 * @author funsheep
 */
public class Activator implements BundleActivator
{

	public static final String PLUGIN_ID = "github.javaappplatform.eclipse.project.builder";

	
	private static BundleContext CONTEXT;

	private static JavaDeltaVisitor INSTANCE = new JavaDeltaVisitor();
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start(BundleContext context) throws Exception
	{
		CONTEXT = context;
		JavaCore.addElementChangedListener(INSTANCE, ElementChangedEvent.POST_CHANGE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop(BundleContext context) throws Exception
	{
		JavaCore.removeElementChangedListener(INSTANCE);
		CONTEXT = null;
	}

	public static BundleContext getContext()
	{
		return CONTEXT;
	}
	
}
