import guitesting.model.ComponentModel;
import guitesting.model.event.EventModel;
import guitesting.util.TestProperty;

import java.util.ArrayList;
import java.util.Map;

import javax.accessibility.Accessible;
import javax.swing.JTable;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
/**
 * 
 * @author Gigon Bae
 *
 */
public class TerpSpreadSheet_Select_Cell extends EventModel {
  protected String chooseText = "";

  public TerpSpreadSheet_Select_Cell(ComponentModel model, String value) {
    super(model);
    setChooseText(value);

  }

  public TerpSpreadSheet_Select_Cell(ComponentModel model) {
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
        targetValues.add("1 1 2 3");
        targetValues.add("2 5 2 5");
        targetValues.add("4 1 4 1");
      }

      children = new ArrayList<EventModel>(targetValues.size());
      int index = 1;
      for (String value : targetValues) {
        getPropertyModel().put("v" + index, value);
        children.add(new TerpSpreadSheet_Select_Cell(model, value));
        index++;
      }

    }
  }

  public void setChooseText(String chooseText) {
    this.chooseText = chooseText;
  }

  @Override
  public String getEventTypeName() {
    return "select_cell";
  }

  @Override
  public int getValueHash() {
    return Hashing.crc32().hashString(chooseText, Charsets.UTF_8).asInt();
  }

  @Override
  public boolean isSupportedBy(ComponentModel model) {
    Accessible component = (Accessible) model.getRef();
    if (component != null)
      return component.getClass().getName().equals("javax.swing.JTable");
    return false;
  }

  @Override
  public boolean stopChainingIfSupported() {
    return true;
  }

  @Override
  public void performImpl(Object... args) {

    JTable table = ((JTable) componentModel.getRef());

    String[] pointStrings = chooseText.split(" ");
    int[] points = new int[pointStrings.length];
    for (int i = 0; i < pointStrings.length; i++) {
      points[i] = Integer.parseInt(pointStrings[i]);
    }
    table.setColumnSelectionInterval(points[0], points[2]);
    table.setRowSelectionInterval(points[1] - 1, points[3] - 1);

  }

  // --- implement Converter interface ---
  @Override
  public boolean canConvert(Class clazz) {
    return TerpSpreadSheet_Select_Cell.class.equals(clazz);
  }

  @Override
  protected void marshalAttributes(EventModel modelEvent, HierarchicalStreamWriter writer) {
    super.marshalAttributes(modelEvent, writer);
    if (((TerpSpreadSheet_Select_Cell) modelEvent).chooseText != null) {
      writer.startNode("chooseText");
      writer.setValue("" + ((TerpSpreadSheet_Select_Cell) modelEvent).chooseText);
      writer.endNode();
    }

  }

  @Override
  protected void unmarshalAttributes(EventModel event, HierarchicalStreamReader reader) {
    super.unmarshalAttributes(event, reader);
    if (reader.hasMoreChildren()) {
      reader.moveDown();
      ((TerpSpreadSheet_Select_Cell) event).chooseText = reader.getValue();
      reader.moveUp();
    }
  }
}
