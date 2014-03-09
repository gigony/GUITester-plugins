import guitesting.model.ComponentModel;
import guitesting.model.event.EventModel;

import javax.swing.JDialog;

import com.thoughtworks.xstream.annotations.XStreamAlias;
/**
 * 
 * @author Gigon Bae
 *
 */
@XStreamAlias("event")
public class TerpPresent_CloseAboutDialog extends EventModel {

  public TerpPresent_CloseAboutDialog(ComponentModel model) {
    super(model);
  }

  @Override
  public String getEventTypeName() {
    return "close_about_dialog";
  }

  @Override
  public boolean isSupportedBy(ComponentModel model) {
    return model.getRef().getClass().getName().equals("GPAboutDialog");
  }

  @Override
  public boolean stopChainingIfSupported() {
    return true;
  }

  @Override
  public int getValueHash() {
    return 0;
  }
  @Override
  public void performImpl(Object... args) {
    JDialog dialogObj = ((JDialog) componentModel.getRef());
    dialogObj.dispose();
  }

  @Override
  public boolean canConvert(Class clazz) {
    return TerpPresent_CloseAboutDialog.class.equals(clazz);
  }
}
