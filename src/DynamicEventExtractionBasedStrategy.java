import guitesting.engine.appmanager.ApplicationManager;
import guitesting.engine.delaymanager.DelayManager;
import guitesting.engine.strategy.AbstractStrategy;
import guitesting.engine.testcasegenerator.ExecutedTestCaseMetaData;
import guitesting.model.ComponentModel;
import guitesting.model.GUIModel;
import guitesting.model.WindowModel;
import guitesting.model.event.EventModel;
import guitesting.model.traces.EventExecutionResult;
import guitesting.ui.GUITester;
import guitesting.util.IOUtil;
import guitesting.util.JFCUtil;
import guitesting.util.MySecurityManager;
import guitesting.util.TestLogger;
import guitesting.util.TestProperty;
import guitesting.util.TimeCounter;

import java.awt.Component;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

import com.google.common.hash.HashCode;

import net.sourceforge.cobertura.coveragedata.ProjectData;

/**
 * 
 * @author Gigon Bae
 *
 */
public class DynamicEventExtractionBasedStrategy extends AbstractStrategy {
  public static final int SYSTEM_INTERACTION_EVENT = 1;
  public static final int TERMINATION_EVENT = 2;
  public static final int UNRESTRICTED_FOCUS_EVENT = 3;
  public static final int RESTRICTED_FOCUS_EVENT = 4;

  ApplicationManager appManager = null;
  DelayManager delayManager = null;
  final MySecurityManager securityManager = new MySecurityManager();

  Random random = null;
  TimeCounter timeCounter = null;
  int eventExecutionCounter = 0;

  ArrayList<EventExecutionResult> traceList;
  ProjectData accumulatedCoverage = null;

  EventExecutionResult executionResult = null;
  int testCaseIndex;
  int testCaseLength;
  int localCount;
  int nonexecutableIndex;
  int failureIndex;

  final static Object lockObject = new Object();

  public void run(String[] mainArgs) {
    // initialize
    initialize(mainArgs);

    // launch GUITester
    GUITester.getInstance().launchGUITesterUI(null);

    // test case execution
    testCaseExecution();

    TestLogger.info("--------- Elapsed time: %s ms ---------", timeCounter.getElapsedTime());

    System.exit(0);

  }

  private void testCaseExecution() {
    timeCounter.reset();
    timeCounter.start();
    // run program
    appManager.runApp();

    // initial delay
    delayManager.delayInitialTime();

    // flush initial coverage
    CoberturaController.getAndInitCoverageInfo();

    testCaseLength = TestProperty.propertyStore.getInt("N");

    
    for (int index = 1; index <= testCaseLength; index++) {
      localCount++;

      GUIModel guiModel = GUITester.getInstance().getGuiModelExtractor().getGUIModel();
      List<EventModel> availableEvents = guiModel.getEvents();

      if (availableEvents.size() == 0) {
        TestLogger.info("Event index %d : no events exist!", localCount);
        nonexecutableIndex = localCount;
        for (EventModel availableEvent : availableEvents) {
          TestLogger.debug("     #%s(%s)", availableEvent.getEventName(), availableEvent.getHashCode());
        }
        saveExecutionTrace(localCount, EventExecutionResult.EVENT_NOT_FOUND);
        break;
      }

      EventModel nextEvent = availableEvents.get(random.nextInt(availableEvents.size()));

      TestLogger.info("Event index %d : %s(%s) is selected...", localCount, nextEvent.toString(), nextEvent.getHashCode());

      executionResult = createExecutionTrace(nextEvent);
      // execute event
      nextEvent.perform();
      delayManager.delayEventIntervalTime();
      synchronized (lockObject) {
        saveExecutionTrace(localCount, EventExecutionResult.EVENT_SUCCEED);
      }
    }
    synchronized (lockObject) {
      // save final info
      saveFinalInfo();
    }

  }

  private void saveExecutionTrace(int localIndex, int status) {
    saveExecutionTrace(localIndex, status, null);
  }

