package github.javaappplatform.eclipse.ui.launch;

import github.javaappplatform.eclipse.ui.util.SWTFactory;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * TODO javadoc
 * 
 * @author funsheep
 */
public class ParameterTab extends AbstractLaunchConfigurationTab implements ILaunchConfigurationTab
{

	private static final Image TAB_IMAGE = (new SourceLookupTab()).getImage();

	private final class WidgetListener implements SelectionListener, ModifyListener
	{

		@Override
		public void widgetSelected(SelectionEvent e)
		{
			ParameterTab.this.updateLaunchConfigurationDialog();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e)
		{
			ParameterTab.this.updateLaunchConfigurationDialog();
		}

		@Override
		public void modifyText(ModifyEvent e)
		{
			ParameterTab.this.updateLaunchConfigurationDialog();
		}

		// @Override
		// public void focusGained(FocusEvent e)
		// {
		// //do nothing
		// }
		//
		// @Override
		// public void focusLost(FocusEvent e)
		// {
		// ParameterTab.this.updateLaunchConfigurationDialog();
		// }
	}


	private final WidgetListener listener = new WidgetListener();
	private Combo loglevel;
	private Text pa;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent)
	{
		GridLayout layout = new GridLayout(1, false);
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		composite.setLayout(layout);

		GridData data = new GridData(SWT.FILL, 0, true, false);
		Group gplat = SWTFactory.createGroup(composite, "Platform", 3, 1, GridData.FILL_BOTH);
		gplat.setLayoutData(data);

		Label logLabel = new Label(gplat, SWT.NONE);
		logLabel.setText("Logging Level:");

		this.loglevel = new Combo(gplat, SWT.READ_ONLY);
		this.loglevel.setItems(new String[] { "SEVERE", "WARNING", "INFO", "CONFIG", "FINE", "FINER", "FINEST" });
		this.loglevel.addSelectionListener(this.listener);

		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		Group gpa = SWTFactory.createGroup(composite, "Platform Arguments", 1, 1, GridData.FILL_BOTH);
		// gpa.setText("Platform Arguments:");
		gpa.setLayoutData(data);

		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		this.pa = new Text(gpa, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
		this.pa.setEditable(true);
		this.pa.setLayoutData(data);
		this.pa.addModifyListener(this.listener);

		setControl(composite);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
	{
		configuration.setAttribute(ILaunchAPI.ATTR_LOG_LEVEL, "WARNING");
		configuration.setAttribute(ILaunchAPI.ATTR_PA, "");
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "github.javaappplatform.Platform");
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration configuration)
	{
		try
		{
			if (this.pa != null)
			{
				this.loglevel.setText(configuration.getAttribute(ILaunchAPI.ATTR_LOG_LEVEL, "WARNING"));
				this.pa.setText(configuration.getAttribute(ILaunchAPI.ATTR_PA, ""));
			}
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
		configuration.setAttribute(ILaunchAPI.ATTR_LOG_LEVEL, this.loglevel.getItem(this.loglevel.getSelectionIndex()));
		configuration.setAttribute(ILaunchAPI.ATTR_PA, this.pa.getText());
	}

//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	public boolean isValid(ILaunchConfiguration launchConfig)
//	{
//		this.setErrorMessage(null);
//		IStatus status = Checks.checkNetwork(launchConfig);
//		if (!status.isOK())
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
		return "Parameter";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Image getImage()
	{
		return TAB_IMAGE;
	}

}
