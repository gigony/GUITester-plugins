import guitesting.engine.strategy.AbstractStrategy;
import guitesting.engine.testcasegenerator.Trie;
import guitesting.model.graph.EventNode;
import guitesting.util.IOUtil;
import guitesting.util.TestLogger;
import guitesting.util.TestProperty;
import guitesting.util.TimeCounter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
/**
 * 
 * @author Gigon Bae
 *
 */
public class EFGBasedTestSuiteGenerationStrategy extends AbstractStrategy {
  public static final int SYSTEM_INTERACTION_EVENT = 1;
  public static final int TERMINATION_EVENT = 2;
  public static final int UNRESTRICTED_FOCUS_EVENT = 3;
  public static final int RESTRICTED_FOCUS_EVENT = 4;

  @Override
  public void run(String[] mainArgs) {
    int sequenceLength = Integer.parseInt(mainArgs[0]);

    TestLogger.info("Starting EFGBasedTestSuiteGeneration...");
    TestLogger.info("Sequence Length : " + sequenceLength);

    TimeCounter counter = new TimeCounter();
    counter.start();

    // get EFG model
    String efgPath = new File(TestProperty.getFolder("result"), "efg.ser").getAbsolutePath();
    TestLogger.info("Loading EFG from %s", efgPath);
    DirectedGraph<EventNode, DefaultEdge> graph = (DirectedGraph<EventNode, DefaultEdge>) IOUtil.loadObject(efgPath);

    TestLogger.info("Getting candidate events...");
    // Retrieve system interaction events
    Set<EventNode> candidateEventSet = new HashSet<EventNode>(graph.vertexSet());
    Iterator<EventNode> iter = candidateEventSet.iterator();
    while (iter.hasNext()) {
      EventNode eventNode = iter.next();
      TestLogger.info("\t%s", eventNode);
    }

    TestLogger.info("Total candidate events : %d", candidateEventSet.size());

    // get root nodes
    String rootNodeFile = new File(TestProperty.getFolder("result"), "rootNodes.ser").getAbsolutePath();
    TestLogger.info("Loading root nodes from %s", rootNodeFile);
    HashSet<EventNode> rootNodes = (HashSet<EventNode>) IOUtil.loadObject(rootNodeFile);
    for (EventNode rootNode : rootNodes) {
      TestLogger.info("\t%s", rootNode);
    }
    TestLogger.info("Total root events : %d", rootNodes.size());
    counter.stop();
    TestLogger.info("Elapsed time : %d ms", counter.getElapsedTime());

    TestLogger.info("Creating test cases...");
    // add null node and connect it into root nodes
    EventNode nullNode = EventNode.NullNode;
    graph.addVertex(nullNode);

    for (EventNode rootNode : rootNodes) {
      graph.addEdge(nullNode, rootNode);
    }

    // create candidate test cases
    ArrayList<ArrayList<EventNode>> testcaseCandidates = new ArrayList<ArrayList<EventNode>>();
    for (EventNode srcNode : candidateEventSet) {
      ArrayList<EventNode> testCaseCandidate = new ArrayList<EventNode>();

      List<DefaultEdge> pathCandidate = DijkstraShortestPath.findPathBetween(graph, nullNode, srcNode);
      if (pathCandidate == null)
        continue;

      for (DefaultEdge edge : pathCandidate) {
        testCaseCandidate.add(graph.getEdgeTarget(edge));
      }

      expandPath(sequenceLength - 1, graph, candidateEventSet, srcNode, testcaseCandidates, testCaseCandidate);

    }
    TestLogger.info("Original size : %d", testcaseCandidates.size());
    counter.stop();
    TestLogger.info("Elapsed time : %d ms", counter.getElapsedTime());

    // TestLogger.info("Appending termination events...");
    // int lastCount = 0;
    // for (ArrayList<EventNode> testCase : testcaseCandidates) {
    // lastCount++;
    //
    // TestLogger.debug_("%d :", lastCount);
    // for (EventNode event : testCase) {
    // TestLogger.debug_("--> %s", event);
    // }
    // EventNode lastNode = testCase.get(testCase.size() - 1);
    // ArrayList<EventNode> postFix = new ArrayList<EventNode>();
    // getTerminationEvents(graph, lastNode, postFix);
    // testCase.addAll(postFix);
    // TestLogger.debug("");
    // }

    TestLogger.info("Reducing test cases...");
    ArrayList<List<EventNode>> reducedTestCases = reduceTestCases(testcaseCandidates);

    TestLogger.info("Reduced Size : %d ", reducedTestCases.size());

    counter.stop();
    TestLogger.info("Elapsed time : %d ms", counter.getElapsedTime());

    saveTestSuite(reducedTestCases);

    counter.stop();
    TestLogger.info("Finished.");
    TestLogger.info("Elapsed time : %d ms", counter.getElapsedTime());

  }

