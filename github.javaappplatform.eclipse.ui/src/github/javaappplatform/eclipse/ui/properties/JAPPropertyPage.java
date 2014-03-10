package github.javaappplatform.eclipse.ui.properties;

import github.javaappplatform.eclipse.project.classpath.JAPClasspathContainer;
import github.javaappplatform.eclipse.ui.util.SWTFactory;
import github.javaappplatform.eclipse.util.Tools;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.framework.Bundle;

public class JAPPropertyPage extends PropertyPage
{

	private IClasspathEntry tempSelection;
	private IJavaProject javaContext;
	private Table workspaceList;
	private Table bundleList;


	{
		this.noDefaultAndApplyButton();
		setTitle("Settings for the Java App Platform Library");
		setImageDescriptor(JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_LIBRARY));
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
    public void setElement(IAdaptable element)
    {
    	super.setElement(element);
    	this.javaContext = (IJavaProject) element;
    	this.tempSelection = this.javaContext.decodeClasspathEntry(JAPClasspathContainer.ID);
    }

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Control createContents(Composite uberControl)
	{
		Composite parent = SWTFactory.createComposite(uberControl, uberControl.getFont(), 2, 1, GridData.FILL_BOTH, 2, 10);

		Group g = SWTFactory.createGroup(parent, "Description", 1, 2, GridData.FILL_HORIZONTAL);

		GridData data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		data.grabExcessHorizontalSpace = true;
		
		Label description = new Label(g, SWT.WRAP);
		description.setText("Please choose the plugins that should be available in the project.\n" +
				"The plugins on the left side are custom projects located in the workspace.");
		description.setLayoutData(data);

		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		
		
		Group base = SWTFactory.createGroup(parent, "Workspace Plugins", 1, 1, GridData.FILL_BOTH);
		this.workspaceList = new Table(base, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL);
		this.workspaceList.setLayoutData(data);
		for (IProject project : Tools.searchForWorkspacePlugins())
		{
			if (this.javaContext != null && !this.javaContext.getProject().getName().equals(project.getName()))
			{
				TableItem item = new TableItem(this.workspaceList, SWT.NONE);
				item.setText(project.getName());
			}
		}

		Group plugins = SWTFactory.createGroup(parent, "Installed Plugins", 1, 1, GridData.FILL_BOTH);
		this.bundleList = new Table(plugins, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL);
		this.bundleList.setLayoutData(data);
		for (Bundle bundle : Tools.searchForInstalledPlugins())
		{	
			TableItem item = new TableItem(this.bundleList, SWT.NONE);
			item.setText(getItemText(bundle));
			item.setData(bundle.getSymbolicName());
		}
		
		this.updateSelectionBundles();
		this.updateSelectionWorkspace();
		
		Dialog.applyDialogFont(parent);
//		setControl(parent);
		return parent;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performOk()
	{
		if (this.workspaceList != null && !this.workspaceList.isDisposed())
		{
			try
			{
				HashSet<String> req = new HashSet<>(Arrays.asList(this.javaContext.getRequiredProjectNames()));
				LinkedList<IClasspathEntry> currentEntries = new LinkedList<>(Arrays.asList(this.javaContext.getRawClasspath()));
				for (TableItem item : this.workspaceList.getItems())
				{
					if (item.getChecked() && !req.contains(item.getText()))	//create a new entry and add it to the current classpath
					{
						IClasspathEntry prjEntry = JavaCore.newProjectEntry(new Path("/"+item.getText()), false); // exported
						currentEntries.add(prjEntry);
					}
					else if (!item.getChecked() && req.contains(item.getText()))	//remove corresponding entry from classpath
					{
						Iterator<IClasspathEntry> it = currentEntries.iterator();
						while (it.hasNext())
						{
							IClasspathEntry entry = it.next();
							if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT && entry.getPath().equals(new Path("/"+item.getText())))
								it.remove();
						}
					}
					
				}
				this.javaContext.setRawClasspath(currentEntries.toArray(new IClasspathEntry[currentEntries.size()]), null);
			}
			catch (JavaModelException e)
			{
				e.printStackTrace();
			}

		}
		
		if (this.bundleList != null && !this.bundleList.isDisposed())
		{
			StringBuilder sb = new StringBuilder(10);
			for (TableItem item : this.bundleList.getItems())
				if (item.getChecked())
				{
					sb.append('/');
					sb.append(item.getData().toString());
				}
			try
			{
				IClasspathEntry[] entries = this.javaContext.getRawClasspath();
				int index = -1;
				for (int i = 0; i < entries.length; i++)
				{
					if (entries[i].getEntryKind() == IClasspathEntry.CPE_CONTAINER && entries[i].getPath().segment(0).equals(JAPClasspathContainer.ID))
					{
						index = i;
						break;
					}
				}
				IClasspathEntry[] newentries = new IClasspathEntry[entries.length+(index == -1 ? 1 : 0)];
				System.arraycopy(entries, 0, newentries, 0, entries.length);
				newentries[index == -1 ? entries.length : index] = JavaCore.newContainerEntry(new Path(JAPClasspathContainer.ID + sb.toString()));
				this.javaContext.setRawClasspath(newentries, null);
			}
			catch (JavaModelException e)
			{
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	private final void updateSelectionBundles()
	{
		if (this.bundleList != null)
		{
			if (this.tempSelection != null)
			{
				for (int k = 1; k < this.tempSelection.getPath().segmentCount(); k++)
				{
					String segment = this.tempSelection.getPath().segment(k);
					for (TableItem item : this.bundleList.getItems())
						if (segment.equals(item.getData().toString()))
							item.setChecked(true);
				}
				this.tempSelection = null;
			}
			else
			{
				for (TableItem item : this.bundleList.getItems())
					item.setChecked(true);
			}
		}
	}
	
	private final void updateSelectionWorkspace()
	{
		if (this.workspaceList != null)
		{
			if (this.javaContext != null)
			{
				try
				{
//					if (!this.javaContext.getProject().hasNature(Nature.NATURE_ID))
//						throw new IllegalStateException("Project must have Java App Platform Nature.");
					HashSet<String> requirements = new HashSet<>(Arrays.asList(this.javaContext.getRequiredProjectNames()));
					for (TableItem item : this.workspaceList.getItems())
					{
						item.setChecked(requirements.contains(item.getText()));
					}
				}
				catch (CoreException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	private static final String getItemText(Bundle b)
	{
		String name = b.getHeaders().get("Bundle-Name");
		return (name != null ? name : b.getSymbolicName()) + " (" + b.getVersion() + ")";
	}

}