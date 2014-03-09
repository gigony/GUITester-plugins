import guitesting.engine.EventFilter;
import guitesting.engine.appmanager.ApplicationManager;
import guitesting.engine.appmanager.ApplicationManager_JFC;
import guitesting.engine.delaymanager.DelayManager;
import guitesting.engine.delaymanager.DelayManager_JFC;
import guitesting.engine.strategy.AbstractStrategy;
import guitesting.engine.windowmonitor.WindowMonitor;
import guitesting.engine.windowmonitor.WindowMonitor_JFC;
import guitesting.model.ComponentModel;
import guitesting.model.EventListModel;
import guitesting.model.GUIModel;
import guitesting.model.WindowModel;
import guitesting.model.event.EventModel;
import guitesting.model.graph.EventNode;
import guitesting.ui.GUITester;
import guitesting.ui.UIAction;
import guitesting.util.IOUtil;
import guitesting.util.MySecurityManager;
import guitesting.util.TestLogger;
import guitesting.util.TestProperty;
import guitesting.util.TimeCounter;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;

import net.sourceforge.cobertura.coveragedata.ProjectData;

import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.IntegerEdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.VertexNameProvider;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
/**
 * 
 * @author Gigon Bae
 *
 */
public class EFGRipperStrategy extends AbstractStrategy implements UIAction {
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

  // ArrayList<ExecutedEventModel> traceList;
  ProjectData accumulatedCoverage = null;

  WindowMonitor winMonitor = null;

  HashSet<EventNode> rootEvents;
  HashMap<WindowModel, WindowModel> rippedWindows;
  HashMap<WindowModel, HashSet<EventModel>> eventsInWindow;

  HashMap<ComponentModel, HashSet<EventModel>> availableEventsWhenAvailable;
  HashMap<EventModel, HashSet<EventModel>> availableEventsAfterInvoked;
  HashMap<WindowModel, HashSet<ComponentModel>> invokingComponents;
  // HashMap<ComponentModel, HashSet<ComponentModel>> invokedComponents;
  HashMap<EventModel, Integer> eventTypes;
  EventFilter terminationEventFilter;

  final static Object lockObject = new Object();
  private HashSet<EventModel> rootSet;
  HashMap<EventModel, HashSet<WindowModel>> eventNodeWindowInvokeMap = new HashMap<EventModel, HashSet<WindowModel>>();

  public void run(String[] mainArgs) {
    // initialize
    initialize(mainArgs);

    // launch GUITester
    GUITester.getInstance().launchGUITesterUI(this);

    // set termination events filter
    String filterListFilePath = new File(TestProperty.getFolder("project"), TestProperty.propertyStore.get(
        "termination_event_filename", "")).getAbsolutePath();

    terminationEventFilter.setFilter((EventListModel) IOUtil.loadObjectFromXML(filterListFilePath));

    timeCounter.reset();
    timeCounter.start();

    // rip GUIs
    ripGUIs();

    timeCounter.stop();
    TestLogger.info("--------- Elapsed time: %s ms ---------", timeCounter.getElapsedTime());
    TestLogger.info("--------- Number of executed events: %d ---------", eventExecutionCounter);

    // createEFG
    createEFG();
    timeCounter.stop();
    TestLogger.info("--------- Elapsed time: %s ms ---------", timeCounter.getElapsedTime());

    System.exit(0);

  }