  private void saveExecutionTrace(int localIndex, int status, Throwable t) {
    long currentTime = timeCounter.stop();

    switch (status) {
    case EventExecutionResult.EVENT_SUCCEED:
      // save coverage for this event
      ProjectData cov = CoberturaController.getAndInitCoverageInfo(); // flush current coverage info
      accumulatedCoverage.merge(cov);

      File coverageFolder = getCoverage4TestCaseFolder(testCaseIndex);
      coverageFolder.mkdirs();
      String coverageFileName = String.format("%07d_%04d.zip", testCaseIndex, localIndex);
      File coverageFile = new File(coverageFolder, coverageFileName);
      IOUtil.saveObjectToZippedXML(accumulatedCoverage, coverageFile.getAbsolutePath());

      // save event execution end time
      traceList.get(localIndex - 1).setEndTime(currentTime);
      // save GUI hashing

      GUIModel guiModel = GUITester.getInstance().getGuiModelExtractor().getGUIModel();
      traceList.get(localIndex - 1).setGuiStateHashingValue(getHashValue4GUIState(guiModel));
      break;
    case EventExecutionResult.EVENT_FAILED:
      // save coverage for this event
      cov = CoberturaController.getAndInitCoverageInfo(); // flush current coverage info
      accumulatedCoverage.merge(cov);

      coverageFolder = getCoverage4TestCaseFolder(testCaseIndex);
      coverageFolder.mkdirs();
      coverageFileName = String.format("%07d_%04d.zip", testCaseIndex, localIndex);
      coverageFile = new File(coverageFolder, coverageFileName);
      IOUtil.saveObjectToZippedXML(accumulatedCoverage, coverageFile.getAbsolutePath());

      // save event execution end time
      traceList.get(localIndex - 1).setEndTime(currentTime);

      // set failed index
      failureIndex = localIndex;

      if (t != null) {
        try {
          // create file
          String errorPath = new File(TestProperty.getFolder("error"), String.format("%07d_%04d.txt", testCaseIndex,
              localCount)).getAbsolutePath();
          FileWriter fstream = new FileWriter(errorPath, false);
          PrintWriter pw = new PrintWriter(fstream);
          t.printStackTrace(pw);
          // close the output stream
          pw.close();
        } catch (Exception e) {// Catch exception if any
          TestLogger.error("Error: " + e.getMessage());
        }
      }

      break;
    case EventExecutionResult.EVENT_NOT_FOUND:
      // set nonexecutable index
      nonexecutableIndex = localIndex;
    }

    timeCounter.start();
  }

  private void saveFinalInfo() {
    long elapsedTime = timeCounter.stop();

    // metadata
    ExecutedTestCaseMetaData metadata = new ExecutedTestCaseMetaData();
    metadata.setTestCaseIndex(testCaseIndex);
    metadata.setInitTimeDelay(TestProperty.propertyStore.getInt("delay_manager.init_delay"));
    metadata.setExecutionTimeDelay(TestProperty.propertyStore.getInt("delay_manager.execution_delay"));
    metadata.setElapsedTime(elapsedTime);
    metadata.setFailureIndex(failureIndex);
    metadata.setUnexecutableIndex(nonexecutableIndex);
    metadata.setLength(testCaseLength);
    metadata.setExecutedLength(traceList.size());

    // save metadata
    IOUtil.saveObject(metadata,
        new File(TestProperty.getFolder("metadata"), String.format("%07d_meta.ser", testCaseIndex)).getAbsolutePath());

    // print metadata
    TestLogger.info(metadata.printString());

    // save trace data
    IOUtil.saveObjectToZippedXML(traceList,
        new File(TestProperty.getFolder("trace"), String.format("%07d.zip", testCaseIndex)).getAbsolutePath());

  }

  private EventExecutionResult createExecutionTrace(EventModel nextEvent) {
    timeCounter.stop();
    long eventStartTime = timeCounter.getElapsedTime();

    // flush current coverage info
    ProjectData cov = CoberturaController.getAndInitCoverageInfo();
    accumulatedCoverage.merge(cov);

    // set the default end time to the start time
    EventExecutionResult result = new EventExecutionResult(nextEvent, 0, testCaseIndex, localCount, eventStartTime,
        eventStartTime);

    // save component image
    File compFile = new File(TestProperty.getFolder("img.component"), String.format("%s.png",
        result.getComponentHashCode()));
    if (nextEvent.getComponentModel().getRef() instanceof Component) {
      if (!compFile.exists())
        JFCUtil.saveComponentImage((Component) nextEvent.getComponentModel().getRef(), compFile.getAbsolutePath(),
            "png");
    }

    // save window image
    File windowFile = new File(TestProperty.getFolder("img.window"), String.format("%s.png",
        result.getComponentHashCode()));
    if (nextEvent.getWindowModel().getRef() instanceof Component) {
      if (!windowFile.exists())
        JFCUtil
            .saveComponentImage((Component) nextEvent.getWindowModel().getRef(), windowFile.getAbsolutePath(), "png");
    }

    traceList.add(result);

    timeCounter.start();
    return result;

  }

