import guitesting.model.ComponentModel;
import guitesting.model.event.EventModel;
import guitesting.util.TestProperty;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JComponent;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
/**
 * 
 * @author Gigon Bae
 *
 */
public class TerpPresent_Drag_Mouse extends EventModel {
  protected String chooseText = "";

  public TerpPresent_Drag_Mouse(ComponentModel model, String value) {
    super(model);
    setChooseText(value);
  }

  public TerpPresent_Drag_Mouse(ComponentModel model) {
    super(model);

    if (model != null) {
      Map<String, String> customValues = TestProperty.eventValueManager.getMatchedCustomValues(this);
      ArrayList<String> targetValues = new ArrayList<String>();

      isContainer = true;

      if (!customValues.isEmpty()) {
        for (int i = 1;; i++) {
          String value = customValues.get("v" + i);
          if (value != null) {
            targetValues.add(value);
          } else
            break;
        }
      }

      if (targetValues.size() == 0) {
        // default line
        targetValues.add("20 20 100 100");
        targetValues.add("100 100 30 30");
      }

      children = new ArrayList<EventModel>(targetValues.size());
      int index = 1;
      for (String value : targetValues) {
        getPropertyModel().put("v" + index, value);
        children.add(new TerpPresent_Drag_Mouse(model, value));
        index++;
      }

    }
  }

  public void setChooseText(String chooseText) {
    this.chooseText = chooseText;
  }

  @Override
  public String getEventTypeName() {
    return "drag_canvas";
  }

  @Override
  public int getValueHash() {
    return Hashing.crc32().hashString(chooseText, Charsets.UTF_8).asInt();
  }

  @Override
  public boolean isSupportedBy(ComponentModel model) {
    if (!model.getRef().getClass().getName().equals("GPGraph"))
      return false;

    if (Integer.parseInt(model.get("width")) <= 0 || Integer.parseInt(model.get("height")) <= 0)
      return false;

    return true;
  }

  @Override
  public boolean stopChainingIfSupported() {
    return true;
  }

  @Override
  public void performImpl(Object... args) {
    JComponent canvasPane = ((JComponent) componentModel.getRef());
    String[] pointStrings = chooseText.split(" ");
    int[] points = new int[pointStrings.length];
    for (int i = 0; i < pointStrings.length; i++) {
      points[i] = Integer.parseInt(pointStrings[i]);
    }
    try {
      MouseEvent mouseMoveEvent = new MouseEvent(canvasPane, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 16,
          points[0], points[1], 1, false, MouseEvent.BUTTON1);
      canvasPane.dispatchEvent(mouseMoveEvent);
      MouseEvent mousePressEvent = new MouseEvent(canvasPane, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 16,
          points[0], points[1], 1, false, MouseEvent.BUTTON1);
      canvasPane.dispatchEvent(mousePressEvent);
      MouseEvent mouseDragEvent = new MouseEvent(canvasPane, MouseEvent.MOUSE_DRAGGED, System.currentTimeMillis(), 16,
          points[2], points[3], 0, false, MouseEvent.BUTTON1);
      canvasPane.dispatchEvent(mouseDragEvent);
      MouseEvent mouseReleaseEvent = new MouseEvent(canvasPane, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(),
          16, points[2], points[3], 1, false, MouseEvent.BUTTON1);
      canvasPane.dispatchEvent(mouseReleaseEvent);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  // --- implement Converter interface ---
  @Override
  public boolean canConvert(Class clazz) {
    return TerpPresent_Drag_Mouse.class.equals(clazz);
  }

  @Override
  protected void marshalAttributes(EventModel modelEvent, HierarchicalStreamWriter writer) {
    super.marshalAttributes(modelEvent, writer);
    if (((TerpPresent_Drag_Mouse) modelEvent).chooseText != null) {
      writer.startNode("chooseText");
      writer.setValue("" + ((TerpPresent_Drag_Mouse) modelEvent).chooseText);
      writer.endNode();
    }

  }

  @Override
  protected void unmarshalAttributes(EventModel event, HierarchicalStreamReader reader) {
    super.unmarshalAttributes(event, reader);
    if (reader.hasMoreChildren()) {
      reader.moveDown();
      ((TerpPresent_Drag_Mouse) event).chooseText = reader.getValue();
      reader.moveUp();
    }
  }
}
