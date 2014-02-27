package github.javaappplatform.eclipse.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * Simple utility class to create ui components.
 * @author funsheep
 */
public class SWTFactory
{

	public static Group createGroup(Composite parent, String text, int columns, int hspan, int fill)
	{
		Group g = new Group(parent, SWT.NONE);
		g.setLayout(new GridLayout(columns, false));
		((GridLayout)g.getLayout()).marginHeight = 10;
		g.setText(text);
		g.setFont(parent.getFont());
		GridData gd = new GridData(fill);
		gd.horizontalSpan = hspan;
		g.setLayoutData(gd);
		return g;
	}

	public static Composite createComposite(Composite parent, Font font, int columns, int hspan, int fill, int marginwidth, int marginheight)
	{
		Composite g = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(columns, false);
		layout.marginWidth = marginwidth;
		layout.marginHeight = marginheight;
		g.setLayout(layout);
		g.setFont(font);
		GridData gd = new GridData(fill);
		gd.horizontalSpan = hspan;
		g.setLayoutData(gd);
		return g;
	}

	private SWTFactory()
	{
		// no instance
	}

}
