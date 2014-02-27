package github.javaappplatform.eclipse.ui.launch.internal;

import github.javaappplatform.eclipse.ui.Activator;
import github.javaappplatform.eclipse.ui.launch.ILaunchAPI;
import github.javaappplatform.eclipse.ui.launch.ILaunchMessages;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

/**
 * TODO javadoc
 * @author funsheep
 */
public class Checks
{
	
	public static class Result extends Status
	{
		
		public final Object result;

		
		public Result(int code, String message, Throwable exception)
		{
			super(code, Activator.PLUGIN_ID, code, message, exception);
			this.result = null;
		}
		
		public Result(Object result)
		{
			super(IStatus.OK, Activator.PLUGIN_ID, IStatus.OK, "ok", null);
			this.result = result;
		}

		public Result(IStatus status)
		{
			super(status.getSeverity(), status.getPlugin(), status.getCode(), status.getMessage(), status.getException());
			this.result = null;
		}
	}

	public static final Result checkForProject(ILaunchConfiguration config)
	{
		String projectName = null;
		try {
			projectName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null); //$NON-NLS-1$
		} catch (CoreException e) {
			return error(e);
		}
		return checkForProject(projectName);
	}
	
	public static final Result checkForProject(String projectName)
	{
		if (projectName != null && projectName.length() > 0)
		{
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IStatus status = workspace.validateName(projectName, IResource.PROJECT);
			if (status.isOK()) {
				IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				if (!project.exists()) {
					return new Result(IStatus.ERROR, ILaunchMessages.PROJECT_NOT_EXISTING, null);
				}
				if (!project.isOpen()) {
					return new Result(IStatus.ERROR, ILaunchMessages.PROJECT_CLOSED, null);
				}
				return new Result(project);
			}
			return new Result(status);
		}
		return new Result(IStatus.ERROR, ILaunchMessages.NO_PROJECT, null);
	}

	public static final Result checkForJavaproject(ILaunchConfiguration config)
	{
		Result status = checkForProject(config);
		if (!status.isOK())
			return status;
		return checkForJavaproject(((IProject) status.result).getName());
	}

	public static final Result checkForJavaproject(String name)
	{
		try
		{
			for (IJavaProject project : JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects())
			{
				if (name.equals(project.getProject().getName()))
					return new Result(project);
			}
		} catch (JavaModelException e)
		{
			return error(e);
		}
		return new Result(IStatus.ERROR, ILaunchMessages.NOT_JAVA_PROJECT, null);
	}
	
	private static final IStatus checkJavaMethod(URI uri, String methodSignature, IJavaProject project)
	{
		IStatus status2 = checkForJavaMethodURI(uri);
		if (!status2.isOK())
			return status2;

		String fragment = uri.getFragment();
		if (fragment.endsWith("()"))
			fragment = fragment.substring(0, fragment.length()-2);
		SearchPattern pattern = SearchPattern.createPattern(uri.getRawPath()+'.'+fragment + methodSignature, IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, IJavaSearchScope.SOURCES);
		final ArrayList<Object> list = new ArrayList<Object>();
		SearchRequestor requestor = new SearchRequestor()
		{
			
			@Override
			public void acceptSearchMatch(SearchMatch match) throws CoreException
			{
				list.add(match);
			}
		};
		SearchEngine engine = new SearchEngine();
		try
		{
			engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope, requestor, null);
		} catch (CoreException e)
		{
			return error(e);
		}
		if (list.size() == 0)
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, ILaunchMessages.COULD_NOT_FIND_JAVA_VISUMODEL_METHOD, null);
		if (list.size() > 1)
			return new Status(IStatus.WARNING, Activator.PLUGIN_ID, IStatus.WARNING, ILaunchMessages.AMBIGUOUS_JAVA_VISUMODEL_DEFINITION, null);
		return Status.OK_STATUS;
	}
	
	public static final IStatus checkForJavaMethodURI(URI uri)
	{
		if (uri == null || !"java".equals(uri.getScheme()))
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, ILaunchMessages.JAVA_URI_SYNTAX_ERROR, null);
		return Status.OK_STATUS;
	}
	
	public static final IStatus checkNetwork(ILaunchConfiguration config)
	{
		try
		{
			if (config.getAttribute(ILaunchAPI.ATTR_NET_ENABLED, false))
			{
				if (config.getAttribute(ILaunchAPI.ATTR_HOST, "").length() == 0)
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, ILaunchMessages.HOST_NOT_SPECIFIED, null);
				if (config.getAttribute(ILaunchAPI.ATTR_TCP, "").length() == 0)
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, ILaunchMessages.TCP_NOT_SPECIFIED, null);
				if (config.getAttribute(ILaunchAPI.ATTR_UDP, "").length()  == 0)
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, ILaunchMessages.UDP_NOT_SPECIFIED, null);
			}
		}
		catch (CoreException e)
		{
			return error(e);
		}
		return Status.OK_STATUS;
	}

	public static final IStatus checkPossibleJavaURISyntax(ILaunchConfiguration config, String attribute)
	{
		try
		{
			String s = config.getAttribute(attribute, (String) null);
			if (s != null && s.length() > 0)
			{
				Result result = checkForURI(s);
				if (!result.isOK())
					return result;
				URI uri = (URI) result.result;
				if ("java".equals(uri.getScheme()))
					return checkForJavaMethodURI(uri);
			}
			return Status.OK_STATUS;
		}
		catch (CoreException e)
		{
			return error(e);
		}
	}	

	public static final IStatus checkPossibleURISyntax(ILaunchConfiguration config, String attribute)
	{
		try
		{
			String s = config.getAttribute(attribute, (String) null);
			if (s != null && s.length() > 0)
				return checkForURI(s);
			return Status.OK_STATUS;
		}
		catch (CoreException e)
		{
			return error(e);
		}
	}
	
	public static final Result checkForURI(ILaunchConfiguration config, String attribute)
	{
		try
		{
			return checkForURI(config.getAttribute(attribute, (String) null));
		}
		catch (CoreException e)
		{
			return error(e);
		}
	}
	
	public static final Result checkForURI(String string)
	{
		if (string == null || string.length() == 0)
			return new Result(IStatus.ERROR, ILaunchMessages.URI_SYNTAX_ERROR + " Given string is empty.", null);
		try
		{
			URI uri = new URI(string);
			if (uri.getScheme() == null)
				return new Result(IStatus.ERROR, ILaunchMessages.URI_SYNTAX_ERROR + " No scheme specified.", null);
			return new Result(uri);
		}
		catch (URISyntaxException exception)
		{
			return new Result(IStatus.ERROR, ILaunchMessages.URI_SYNTAX_ERROR + (exception.getReason() != null ? " " + exception.getReason() : ""), exception);
		}
	}
	
	public static final IStatus checkForRequiredStringAttribute(String attribute, ILaunchConfiguration config)
	{
		String attr;
		try
		{
			attr = config.getAttribute(attribute, (String) null);
		} catch (CoreException e)
		{
			return error(e);
		}
		if (attr == null || attr.length() == 0)
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, "Required attribute " + attribute + " not set.", null);
		return Status.OK_STATUS;
	}
	

	private static final Result error(Exception e)
	{
		return new Result(IStatus.ERROR, null, e);
	}

	private Checks()
	{
		//do nothing
	}

}
