package github.javaappplatform.eclipse.ui.launch;

import github.javaappplatform.eclipse.ui.launch.internal.BundleIProjectComparator;
import github.javaappplatform.eclipse.ui.util.SWTFactory;
import github.javaappplatform.eclipse.util.Tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

/**
 * TODO javadoc
 * 
 * @author funsheep
 */
public class ExtensionsTab extends AbstractLaunchConfigurationTab
{

	private Table pluginList;
	private Tree extTree;
//	private Label msgImage;
//	private Label msg;


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite uber)
	{
		Composite parent = SWTFactory.createComposite(uber, uber.getFont(), 2, 1, GridData.FILL_BOTH, 0, 0);

		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;

		Group pluginGroup = SWTFactory.createGroup(parent, "Active Plugins", 1, 1, GridData.FILL_BOTH);
		this.pluginList = new Table(pluginGroup, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL);
		this.pluginList.setLayoutData(data);

		Group extGroup = SWTFactory.createGroup(parent, "Extensions", 1, 1, GridData.FILL_BOTH);
		this.extTree = new Tree(extGroup, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
		this.extTree.setLinesVisible(true);
		this.extTree.setLayoutData(data);
		// copied from SWT Snippets
		this.extTree.addListener(SWT.Selection, new Listener()
		{
			@Override
			public void handleEvent(Event event)
			{
				if (event.detail == SWT.CHECK)
				{
					TreeItem item = (TreeItem)event.item;
					boolean checked = item.getChecked();
					checkItems(item, checked);
					checkPath(item.getParentItem(), checked, false);
					// update parent or checked item appropriately - if you are the parent
//					ExtensionsTab.this.updateLaunchConfigurationDialog();
				}
			}
		});
		this.pluginList.addListener(SWT.Selection, new Listener()
		{
			@Override
			public void handleEvent(Event event)
			{
				if (event.detail == SWT.CHECK)
				{
					TableItem item = (TableItem) event.item;
					if (item.getChecked())
						//add bundle/project
						ExtensionsTab.this.addPluginToTree(item.getData(), null);
					else
						ExtensionsTab.this.removeObjectFromTree(item.getData());
					// update parent or checked item appropriately - if you are the parent
//					ExtensionsTab.this.updateLaunchConfigurationDialog();
				}
			}
		});

//		Composite msgParent = SWTFactory.createComposite(tableg, parent.getFont(), 2, 1, GridData.FILL_HORIZONTAL, 0, 0);
//		this.msgImage = new Label(msgParent, SWT.NONE);
//
//		data = new GridData();
//		data.horizontalAlignment = GridData.FILL;
//		data.grabExcessHorizontalSpace = true;
//		this.msg = new Label(msgParent, SWT.NONE);
//		this.msg.setLayoutData(data);

		setControl(parent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
	{
		configuration.setAttribute(ILaunchAPI.ATTR_EXCLUDED_EXTENSIONS, Collections.<String>emptySet());
		configuration.setAttribute(ILaunchAPI.ATTR_EXCLUDED_PLUGINS, Collections.<String>emptySet());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration configuration)
	{
		this.extTree.removeAll();
		this.pluginList.removeAll();
		Set<String> excludedPlugins = getExcluded(configuration, ILaunchAPI.ATTR_EXCLUDED_PLUGINS);

		ArrayList<Object> list = new ArrayList<Object>(Arrays.asList(Tools.searchForInstalledPlugins()));
		list.addAll(Arrays.asList(Tools.searchForWorkspacePlugins()));
		Collections.sort(list, new BundleIProjectComparator());
		
		for (Object o : list)
		{
			TableItem titem = this.createTableItemFor(o);
			titem.setChecked(!excludedPlugins.contains(titem.getText()));
			
			if (titem.getChecked())
			{
				//add to tree
				this.addPluginToTree(o, configuration);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration)
	{
		HashSet<String> excludedPlugins = new HashSet<>();
		for (TableItem item : this.pluginList.getItems())
			if (!item.getChecked())
				excludedPlugins.add(item.getText());
		configuration.setAttribute(ILaunchAPI.ATTR_EXCLUDED_PLUGINS, excludedPlugins);
		
		HashSet<String> excludedExtensions = new HashSet<>();
		for (TreeItem root : this.extTree.getItems())
			for (TreeItem child : root.getItems())
				if (!child.getChecked())
					excludedExtensions.add(child.getText());
		configuration.setAttribute(ILaunchAPI.ATTR_EXCLUDED_EXTENSIONS, excludedExtensions);
	}

	private TableItem createTableItemFor(Object o)
	{
		Object[] textImage = getTextAndImageFor(o);

		TableItem titem = new TableItem(this.pluginList, SWT.NONE);
		titem.setText((String) textImage[0]);
		titem.setImage((Image) textImage[1]);
		titem.setData(o);
		return titem;
	}
	
	private TreeItem createTreeItemFor(Object o, TreeItem parent)
	{
		Object[] textImage = getTextAndImageFor(o);
		TreeItem titem;
		if (parent != null)
			titem = new TreeItem(parent, SWT.NONE);
		else
		{
			int index = 0;
			for (TreeItem root : this.extTree.getItems())
			{
				if (root.getText().compareTo(String.valueOf(textImage[0])) > 0)
					break;
				index++;
			}
			titem = new TreeItem(this.extTree, SWT.NONE, index);
		}
		titem.setText((String) textImage[0]);
		titem.setImage((Image) textImage[1]);
		titem.setData(o);
		return titem;
	}
	

	private void addPluginToTree(Object o, ILaunchConfiguration configuration)
	{
		TreeItem titem = this.createTreeItemFor(o, null);
		Set<String> excludedExts = configuration != null ? getExcluded(configuration, ILaunchAPI.ATTR_EXCLUDED_EXTENSIONS) : Collections.<String>emptySet();

		TreeSet<String> sortedexts = new TreeSet<>(Tools.extensionsFor(o));
		for (String ext : sortedexts)
		{
			TreeItem child = this.createTreeItemFor(ext, titem);
			boolean checked = !excludedExts.contains(child.getData());
			child.setChecked(checked);
			checkPath(child, checked, false);
		}
		titem.setExpanded(true);
	}
	
	private void removeObjectFromTree(Object o)
	{
		for (TreeItem item : this.extTree.getItems())
			if (item.getData() == o)
				item.dispose();
	}
	

	private static final Object[] getTextAndImageFor(Object o)
	{
		Image image;
		String text;
		if (o instanceof Bundle)
		{
			image = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_LIBRARY);
			text = ((Bundle) o).getSymbolicName();
		}
		else if (o instanceof IProject)
		{
			image = PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT);
			text = ((IProject) o).getName();
		}
		else
		{
			image = PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_OBJ_FILE);
			text = String.valueOf(o);
		}
		return new Object[] { text, image };
	}
	
//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	public boolean isValid(ILaunchConfiguration configuration)
//	{
//		this.setErrorMessage(null);
////		IStatus status = Checks.checkForJavaproject(configuration);
//		if (!status.isOK() && status.getCode() == IStatus.ERROR)
//		{
//			this.setErrorMessage(status.getMessage());
//			return false;
//		}
//		return true;
//	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName()
	{
		return "Extensions";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Image getImage()
	{
		return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_LIBRARY);
	}

//	public void setWarningMessage(String msg)
//	{
//		if (msg == null)
//		{
//			this.msgImage.setImage(null);
//			this.msg.setText("");
//		} else
//		{
//			this.msgImage.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_OBJS_WARN_TSK));
//			this.msg.setText(msg);
//		}
//	}

	// copied from SWT Snippets
	static void checkPath(TreeItem item, boolean checked, boolean grayed)
	{
		if (item == null)
			return;
		if (grayed)
		{
			checked = true;
		} else
		{
			int index = 0;
			TreeItem[] items = item.getItems();
			while (index < items.length)
			{
				TreeItem child = items[index];
				if (child.getGrayed() || checked != child.getChecked())
				{
					checked = grayed = true;
					break;
				}
				index++;
			}
		}
		item.setChecked(checked);
		item.setGrayed(grayed);
		checkPath(item.getParentItem(), checked, grayed);
	}

	// copied from SWT Snippets
	static void checkItems(TreeItem item, boolean checked)
	{
		item.setGrayed(false);
		item.setChecked(checked);
		TreeItem[] items = item.getItems();
		for (int i = 0; i < items.length; i++)
		{
			checkItems(items[i], checked);
		}
	}

	@SuppressWarnings("unchecked")
	public static final Set<String> getExcluded(ILaunchConfiguration config, String attrID)
	{
		Set<String> excluded = Collections.<String>emptySet();
		try
		{
			excluded = config.getAttribute(attrID, Collections.EMPTY_SET);
		}
		catch (CoreException e)
		{
			//ignore
		}
		return excluded;
	}

}