  private ArrayList<List<EventNode>> reduceTestCases(ArrayList<ArrayList<EventNode>> testcaseCandidates) {
    ArrayList<List<EventNode>> reducedTestCases = new ArrayList<List<EventNode>>();
    Trie<EventNode> trie = new Trie<EventNode>();

    int listSize = testcaseCandidates.size();
    for (int i = 0; i < listSize; i++) {
      ArrayList<EventNode> item = testcaseCandidates.get(i);
      trie.add(item);
    }
    trie.collectLeafItems(reducedTestCases);
    // System.out.println("Total Item:"+trie.countAllItems());

    return reducedTestCases;
  }

  private void saveTestSuite(ArrayList<List<EventNode>> reducedTestCases) {
    File testSuiteFolder = new File(TestProperty.getFolder("result"), "testsuite");

    TestLogger.info("Deleting test suite folder (%s)", testSuiteFolder.getAbsolutePath());
    if (testSuiteFolder.exists())
      IOUtil.deleteFolder(testSuiteFolder);

    TestLogger.info("Creating test suite folder (%s)", testSuiteFolder.getAbsolutePath());
    testSuiteFolder.mkdirs();

    TestLogger.info("Saving test suite...");
    int testCaseIndex = 0;
    for (List<EventNode> testcase : reducedTestCases) {
      testCaseIndex++;
      String fileName = String.format("testcase_%08d.xml", testCaseIndex);
      File testCasePath = new File(testSuiteFolder, fileName);
      IOUtil.saveObjectToXML(testcase, testCasePath.getAbsolutePath());
    }
  }

  private void expandPath(int remained, DirectedGraph<EventNode, DefaultEdge> graph, Set<EventNode> candidateEventSet,
      EventNode srcNode, ArrayList<ArrayList<EventNode>> testcaseCandidates, ArrayList<EventNode> testCaseCandidate) {
    if (remained == 0) {
      testcaseCandidates.add(testCaseCandidate);
      // System.out.println(String.format("%d : to(%s)", testcaseCandidates.size(),srcNode));
      // System.out.print("\t");
      // for (EventNode event : testCaseCandidate) {
      // System.out.print("-->" + event);
      // }
      // System.out.println();
      return;
    }

    for (EventNode dstNode : candidateEventSet) {
      ArrayList<EventNode> newTestCaseCandidate = new ArrayList<EventNode>(testCaseCandidate);
      List<DefaultEdge> pathCandidate = DijkstraShortestPath.findPathBetween(graph, srcNode, dstNode);
      if (pathCandidate != null) {
        if (pathCandidate.isEmpty()) {
          if (graph.containsEdge(srcNode, dstNode)) {
            newTestCaseCandidate.add(dstNode);
          }
        } else {
          for (DefaultEdge edge : pathCandidate) {
            newTestCaseCandidate.add(graph.getEdgeTarget(edge));
          }
        }
        expandPath(remained - 1, graph, candidateEventSet, dstNode, testcaseCandidates, newTestCaseCandidate);
      } else {
        TestLogger.warn("Disconnected! %s --> %s", srcNode, dstNode);
        expandPath(0, graph, candidateEventSet, dstNode, testcaseCandidates, newTestCaseCandidate);
      }

    }
  }

  private void getTerminationEvents(DirectedGraph<EventNode, DefaultEdge> graph, EventNode lastNode,
      ArrayList<EventNode> postFix) {

    for (DefaultEdge edge : graph.outgoingEdgesOf(lastNode)) {
      EventNode nextNode = graph.getEdgeTarget(edge);

      if (nextNode.getEventType() == TERMINATION_EVENT) {
        postFix.add(nextNode);
        TestLogger.debug_("--> ## %s ##", nextNode);
        getTerminationEvents(graph, nextNode, postFix);
        return;
      }
    }
  }
}