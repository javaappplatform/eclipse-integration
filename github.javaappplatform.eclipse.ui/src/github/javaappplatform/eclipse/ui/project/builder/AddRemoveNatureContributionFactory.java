package github.javaappplatform.eclipse.ui.project.builder;

import github.javaappplatform.eclipse.project.builder.Nature;

import java.util.List;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Workaround for Configure menu addition as long as eclipse bug #428866 is not resolved.
 * @author funsheep
 */
public class AddRemoveNatureContributionFactory extends ExtensionContributionFactory
{
	
	private static final String ADD_COMMAND_ID = "github.javaappplatform.eclipse.project.builder.AddNature";
	private static final String REMOVE_COMMAND_ID = "github.javaappplatform.eclipse.project.builder.RemoveNature";

	private static final String ADD_MENU_ID = "github.javaappplatform.eclipse.ui.project.builder.AddMenu";
	private static final String REMOVE_MENU_ID = "github.javaappplatform.eclipse.ui.project.builder.RemoveMenu";

	private static class AddRemoveExpression extends Expression
	{

		private final boolean add;
		
		public AddRemoveExpression(boolean add)
		{
			this.add = add;
		}

	
		@Override
		public EvaluationResult evaluate(IEvaluationContext context)
		{
			try
			{
				if (context.getDefaultVariable() instanceof List<?>)
				{
					List<?> list = (List<?>) context.getDefaultVariable();
					if (list.size() == 1 && list.get(0) instanceof IJavaProject)
					{
						IJavaProject project = (IJavaProject) list.get(0);
						if ((project.getProject().getNature(Nature.NATURE_ID) == null) == this.add)
							return EvaluationResult.TRUE;
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return EvaluationResult.FALSE;
		}
	}

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions)
	{
		CommandContributionItemParameter params = new CommandContributionItemParameter(serviceLocator, ADD_MENU_ID, ADD_COMMAND_ID, CommandContributionItem.STYLE_PUSH);
		additions.addContributionItem(new CommandContributionItem(params), new AddRemoveExpression(true));
		params = new CommandContributionItemParameter(serviceLocator, REMOVE_MENU_ID, REMOVE_COMMAND_ID, CommandContributionItem.STYLE_PUSH);
		additions.addContributionItem(new CommandContributionItem(params), new AddRemoveExpression(false));
	}

}
