package github.javaappplatform.eclipse.ui.launch;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;

/**
 * TODO javadoc
 * @author funsheep
 */
public class LaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup implements ILaunchConfigurationTabGroup
{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode)
	{
		final ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[]
		{
			new ParameterTab(),
			new ExtensionsTab(),
			new ArgumentsTab(),
			new EnvironmentTab(),
			new CommonTab()
			
		};
		setTabs(tabs);
	}

}
