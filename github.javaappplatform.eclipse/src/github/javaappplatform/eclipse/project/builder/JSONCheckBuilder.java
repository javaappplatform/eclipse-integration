package github.javaappplatform.eclipse.project.builder;

import java.io.IOException;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;


public class JSONCheckBuilder extends IncrementalProjectBuilder
{

	private class DeltaVisitor implements IResourceDeltaVisitor
	{

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(IResourceDelta delta) throws CoreException
		{
			IResource resource = delta.getResource();
			if (resource.getType() == IResource.FILE)
			{
				switch (delta.getKind())
				{
					case IResourceDelta.ADDED:
						checkJson(resource);
//					break;
					case IResourceDelta.CHANGED:
					case IResourceDelta.CONTENT:
						checkJson(resource);
						break;
					case IResourceDelta.REMOVED:
						deleteMarkers((IFile) resource);
				}
			}
			// return true to continue visiting children.
			return true;
		}
	}

	private class ResourceVisitor implements IResourceVisitor
	{
		
		@Override
		public boolean visit(IResource resource) throws CoreException
		{
			checkJson(resource);
			// return true to continue visiting children.
			return true;
		}
	}


	public static final String BUILDER_ID = "github.javaappplatform.eclipse.project.builder.JSONCheckBuilder";

	private static final String MARKER_TYPE = "github.javaappplatform.eclipse.project.builder.jsonProblem";


	private final JsonFactory factory = new JsonFactory();

	{
		this.factory.enable(Feature.ALLOW_COMMENTS);
		this.factory.enable(Feature.ALLOW_NON_NUMERIC_NUMBERS);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException
	{
		if (kind == FULL_BUILD)
			//full build
			this.getProject().accept(new ResourceVisitor());
		else
		{
			IResourceDelta delta = getDelta(getProject());
			if (delta == null)
				//full build
				this.getProject().accept(new ResourceVisitor());
			else
				//incremental build
				delta.accept(new DeltaVisitor());
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException
	{
		// delete markers set and files created
		this.getProject().deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
	}

	
	private void checkJson(IResource resource) throws CoreException
	{
		if ("ext".equals(resource.getFileExtension()))
		{
			IFile file = (IFile) resource;
			deleteMarkers(file);

			try
			{
				JsonParser parser = this.factory.createParser(file.getContents(true));
				while (parser.nextToken() != null)
				{
					//just run through
				}
			}
			catch (JsonParseException ex)
			{
				JSONCheckBuilder.this.addMarker(file, ex.getMessage(), ex.getLocation().getLineNr(), IMarker.SEVERITY_ERROR);
			}
			catch (IOException | CoreException e)
			{
				//ignore
			}
		}
	}

	private void addMarker(IFile file, String message, int lineNumber, int severity)
	{
		try
		{
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1)
			{
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e)
		{
			//ignore
		}
	}

	private void deleteMarkers(IFile file)
	{
		try
		{
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce)
		{
			// ignore
		}
	}

}
