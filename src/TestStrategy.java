import guitesting.engine.strategy.AbstractStrategy;
import guitesting.ui.GUITester;
import guitesting.util.IOUtil;
/**
 * 
 * @author Gigon Bae
 *
 */
public class TestStrategy extends AbstractStrategy {

  @Override
  public void run(String[] mainArgs) {
    IOUtil.configureWorkspaceFiles();

    GUITester tester = GUITester.getInstance();

    tester.getApplicationManager().runApp();
    tester.launchGUITesterUI();

  }

}
