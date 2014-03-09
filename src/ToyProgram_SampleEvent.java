import guitesting.model.ComponentModel;
import guitesting.model.event.EventModel;

import javax.swing.JButton;
import javax.swing.JComponent;

import org.netbeans.jemmy.operators.JComponentOperator;
/**
 * 
 * @author Gigon Bae
 *
 */
public class ToyProgram_SampleEvent extends EventModel {

  public ToyProgram_SampleEvent(ComponentModel model) {
    super(model);
  }

  public String getEventTypeName() {
    return "click draw button";
  }

  public boolean isSupportedBy(ComponentModel model) {
    if(model==null || model.getRef()==null)
      return false;
    if ((model.getRef() instanceof JButton) && "Draw".equals(model.get("title")))
      return true;

    return false;
  }
  @Override
  public int getValueHash() {
    return 0;
  }

  public boolean stopChainingIfSupported() {
    return true;
  }

  public String getActionPropertyString() {
    return "click";
  }

  public void performImpl(Object... args) {
    new JComponentOperator((JComponent) getComponentModel().getRef()).clickMouse();
  }

}