  private void initialize(String[] mainArgs) {
    // setup parameters
    random = new Random();

    File cleanCoverageFile = new File(TestProperty.getFolder("project"), TestProperty.CleanCoverageFileName);

    accumulatedCoverage = (ProjectData) IOUtil.loadObject(cleanCoverageFile.getAbsolutePath());
    // traceList = new ArrayList<ExecutedEventModel>();

    rippedWindows = new HashMap<WindowModel, WindowModel>();
    eventsInWindow = new HashMap<WindowModel, HashSet<EventModel>>();
    availableEventsWhenAvailable = new HashMap<ComponentModel, HashSet<EventModel>>();
    availableEventsAfterInvoked = new HashMap<EventModel, HashSet<EventModel>>();
    invokingComponents = new HashMap<WindowModel, HashSet<ComponentModel>>();
    // invokedComponents = new HashMap<ComponentModel, HashSet<ComponentModel>>();
    eventTypes = new HashMap<EventModel, Integer>();
    appManager = new ApplicationManager_JFC();
    delayManager = new DelayManager_JFC();
    winMonitor = new WindowMonitor_JFC();

    timeCounter = new TimeCounter();

    terminationEventFilter = new EventFilter();
    IOUtil.configureWorkspaceFiles();

    securityManager.disableExitMethod(true);

    // add default exception handler
    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      public void uncaughtException(Thread t, Throwable e) {
        synchronized (lockObject) {
          TestLogger.error("%s", e);
          e.printStackTrace();

          // timeCounter.stop();
          //
          // securityManager.disableExitMethod(false);
          // System.exit(0);
        }
      }
    });

  }

  private void ripGUIs() {

    // run program
    appManager.runApp();

    // initial delay
    delayManager.delayInitialTime();

    // save initial coverage
    ProjectData cov = CoberturaController.getAndInitCoverageInfo();
    accumulatedCoverage.merge(cov);
    IOUtil.saveObject(accumulatedCoverage,
        new File(TestProperty.getFolder("result"), "InitialEFGRipperCoverage.ser").getAbsolutePath());

    // GUIModel guiModel = tester.getGuiModelExtractor().getGUIModel(true); // include disabled widgets also
    GUIModel guiModel = GUITester.getInstance().getGuiModelExtractor().getGUIModel(true); // include disabled widgets
                                                                                          // also

    rootSet = new HashSet<EventModel>(guiModel.getEvents(false));

    // add all root events
    ArrayList<WindowModel> activeWindows = guiModel.getActiveWindowList();

    // for each active window
    for (WindowModel targetWindow : activeWindows) {
      ripWindow(targetWindow, null);
    }

    // save final coverage
    cov = CoberturaController.getAndInitCoverageInfo();
    accumulatedCoverage.merge(cov);

    printDebugInfo();

    securityManager.disableExitMethod(false);

  }

  private void ripWindow(WindowModel activeWindow, EventModel invokerEvent) {
    // GUIModel guiModel = tester.getGuiModelExtractor().getGUIModel(true); // include disabled widgets also
    GUIModel guiModel = GUITester.getInstance().getGuiModelExtractor().getGUIModel(true); // include disabled widgets
                                                                                          // also
    // List<EventModel> activeVisibleEvents = guiModel.getEvents(true);
    List<EventModel> activeVisibleEvents = guiModel.getEvents(false);

    updateInvokedComponents(activeVisibleEvents, invokerEvent);

    TestLogger.info("##RipWindow: %s", activeWindow.get("title"));
    if (!isRippedWindow(activeWindow)) {
      addRippedWindow(activeWindow);

      LinkedList<EventModel> eventWorkList = new LinkedList<EventModel>(activeVisibleEvents);

      while (!eventWorkList.isEmpty()) {
        EventModel event = eventWorkList.removeFirst();
        // ComponentModel componentModel = event.getComponentModel();
        TestLogger.info("  #Inspect event: %s", event);
        if (event.getWindowModel().equals(activeWindow)) {
          if (isExpendable(event)) {
            TestLogger.info("    #Expendable!");
            winMonitor.reset();
            winMonitor.start();

            event.perform();
            eventExecutionCounter++;

            delayManager.delayEventIntervalTime();

            winMonitor.stop();

            LinkedList<WindowModel> openedWindows = winMonitor.getOpenedWindows();
            boolean isWindowOpeningEvents = false;
            boolean isRestrictedFocusEvent = false;
            while (!openedWindows.isEmpty()) {

              WindowModel openedWindow = openedWindows.removeFirst();

              TestLogger.info("    #Opened: %s", openedWindow);
              if (GUITester.getInstance().getGuiModelExtractor().isAvailable(openedWindow)) {
                TestLogger.info("      #Available!");
                isWindowOpeningEvents = true;

                if (GUITester.getInstance().getGuiModelExtractor().isModalBlocked(activeWindow)) { // if the previous
                                                                                                   // active window is
                                                                                                   // blocked
                  isRestrictedFocusEvent = true;
                }
                ripWindow(openedWindow, event);

                winMonitor.closeWindow(openedWindow);

                // get additional information for graph
                HashSet<WindowModel> oWindows = eventNodeWindowInvokeMap.get(event);
                if (oWindows == null) {
                  oWindows = new HashSet<WindowModel>();
                  eventNodeWindowInvokeMap.put(event, oWindows);
                }
                oWindows.add(openedWindow);
              }
            }
            if (isWindowOpeningEvents) {
              if (isRestrictedFocusEvent)
                eventTypes.put(event, RESTRICTED_FOCUS_EVENT); // set restricted_focus
              else
                eventTypes.put(event, UNRESTRICTED_FOCUS_EVENT); // set unrestricted_focus
            } else {
              // GUIModel newGuiModel = tester.getGuiModelExtractor().getGUIModel(true);
              GUIModel newGuiModel = GUITester.getInstance().getGuiModelExtractor().getGUIModel(true);

              // LinkedList<EventModel> newActiveVisibleEvents = GUIModel.getEvents(newGuiModel, true);
              LinkedList<EventModel> newActiveVisibleEvents = GUIModel.getEvents(newGuiModel, false);
              LinkedList<EventModel> addedEvents = updateInvokedComponents(newActiveVisibleEvents, event);

              // if(!addedEvents.isEmpty()){
              // eventTypes.put(event, UNRESTRICTED_FOCUS_EVENT); // set unrestricted_focus when additional nodes are
              // created when this event is
              // executed
              // }

              // trace
              TestLogger.info_("      #Added:");
              for (EventModel addE : addedEvents) {
                TestLogger.info_("%s, ", addE);
              }
              TestLogger.info("");

              // add newly created events in this window.
              while (!addedEvents.isEmpty()) {
                eventWorkList.addFirst(addedEvents.removeLast());
              }
              updateWorkListInfo(eventWorkList, newActiveVisibleEvents); // for precise ripping (refresh event model
                                                                         // info)
            }
          }
        } else {
          TestLogger.info("    #Is not event in this window.");
        }
      }
    } else {
      TestLogger.info("  already ripped");
    }
    TestLogger.info("##Finish RipWindow: %s", activeWindow.get("title"));
  }

  private LinkedList<EventModel> updateInvokedComponents(List<EventModel> activeVisibleEvents, EventModel invokerEvent) {
    LinkedList<EventModel> addedEvents = new LinkedList<EventModel>();
    ComponentModel invokerComponent = null;
    if (invokerEvent != null)
      invokerComponent = invokerEvent.getComponentModel();

    for (EventModel activeVisibleEvent : activeVisibleEvents) {
      WindowModel winModel = activeVisibleEvent.getWindowModel();
      ComponentModel compModel = activeVisibleEvent.getComponentModel();
      // set available events when first invoked
      if (!availableEventsWhenAvailable.containsKey(compModel)) {
        HashSet<EventModel> availableEvents = new HashSet<EventModel>(activeVisibleEvents);
        availableEventsWhenAvailable.put(compModel, availableEvents);
      }

      // add invoker components
      HashSet<ComponentModel> invokingComps = invokingComponents.get(winModel);
      if (invokingComps == null) {
        invokingComps = new HashSet<ComponentModel>();
        invokingComponents.put(winModel, invokingComps);
      }
      if (invokerComponent != null) {
        if (!compModel.getWindowModel().equals(invokerComponent.getWindowModel())) {
          TestLogger.info("\t\t\t#set invoking comp:%s -->%s", invokerComponent, winModel);
          invokingComps.add(invokerComponent);
        }
      }

      // add default event type
      Integer eventType = eventTypes.get(activeVisibleEvent);
      if (eventType == null) {
        if (isTerminalEvent(activeVisibleEvent)) {
          eventTypes.put(activeVisibleEvent, TERMINATION_EVENT);
        } else {
          eventTypes.put(activeVisibleEvent, SYSTEM_INTERACTION_EVENT);
        }
        addedEvents.add(activeVisibleEvent); // add new events
      }

      // additional information
      // add events info for a window
      HashSet<EventModel> eventsInWin = eventsInWindow.get(activeVisibleEvent.getWindowModel());
      if (eventsInWin == null) {
        eventsInWin = new HashSet<EventModel>();
        eventsInWindow.put(activeVisibleEvent.getWindowModel(), eventsInWin);
      }
      eventsInWin.add(activeVisibleEvent);
    }

    if (invokerEvent != null) {
      // change available events when first invoked
      HashSet<EventModel> availableEvents = availableEventsAfterInvoked.get(invokerEvent);

      if (availableEvents == null) {
        availableEvents = new HashSet<EventModel>();
        availableEventsAfterInvoked.put(invokerEvent, availableEvents);
      }
      availableEvents.addAll(activeVisibleEvents);
    }

    return addedEvents;
  }

  private void updateWorkListInfo(LinkedList<EventModel> eventWorkList, LinkedList<EventModel> newActiveVisibleEvents) {
    // need to update the information to retrieve the state information changed by event executions
    HashMap<EventModel, EventModel> refreshedModel = new HashMap<EventModel, EventModel>();
    for (EventModel newModel : newActiveVisibleEvents) {
      refreshedModel.put(newModel, newModel);
    }

    // replace existing event models to new models
    ListIterator<EventModel> iter = eventWorkList.listIterator();
    while (iter.hasNext()) {
      EventModel oldModel = iter.next();
      EventModel newModel = refreshedModel.get(oldModel);
      if (newModel != null) {
        iter.set(newModel);
      }

    }
  }

  private void addRippedWindow(WindowModel activeWindow) {
    rippedWindows.put(activeWindow, activeWindow);
  }

  private boolean isRippedWindow(WindowModel openedWindow) {
    return rippedWindows.containsKey(openedWindow);
  }

  private boolean isExpendable(EventModel event) {
    Component component = (Component) event.getComponentModel().getRef();
    if (component == null || !component.isEnabled())// || !component.isDisplayable() || !component.isVisible())
      return false;

    if (isTerminalEvent(event))
      return false;

    return isClickableEvent(event);

  }

  private boolean isClickableEvent(EventModel event) {
    return event.getEventTypeName().equals("action event") || event.getEventTypeName().equals("menu selection event")
        || event.getComponentModel().get("role").equals("page tab list");
  }

  private boolean isTerminalEvent(EventModel event) {
    return terminationEventFilter.shouldFilterComponent(event.getComponentModel());
  }

  private void printDebugInfo() {
    for (WindowModel w : eventsInWindow.keySet()) {

      TestLogger.info("Window: " + w.get("title"));
      HashSet<EventModel> events = eventsInWindow.get(w);
      for (EventModel event : events) {
        TestLogger.info("\t%s : %d", event.getEventName(), eventTypes.get(event));
        HashSet<EventModel> availableEvents = availableEventsWhenAvailable.get(event.getComponentModel());
        HashSet<WindowModel> invokedWindows = new HashSet<WindowModel>();
        if (availableEvents != null) {
          for (EventModel eventModel : availableEvents) {
            invokedWindows.add(eventModel.getWindowModel());
          }
        }
        TestLogger.info_("\t\twindows available after this event : ");
        for (WindowModel invokedW : invokedWindows) {
          TestLogger.info_("%s, ", invokedW.get("title"));
        }
        TestLogger.info("");

        HashSet<ComponentModel> invokingComps = invokingComponents.get(event.getWindowModel());
        if (invokingComps != null) {
          TestLogger.info_("\t\tinvoked by : ");
          for (ComponentModel compModel : invokingComps) {
            TestLogger.info_("%s, ", compModel.get("title"));
          }
          TestLogger.info("");
        }
      }

    }
  }

  private void createEFG() {
    TestLogger.info("Creating EFG...");

    TestLogger.info("Retrieving vertexes...");
    DirectedGraph<EventNode, DefaultEdge> g = new DefaultDirectedGraph<EventNode, DefaultEdge>(DefaultEdge.class);
    HashSet<EventNode> rootNodes = new HashSet<EventNode>();
    for (EventModel event : eventTypes.keySet()) {
      EventNode eventNode = new EventNode(event, eventTypes.get(event));
      boolean isRoot = false;
      if (rootSet.contains(event)) {
        rootNodes.add(eventNode);
        isRoot = true;
      }
      g.addVertex(eventNode);
      TestLogger.info("\t%s  isRoot:%s", eventNode, isRoot);
    }
    TestLogger.info("-----------------------------------", eventTypes.size());
    TestLogger.info("Number of nodes : %d", eventTypes.size());
    TestLogger.info("Number of initial nodes : %d", rootNodes.size());

    TestLogger.info("Retrievieing edges...");
    try {
      Set<EventNode> vSet = new HashSet<EventNode>(g.vertexSet());
      for (EventNode srcNode : vSet) {
        Set<EventModel> followsSet = getFollows(srcNode);
        TestLogger.info("\t#%s", srcNode);
        for (EventModel dstNode : followsSet) {
          g.addEdge(srcNode, new EventNode(dstNode, eventTypes.get(dstNode)));
          TestLogger.info("\t\t-->  %s", dstNode);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    TestLogger.info("----------------------------------");
    TestLogger.info("Number of edges : %d", g.edgeSet().size());
    VertexNameProvider<EventNode> vertexNameProvider = new VertexNameProvider<EventNode>() {
      @Override
      public String getVertexName(EventNode paramV) {
        return paramV.getName();
      }

    };

    // save graph
    IOUtil.saveObject(g, new File(TestProperty.getFolder("result"), "efg.ser").getAbsolutePath());
    IOUtil.saveObject(rootNodes, new File(TestProperty.getFolder("result"), "rootNodes.ser").getAbsolutePath());
    IOUtil.saveObject(eventsInWindow,
        new File(TestProperty.getFolder("result"), "eventsInWindow.ser").getAbsolutePath());
    IOUtil.saveObject(eventTypes, new File(TestProperty.getFolder("result"), "eventTypes.ser").getAbsolutePath());
    IOUtil.saveObject(accumulatedCoverage,
        new File(TestProperty.getFolder("result"), "EFGRipperCoverage.ser").getAbsolutePath());
    CoberturaController.reportCoberturaData(TestProperty.getFolder("source").getAbsolutePath(),
        new File(TestProperty.getFolder("result"), "InitialEFGRipperCoverage.ser").getAbsolutePath(), new File(
            TestProperty.getFolder("result"), "report_init").getAbsolutePath());
    CoberturaController.reportCoberturaData(TestProperty.getFolder("source").getAbsolutePath(),
        new File(TestProperty.getFolder("result"), "EFGRipperCoverage.ser").getAbsolutePath(),
        new File(TestProperty.getFolder("result"), "report_ripper").getAbsolutePath());

    // export graph to GraphML

    MyGraphMLExporter<EventNode, DefaultEdge> exporter = new MyGraphMLExporter<EventNode, DefaultEdge>(
        new IntegerNameProvider(), vertexNameProvider, new IntegerEdgeNameProvider(), null);

    try {
      Writer writer = new PrintWriter(new FileOutputStream(new File(TestProperty.getFolder("report"),
          "EFGModel.graphml")));
      // exporter.export(writer,g);
      exporter.export4(writer, g, eventsInWindow, eventTypes, eventNodeWindowInvokeMap, rootNodes);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private Set<EventModel> getFollows(EventNode eventNode) {
    EventModel eventModel = eventNode.getEventModel();
    HashSet<EventModel> availableEventsAfter;
    int eventType = eventNode.getEventType();

    switch (eventType) {
    case TERMINATION_EVENT:
      availableEventsAfter = new HashSet<EventModel>();
      HashSet<ComponentModel> invokingComps = invokingComponents.get(eventModel.getWindowModel());
      for (ComponentModel invokingComp : invokingComps) {
        availableEventsAfter.addAll(availableEventsWhenAvailable.get(invokingComp));
      }
      return availableEventsAfter;
    case SYSTEM_INTERACTION_EVENT:
    case RESTRICTED_FOCUS_EVENT:
    case UNRESTRICTED_FOCUS_EVENT:
      if (availableEventsAfterInvoked.containsKey(eventModel))
        availableEventsAfter = availableEventsAfterInvoked.get(eventModel);
      else
        availableEventsAfter = availableEventsWhenAvailable.get(eventModel.getComponentModel());
      return availableEventsAfter;
    }

    // error case
    return null;
  }

  @Override
  public void beforePerformEventUI(EventModel event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void afterPerformEventUI(EventModel event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void runCommand(String command) {
    // TODO Auto-generated method stub

  }

}