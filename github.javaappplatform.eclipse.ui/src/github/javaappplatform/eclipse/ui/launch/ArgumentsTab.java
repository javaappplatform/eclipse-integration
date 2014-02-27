package github.javaappplatform.eclipse.ui.launch;

import github.javaappplatform.eclipse.ui.util.SWTFactory;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * TODO javadoc
 * @author funsheep
 */
public class ArgumentsTab extends AbstractLaunchConfigurationTab
{
	
	private static final String ATTR_OTHER = "attr-other";

	private final ModifyListener listener = new ModifyListener()
	{
		
		@Override
		public void modifyText(ModifyEvent e)
		{
			ArgumentsTab.this.updateLaunchConfigurationDialog();
		}

	};

	private Text vma;
	private Button workspace;
	private Button other;
	private Text otherPath;
	private final Image image = (new JavaArgumentsTab()).getImage();

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		composite.setLayout(layout);
		composite.setFont(parent.getFont());
		
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		Group gvma = SWTFactory.createGroup(composite, "VM Arguments", 1, 1, GridData.FILL_BOTH);
		
		this.vma = new Text(gvma, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
		this.vma.setEditable(true);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		this.vma.setLayoutData(data);
		this.vma.addModifyListener(this.listener);

		Group gwork = SWTFactory.createGroup(composite, "Working Directory", 2, 1, GridData.FILL_HORIZONTAL);
		
		data = new GridData();
		data.horizontalSpan = 2;
		this.workspace = new Button(gwork, SWT.RADIO);
		this.workspace.setText("Workspace");
		this.workspace.setLayoutData(data);
		this.workspace.setSelection(true);
		this.workspace.addListener(SWT.Selection, new Listener()
		{
			
			@Override
			public void handleEvent(Event event)
			{
				ArgumentsTab.this.checkSelection();
			}
		});

		this.other = new Button(gwork, SWT.RADIO);
		this.other.setText("Other:");
		
		data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		data.grabExcessHorizontalSpace = true;
		this.otherPath = new Text(gwork, SWT.SINGLE | SWT.BORDER);
		this.otherPath.setEditable(false);
		this.otherPath.setEnabled(false);
		this.otherPath.setLayoutData(data);
		this.otherPath.addModifyListener(this.listener);
		
		setControl(composite);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
	{
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, (String) null);
		configuration.setAttribute(ATTR_OTHER, false);
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, workspaceDir());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration configuration)
	{
		try
		{
			if (this.vma != null)
				this.vma.setText(configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, ""));
			boolean attr_other = configuration.getAttribute(ATTR_OTHER, false);
			this.other.setSelection(attr_other);
			this.workspace.setSelection(!attr_other);
			if (attr_other)
				this.otherPath.setText(configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, ""));
			this.checkSelection();
		} catch (CoreException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration)
	{
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, this.vma.getText());
		configuration.setAttribute(ATTR_OTHER, this.other.getSelection());
		String dir = workspaceDir();
		if (this.other.getSelection())
			dir = this.otherPath.getText();
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, dir);
	}

	private void checkSelection()
	{
		this.otherPath.setEditable(this.other.getSelection());
		this.otherPath.setEnabled(this.other.getSelection());
		this.updateLaunchConfigurationDialog();
	}
	
	private static String workspaceDir()
	{
		return "${workspace_loc}";
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName()
	{
		return "Arguments";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Image getImage()
	{
		return this.image;
	}
	
}
