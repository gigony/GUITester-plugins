import guitesting.model.ComponentModel;
import guitesting.model.event.EventModel;

import java.awt.Component;
import java.awt.event.InputEvent;

import org.netbeans.jemmy.operators.ComponentOperator;
/**
 * 
 * @author Gigon Bae
 *
 */
public class TerpPaint_RightClick extends EventModel {

  public TerpPaint_RightClick(ComponentModel model) {
    super(model);
  }

  @Override
  public String getEventTypeName() {
    return "right_click";
  }

  @Override
  public boolean isSupportedBy(ComponentModel model) {
    String className = model.get("class");
    String titleName = model.get("title");
    String actionCommand = model.get("actioncommand");
    String width = model.get("width");
    String height = model.get("height");

    if (className.equals("javax.swing.JButton") && titleName.startsWith("JButton[") && actionCommand.equals("line")
        && width.equals("16") && height.equals("16"))
      return true;

    return false;
  }
  
  @Override
  public int getValueHash() {
    return 0;
  }

  @Override
  public boolean stopChainingIfSupported() {
    return false;
  }

  @Override
  public void performImpl(Object... args) {
    Component comp = (Component) getComponentModel().getRef();
    new ComponentOperator(comp).clickMouse(1, InputEvent.BUTTON3_MASK);
  }
}
