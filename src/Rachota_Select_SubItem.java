import guitesting.model.ComponentModel;
import guitesting.model.event.EventModel;
import guitesting.util.TestProperty;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
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
public class Rachota_Select_SubItem extends EventModel {
  protected String chooseText = "";

  // ArrayList<Object> taskList = new ArrayList<Object>();

  public Rachota_Select_SubItem(ComponentModel model, String value) {
    super(model);
    setChooseText(value);
    // taskList.addAll(srcTaskList);

  }

  public Rachota_Select_SubItem(ComponentModel model) {
    super(model);
    if (model != null) {
      Map<String, String> customValues = TestProperty.eventValueManager.getMatchedCustomValues(this);
      ArrayList<String> targetValues = new ArrayList<String>();

      isContainer = true;

      Component tableObj = ((Component) componentModel.getRef());

      Class<?> tableClass;
      try {
        tableClass = Class.forName("javax.swing.JTable");
        // Method getModel = tableClass.getMethod("getModel", new Class[0]);
        // Object tableModelObj = getModel.invoke(tableObj, new Object[0]);

        // Class<?> tableModelClass = tableModelObj.getClass();

        // Method getTask = tableModelClass.getMethod("getTask", new Class[] { int.class });

        if (!customValues.isEmpty()) {
          for (int i = 1;; i++) {
            String value = customValues.get("v" + i);
            if (value != null) {
              targetValues.add(value);
              // Object task = getTask.invoke(tableModelObj, new Object[] { Integer.parseInt(value) });
              // taskList.add(task);
            } else
              break;
          }
        }

        if (targetValues.size() == 0) {

          Method getRowCount = tableClass.getMethod("getRowCount", new Class[0]);
          Integer rowCount = (Integer) getRowCount.invoke(tableObj, new Object[0]);

          for (int i = 0; i < rowCount; i++) {
            targetValues.add("" + i);
            // Object task = getTask.invoke(tableModelObj, new Object[] { i });
            // taskList.add(task);
          }

        }

      } catch (Exception e) {
        e.printStackTrace();
      }

      children = new ArrayList<EventModel>(targetValues.size());
      int index = 1;
      for (String value : targetValues) {
        getPropertyModel().put("v" + index, value);
        children.add(new Rachota_Select_SubItem(model, value));
        index++;
      }

    }
  }

  public void setChooseText(String chooseText) {
    this.chooseText = chooseText;
  }

  @Override
  public String getEventTypeName() {
    return "select_sub_item_in_table";
  }
  
  @Override
  public int getValueHash() {
    return Hashing.crc32().hashString(chooseText, Charsets.UTF_8).asInt();
  }

  @Override
  public boolean isSupportedBy(ComponentModel model) {
    Accessible component = (Accessible) model.getRef();
    if (component != null)
      return component.getClass().getName().equals("javax.swing.JTable") && model.get("title").equals("Filters:");
    return false;
  }

  @Override
  public boolean stopChainingIfSupported() {
    return true;
  }

  @Override
  public void performImpl(Object... args) {
    Component tableObj = ((Component) componentModel.getRef());
    try {

      Rectangle rect = ((JTable) tableObj).getCellRect(Integer.parseInt(chooseText), 0, true);
      // System.out.println(rect.x+" "+rect.y+" "+rect.width+" "+rect.height);
      int x = rect.x + rect.width / 2;
      int y = rect.y + rect.height / 2;

      // fireTableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
      // tableClass = Class.forName("javax.swing.JTable");
      // Method getModel = tableClass.getMethod("changeSelection", new Class[]
      // {int.class,int.class,boolean.class,boolean.class });
      // getModel.invoke(tableObj, Integer.parseInt(chooseText),0,false,false);
      MouseEvent mouseMoveEvent = new MouseEvent(tableObj, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 16, x,
          y, 1, false, MouseEvent.BUTTON1);
      tableObj.dispatchEvent(mouseMoveEvent);
      MouseEvent mousePressEvent = new MouseEvent(tableObj, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 16,
          x, y, 1, false, MouseEvent.BUTTON1);
      tableObj.dispatchEvent(mousePressEvent);
      MouseEvent mouseReleaseEvent = new MouseEvent(tableObj, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(),
          16, x, y, 1, false, MouseEvent.BUTTON1);
      tableObj.dispatchEvent(mouseReleaseEvent);
      MouseEvent mouseDragEvent = new MouseEvent(tableObj, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 16, x,
          y, 1, false, MouseEvent.BUTTON1);
      tableObj.dispatchEvent(mouseDragEvent);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  // --- implement Converter interface ---
  @Override
  public boolean canConvert(Class clazz) {
    return Rachota_Select_SubItem.class.equals(clazz);
  }

  @Override
  protected void marshalAttributes(EventModel modelEvent, HierarchicalStreamWriter writer) {
    super.marshalAttributes(modelEvent, writer);
    if (((Rachota_Select_SubItem) modelEvent).chooseText != null) {
      writer.startNode("chooseText");
      writer.setValue("" + ((Rachota_Select_SubItem) modelEvent).chooseText);
      writer.endNode();
    }

  }

  @Override
  protected void unmarshalAttributes(EventModel event, HierarchicalStreamReader reader) {
    super.unmarshalAttributes(event, reader);
    if (reader.hasMoreChildren()) {
      reader.moveDown();
      ((Rachota_Select_SubItem) event).chooseText = reader.getValue();
      reader.moveUp();
    }
  }
}
