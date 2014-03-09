import guitesting.engine.monitors.IExecutionMonitor;
import guitesting.model.EventListModel;
import guitesting.model.GUIModel;
import guitesting.model.event.EventModel;
import guitesting.util.JFCUtil;
import guitesting.util.TestProperty;

import java.awt.Component;
import java.awt.Window;
import java.io.File;

import javax.swing.SwingUtilities;

/**
 * monitors.save_image.output_folder <br>
 * monitors.save_image.window_file_name<br>
 * monitors.save_image.component_file_name<br>
 * monitors.save_image.screen_file_name <br>
 * 
 * @author gigony
 * 
 */
public class SaveImageMonitor implements IExecutionMonitor {
  private String monitorOutputFolder;

  public SaveImageMonitor() {

    monitorOutputFolder = TestProperty.propertyStore.get("monitors.save_image.output_folder",
        TestProperty.getFolder("result").getAbsolutePath());

  }

  @Override
  public void beforeExecution(Object testcase, GUIModel guiModel) {

  }

  @Override
  public void beforeStep(Object testcase, int stepIndex, EventModel event, GUIModel guiModel) {

  }

  @Override
  public void afterStep(Object testcaseObj, int stepIndex, EventModel event, GUIModel guiModel, boolean isExited) {
    EventListModel testcase = (EventListModel) testcaseObj;
    if (testcase.getEventCount() - 1 == stepIndex) {
      Component component = (Component) testcase.getEvent(stepIndex).getComponentModel().getRef();
      if (component != null) {
        Window window = null;
        if (component instanceof Window)
          window = (Window) component;
        else
          window = SwingUtilities.getWindowAncestor(component);
        if (window != null)
          JFCUtil.saveComponentImage(
              window,
              new File(monitorOutputFolder, String.format("%s_%d.jpg",
                  TestProperty.propertyStore.get("monitors.save_image.window_file_name", "windowImage"), stepIndex))
                  .getAbsolutePath(), "jpg");
        JFCUtil.saveComponentImage(
            component,
            new File(monitorOutputFolder, String.format("%s_%d.jpg",
                TestProperty.propertyStore.get("monitors.save_image.component_file_name", "windowImage"), stepIndex))
                .getAbsolutePath(), "jpg");

      }
    }
  }

  @Override
  public void afterExecution(Object testcase, EventModel event, GUIModel guiModel, boolean isExited) {
    JFCUtil.saveWindowsImage(
        guiModel,
        new File(monitorOutputFolder, String.format("%s.jpg",
            TestProperty.propertyStore.get("monitors.save_image.screen_file_name", "screenImage"))).getAbsolutePath(),
        "jpg");
  }

}