  private void initialize(String[] mainArgs) {
    // setup parameters
    random = new Random();

    // File cleanCoverageFile = new File(TestProperty.getProjectFolder(), TestProperty.cleanCoverageFileName);
    accumulatedCoverage = new ProjectData();// (ProjectData) IOUtil.loadObject(cleanCoverageFile.getAbsolutePath());
    traceList = new ArrayList<EventExecutionResult>();

    GUITester tester = GUITester.getInstance();
    appManager = tester.getApplicationManager();
    delayManager = tester.getDelayManager();

    timeCounter = new TimeCounter();

    IOUtil.configureWorkspaceFiles();

    testCaseIndex = TestProperty.propertyStore.getInt("run_index");
    localCount = 0;
    nonexecutableIndex = 0;
    failureIndex = 0;
    testCaseLength = 0;

    securityManager.disableExitMethod(true);

    // add default exception handler
    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      public void uncaughtException(Thread t, Throwable e) {
        Thread.setDefaultUncaughtExceptionHandler(null);
        synchronized (lockObject) {
          e.printStackTrace();
          long eventEndTime = timeCounter.stop();
          executionResult.setEndTime(eventEndTime);
          saveExecutionTrace(localCount, EventExecutionResult.EVENT_FAILED, e);

          // save final info
          saveFinalInfo();

          securityManager.disableExitMethod(false);
          System.exit(0);
        }
      }
    });

  }

  public static File getCoverage4TestCaseFolder(int runIndex) {
    File coverageFolder = TestProperty.getFolder("coverage");
    File bucketFolder = new File(coverageFolder, String.format("%d", runIndex / 10000));
    File coverage4TestCaseFolder = new File(bucketFolder, String.format("%d", runIndex));
    return coverage4TestCaseFolder;
  }

  //TODO implement with guava
  public static String[] getHashValue4GUIState(GUIModel root) {
    String hashString[] = { "", "", "" };
    final String[] additionalProperty = { "enabled", "text", "width", "height", "opaque", "alignmentx", "alignmenty",
        "backgroundset", "foregroundset", "tooltiptext", "background", "foreground" };
    final String[] additionalProperty2 = { "title", "class", "componentIndex", "name", "role", "ancestorlisteners",
        "componentlisteners", "containerlisteners", "focuslisteners", "hierarchyboundslisteners", "hierarchylisteners",
        "inputmethodlisteners", "keylisteners", "mouselisteners", "mousemotionlisteners", "mousewheellisteners",
        "propertychangelisteners", "vetoablechangelisteners", "layout", "x", "y", "width", "height", "opaque",
        "enabled", "displayable", "visible", "focusable", "focustraversable", "showing", "cursorset", "componentcount",
        "alignmentx", "alignmenty", "backgroundset", "foregroundset", "background", "foreground", "font", "fontset",
        "tooltiptext", "text", "modalblocked" };
    ArrayList<ComponentModel> componentSet = new ArrayList<ComponentModel>();

    // get component node
    for (Enumeration<GUIModel> enumurator = root.breadthFirstEnumeration(); enumurator.hasMoreElements();) {
      GUIModel node = enumurator.nextElement();
      Object userObj = node.getUserObject();
      if (userObj instanceof ComponentModel) {
        ComponentModel element = (ComponentModel) userObj;
        componentSet.add(element);
      }
    }

    Collections.sort(componentSet, new Comparator<ComponentModel>() {
      @Override
      public int compare(ComponentModel o1, ComponentModel o2) {
        int winID1 = o1.getWindowModel() == null ? 0 : HashCode.fromString(o1.getWindowModel().get("id")).asInt();
        int winID2 = o2.getWindowModel() == null ? 0 : HashCode.fromString(o2.getWindowModel().get("id")).asInt();
        if (winID1 < winID2)
          return -1;
        if (winID1 > winID2)
          return 1;

        int compID1 = HashCode.fromString(o1.get("id")).asInt();
        int compID2 = HashCode.fromString(o2.get("id")).asInt();
        if (compID1 < compID2)
          return -1;
        if (compID1 > compID2)
          return 1;

        for (String property : additionalProperty) {
          String prop1 = o1.get(property);
          if (prop1 == null)
            prop1 = "";
          String prop2 = o2.get(property);
          if (prop2 == null)
            prop2 = "";
          if (prop1.compareTo(prop2) != 0)
            return prop1.compareTo(prop2);

        }
        return 0;
      }
    });

    // init hashing
    ByteArrayOutputStream buf = null;
    try {
      buf = new ByteArrayOutputStream(1024);
      // hashing component set
      for (ComponentModel component : componentSet) {
        int winID = component.getWindowModel() == null ? 0 : HashCode.fromString(component.getWindowModel().get("id")).asInt();
        int compID = HashCode.fromString(component.get("id")).asInt();
        buf.write(IOUtil.intToByteArray(winID));
        buf.write(IOUtil.intToByteArray(compID));

        for (String property : additionalProperty) {
          String prop = component.get(property);
          if (prop == null)
            prop = "";
          buf.write(prop.getBytes());
        }

      }
      buf.close();
    } catch (IOException e) {
      e.printStackTrace();
      TestLogger.error(e.getMessage());
    }

    Collections.sort(componentSet, new Comparator<ComponentModel>() {
      @Override
      public int compare(ComponentModel o1, ComponentModel o2) {
        int winID1 = o1.getWindowModel() == null ? 0 : HashCode.fromString(o1.getWindowModel().get("id")).asInt();
        int winID2 = o2.getWindowModel() == null ? 0 : HashCode.fromString(o2.getWindowModel().get("id")).asInt();
        if (winID1 < winID2)
          return -1;
        if (winID1 > winID2)
          return 1;

        int compID1 = HashCode.fromString(o1.get("id")).asInt();
        int compID2 = HashCode.fromString(o2.get("id")).asInt();
        if (compID1 < compID2)
          return -1;
        if (compID1 > compID2)
          return 1;

        for (String property : additionalProperty2) {
          String prop1 = o1.get(property);
          if (prop1 == null)
            prop1 = "";
          String prop2 = o2.get(property);
          if (prop2 == null)
            prop2 = "";
          if (prop1.compareTo(prop2) != 0)
            return prop1.compareTo(prop2);

        }
        return 0;
      }
    });

    // init hashing
    ByteArrayOutputStream buf2 = null;
    try {
      buf2 = new ByteArrayOutputStream(1024);
      // hashing component set
      for (ComponentModel component : componentSet) {
        int winID = component.getWindowModel() == null ? 0 : HashCode.fromString(component.getWindowModel().get("id")).asInt();
        int compID = HashCode.fromString(component.get("id")).asInt();
        buf2.write(IOUtil.intToByteArray(winID));
        buf2.write(IOUtil.intToByteArray(compID));

        for (String property : additionalProperty2) {
          String prop = component.get(property);
          if (prop == null)
            prop = "";
          buf2.write(prop.getBytes());
        }

      }
      buf2.close();
    } catch (IOException e) {
      TestLogger.error(e.getMessage());
    }

    byte[] bufArray = buf.toByteArray();
    byte[] bufArray2 = buf2.toByteArray();
    hashString[0] = IOUtil.getSHA_256(bufArray, bufArray.length);
    hashString[1] = IOUtil.getSHA_256(bufArray2, bufArray2.length);

    ByteArrayOutputStream buf3 = new ByteArrayOutputStream(1024);
    ArrayList<WindowModel> windowSet = new ArrayList<WindowModel>();
    // get windows (do not sort)
    for (Enumeration<GUIModel> enumurator = (Enumeration<GUIModel>) root.children(); enumurator.hasMoreElements();) {
      GUIModel node = enumurator.nextElement();
      Object userObj = node.getUserObject();
      if (userObj instanceof WindowModel) {
        WindowModel element = (WindowModel) userObj;
        windowSet.add(element);
        try {
          buf3.write(GUITester.getInstance().getGuiModelExtractor().getImageBuffer(element));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    byte[] bufArray3 = buf3.toByteArray();
    hashString[2] = IOUtil.getSHA_256(bufArray3, bufArray3.length);

    return hashString;

  }
}
