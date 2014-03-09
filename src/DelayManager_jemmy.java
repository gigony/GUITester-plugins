import guitesting.engine.delaymanager.DelayManager;
import guitesting.util.TestLogger;

import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;

/**
 * 
 * @author Gigon Bae
 *
 */
public class DelayManager_jemmy extends DelayManager {
  private static final long TIME_OUT = 5000;
  QueueTool queueTool;

  public DelayManager_jemmy() {
    queueTool = new QueueTool();
    Timeouts timeOut = queueTool.getTimeouts();
    timeOut.setTimeout("QueueTool.WaitQueueEmptyTimeout", TIME_OUT);
  }

  @Override
  public boolean delayInitialTime(long initialDelay) {
    return delayTime(initialDelay);
  }

  @Override
  public boolean delayEventIntervalTime(long executionDelay) {
    return delayTime(executionDelay);
  }

  @Override
  public boolean delayTime(long time) {
    try {
      queueTool.waitEmpty(time);
    } catch (TimeoutExpiredException e) {
      TestLogger.info(String.format("WaitQueueEmptyTimeout time(%d) is reached. Skip..", TIME_OUT));
      return false;
    }
    return true;
  }

}
