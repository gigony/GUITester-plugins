import guitesting.model.ComponentModel;
import guitesting.model.event.EventModel;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 
 * @author Gigon Bae
 *
 */
public class TerpPaint_DragEvent extends EventModel {

  public TerpPaint_DragEvent(ComponentModel model) {
    super(model);
  }

  @Override
  public String getEventTypeName() {
    return "drag_canvas";
  }

  @Override
  public boolean isSupportedBy(ComponentModel model) {
    return model.getRef().getClass().getName().equals("main_canvas");
  }
  @Override
  public int getValueHash() {
    return 0;
  }

  @Override
  public boolean stopChainingIfSupported() {
    return true;
  }

  @Override
  public void performImpl(Object... args) {
    // Point result = new Point(0, 0);
    // SwingUtilities.convertPointToScreen(result, (Component) componentModel.getRef());

    Component canvasObj = ((Component) componentModel.getRef());

    Class<?> canvas;
    Component pictureScrollPane = null;
    int w = 32;
    int h = 32;
    try {
      canvas = Class.forName("main_canvas");
      Field field = canvas.getField("pictureScrollPane");
      pictureScrollPane = (Component) field.get(canvasObj);
      Method getBufferedImage = canvas.getMethod("getBufferedImage", new Class[0]);
      BufferedImage bufferedImage = (BufferedImage) getBufferedImage.invoke(canvasObj, new Object[0]);
      w = bufferedImage.getWidth();
      h = bufferedImage.getHeight();
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      MouseEvent mouseMoveEvent = new MouseEvent(pictureScrollPane, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(),
          16, 1, 1, 1, false, MouseEvent.BUTTON1);
      pictureScrollPane.dispatchEvent(mouseMoveEvent);
      MouseEvent mousePressEvent = new MouseEvent(pictureScrollPane, MouseEvent.MOUSE_PRESSED,
          System.currentTimeMillis(), 16, 1, 1, 1, false, MouseEvent.BUTTON1);
      pictureScrollPane.dispatchEvent(mousePressEvent);
      MouseEvent mouseDragEvent = new MouseEvent(pictureScrollPane, MouseEvent.MOUSE_DRAGGED,
          System.currentTimeMillis(), 16, w / 4, h / 4, 0, false, MouseEvent.BUTTON1);
      pictureScrollPane.dispatchEvent(mouseDragEvent);
      mouseDragEvent = new MouseEvent(pictureScrollPane, MouseEvent.MOUSE_DRAGGED, System.currentTimeMillis(), 16,
          w / 2, h / 2, 0, false, MouseEvent.BUTTON1);
      pictureScrollPane.dispatchEvent(mouseDragEvent);
      MouseEvent mouseReleaseEvent = new MouseEvent(pictureScrollPane, MouseEvent.MOUSE_RELEASED,
          System.currentTimeMillis(), 16, w / 2, h / 2, 1, false, MouseEvent.BUTTON1);
      pictureScrollPane.dispatchEvent(mouseReleaseEvent);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
