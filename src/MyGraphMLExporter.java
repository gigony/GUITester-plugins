

import guitesting.model.WindowModel;
import guitesting.model.event.EventModel;
import guitesting.model.graph.EventNode;
import guitesting.util.Pair;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.IntegerEdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.VertexNameProvider;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.x5.template.Chunk;
import com.x5.template.Theme;
/**
 * 
 * @author Gigon Bae
 *
 */
public class MyGraphMLExporter<V, E> {

  final int SYSTEM_INTERACTION_EVENT = 1;
  final int TERMINATION_EVENT = 2;
  final int UNRESTRICTED_FOCUS_EVENT = 3;
  final int RESTRICTED_FOCUS_EVENT = 4;
  final int INITIAL_EVENT = 5;

  private VertexNameProvider<V> vertexIDProvider;
  private VertexNameProvider<V> vertexLabelProvider;
  private EdgeNameProvider<E> edgeIDProvider;
  private EdgeNameProvider<E> edgeLabelProvider;

  public MyGraphMLExporter() {
    this(new IntegerNameProvider(), null, new IntegerEdgeNameProvider(), null);
  }

  public MyGraphMLExporter(VertexNameProvider<V> paramVertexNameProvider1,
      VertexNameProvider<V> paramVertexNameProvider2, EdgeNameProvider<E> paramEdgeNameProvider1,
      EdgeNameProvider<E> paramEdgeNameProvider2) {
    this.vertexIDProvider = paramVertexNameProvider1;
    this.vertexLabelProvider = paramVertexNameProvider2;
    this.edgeIDProvider = paramEdgeNameProvider1;
    this.edgeLabelProvider = paramEdgeNameProvider2;
  }

  public void export(Writer paramWriter, Graph<V, E> paramGraph) throws SAXException, TransformerConfigurationException {
    PrintWriter localPrintWriter = new PrintWriter(paramWriter);
    StreamResult localStreamResult = new StreamResult(localPrintWriter);
    SAXTransformerFactory localSAXTransformerFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
    TransformerHandler localTransformerHandler = localSAXTransformerFactory.newTransformerHandler();
    Transformer localTransformer = localTransformerHandler.getTransformer();
    localTransformer.setOutputProperty("encoding", "UTF-8");
    localTransformer.setOutputProperty("indent", "yes");
    localTransformerHandler.setResult(localStreamResult);
    localTransformerHandler.startDocument();
    AttributesImpl localAttributesImpl = new AttributesImpl();
    AttributesImpl localAttributesImpl2 = new AttributesImpl();
    localTransformerHandler.startPrefixMapping("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    localAttributesImpl.addAttribute("", "", "xsi:schemaLocation", "CDATA",
        "http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd");
    localAttributesImpl.addAttribute("", "", "xmlns:yed", "CDATA", "http://www.yworks.com/xml/yed/3");
    localAttributesImpl.addAttribute("", "", "xmlns:y", "CDATA", "http://www.yworks.com/xml/graphml");
    localTransformerHandler.startElement("http://graphml.graphdrawing.org/xmlns", "", "graphml", localAttributesImpl);
    localTransformerHandler.endPrefixMapping("xsi");
    if (this.vertexLabelProvider != null) {
      localAttributesImpl.clear();
      localAttributesImpl.addAttribute("", "", "id", "CDATA", "vertex_label");
      localAttributesImpl.addAttribute("", "", "for", "CDATA", "node");
      localAttributesImpl.addAttribute("", "", "attr.name", "CDATA", "Vertex Label");
      localAttributesImpl.addAttribute("", "", "attr.type", "CDATA", "string");
      localTransformerHandler.startElement("", "", "key", localAttributesImpl);
      localTransformerHandler.endElement("", "", "key");

      localAttributesImpl.clear();
      localAttributesImpl.addAttribute("", "", "id", "CDATA", "d7");
      localAttributesImpl.addAttribute("", "", "for", "CDATA", "node");
      localAttributesImpl.addAttribute("", "", "yfiles.type", "CDATA", "nodegraphics");
      localTransformerHandler.startElement("", "", "key", localAttributesImpl);
      localTransformerHandler.endElement("", "", "key");
    }
    if (this.edgeLabelProvider != null) {
      localAttributesImpl.clear();
      localAttributesImpl.addAttribute("", "", "id", "CDATA", "edge_label");
      localAttributesImpl.addAttribute("", "", "for", "CDATA", "edge");
      localAttributesImpl.addAttribute("", "", "attr.name", "CDATA", "Edge Label");
      localAttributesImpl.addAttribute("", "", "attr.type", "CDATA", "string");
      localTransformerHandler.startElement("", "", "key", localAttributesImpl);
      localTransformerHandler.endElement("", "", "key");
    }
    localAttributesImpl.clear();
    localAttributesImpl.addAttribute("", "", "edgedefault", "CDATA", (paramGraph instanceof DirectedGraph) ? "directed"
        : "undirected");
    localTransformerHandler.startElement("", "", "graph", localAttributesImpl);
    Iterator localIterator = paramGraph.vertexSet().iterator();
    Object localObject;
    String str;
    while (localIterator.hasNext()) {
      localObject = localIterator.next();
      localAttributesImpl.clear();
      localAttributesImpl.addAttribute("", "", "id", "CDATA", this.vertexIDProvider.getVertexName((V) localObject));
      localTransformerHandler.startElement("", "", "node", localAttributesImpl);
      if (this.vertexLabelProvider != null) {
        localAttributesImpl.clear();
        localAttributesImpl.addAttribute("", "", "key", "CDATA", "vertex_label");
        localTransformerHandler.startElement("", "", "data", localAttributesImpl);
        str = this.vertexLabelProvider.getVertexName((V) localObject);
        localTransformerHandler.characters(str.toCharArray(), 0, str.length());
        localTransformerHandler.endElement("", "", "data");

        localAttributesImpl.clear();
        localAttributesImpl.addAttribute("", "", "key", "CDATA", "d7");
        localTransformerHandler.startElement("", "", "data", localAttributesImpl);

        localAttributesImpl2.clear();
        localTransformerHandler.startElement("", "", "y:ShapeNode", localAttributesImpl2);

        localAttributesImpl2.clear();
        localAttributesImpl2.addAttribute("", "", "height", "CDATA", "30");
        localAttributesImpl2.addAttribute("", "", "width", "CDATA", "300");
        localTransformerHandler.startElement("", "", "y:Geometry", localAttributesImpl2);
        localTransformerHandler.endElement("", "", "y:Geometry");

        localAttributesImpl2.clear();
        localTransformerHandler.startElement("", "", "y:NodeLabel", localAttributesImpl2);
        localTransformerHandler.characters(str.toCharArray(), 0, str.length());

        localAttributesImpl2.clear();
        localTransformerHandler.startElement("", "", "y:LabelModel", localAttributesImpl2);
        localTransformerHandler.endElement("", "", "y:LabelModel");

        localTransformerHandler.endElement("", "", "y:NodeLabel");

        localAttributesImpl.clear();
        localAttributesImpl.addAttribute("", "", "type", "CDATA", "rectangle");
        localTransformerHandler.startElement("", "", "y:Shape", localAttributesImpl);
        localTransformerHandler.endElement("", "", "y:Shape");

        localTransformerHandler.endElement("", "", "y:ShapeNode");

        localTransformerHandler.endElement("", "", "data");

      }
      localTransformerHandler.endElement("", "", "node");

    }
    localIterator = paramGraph.edgeSet().iterator();
    while (localIterator.hasNext()) {
      localObject = localIterator.next();
      localAttributesImpl.clear();
      localAttributesImpl.addAttribute("", "", "id", "CDATA", this.edgeIDProvider.getEdgeName((E) localObject));
      localAttributesImpl.addAttribute("", "", "source", "CDATA",
          this.vertexIDProvider.getVertexName(paramGraph.getEdgeSource((E) localObject)));
      localAttributesImpl.addAttribute("", "", "target", "CDATA",
          this.vertexIDProvider.getVertexName(paramGraph.getEdgeTarget((E) localObject)));
      localTransformerHandler.startElement("", "", "edge", localAttributesImpl);
      if (this.edgeLabelProvider != null) {
        localAttributesImpl.clear();
        localAttributesImpl.addAttribute("", "", "key", "CDATA", "edge_label");
        localTransformerHandler.startElement("", "", "data", localAttributesImpl);
        str = this.edgeLabelProvider.getEdgeName((E) localObject);
        localTransformerHandler.characters(str.toCharArray(), 0, str.length());
        localTransformerHandler.endElement("", "", "data");
      }
      localTransformerHandler.endElement("", "", "edge");
    }
    localTransformerHandler.endElement("", "", "graph");
    localTransformerHandler.endElement("", "", "graphml");
    localTransformerHandler.endDocument();
    localPrintWriter.flush();
  }

  public void export(Writer paramWriter, Graph<V, E> paramGraph,
      HashMap<WindowModel, HashSet<EventModel>> eventsInWindow, HashMap<EventModel, Integer> eventTypes)
      throws SAXException, TransformerConfigurationException {
    PrintWriter localPrintWriter = new PrintWriter(paramWriter);
    StreamResult localStreamResult = new StreamResult(localPrintWriter);
    SAXTransformerFactory localSAXTransformerFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
    TransformerHandler localTransformerHandler = localSAXTransformerFactory.newTransformerHandler();
    Transformer localTransformer = localTransformerHandler.getTransformer();
    localTransformer.setOutputProperty("encoding", "UTF-8");
    localTransformer.setOutputProperty("indent", "yes");
    localTransformerHandler.setResult(localStreamResult);
    localTransformerHandler.startDocument();
    AttributesImpl localAttributesImpl = new AttributesImpl();
    AttributesImpl localAttributesImpl2 = new AttributesImpl();
    localTransformerHandler.startPrefixMapping("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    localAttributesImpl.addAttribute("", "", "xsi:schemaLocation", "CDATA",
        "http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd");
    localAttributesImpl.addAttribute("", "", "xmlns:yed", "CDATA", "http://www.yworks.com/xml/yed/3");
    localAttributesImpl.addAttribute("", "", "xmlns:y", "CDATA", "http://www.yworks.com/xml/graphml");
    localTransformerHandler.startElement("http://graphml.graphdrawing.org/xmlns", "", "graphml", localAttributesImpl);
    localTransformerHandler.endPrefixMapping("xsi");
    if (this.vertexLabelProvider != null) {
      localAttributesImpl.clear();
      localAttributesImpl.addAttribute("", "", "id", "CDATA", "vertex_label");
      localAttributesImpl.addAttribute("", "", "for", "CDATA", "node");
      localAttributesImpl.addAttribute("", "", "attr.name", "CDATA", "Vertex Label");
      localAttributesImpl.addAttribute("", "", "attr.type", "CDATA", "string");
      localTransformerHandler.startElement("", "", "key", localAttributesImpl);
      localTransformerHandler.endElement("", "", "key");

      localAttributesImpl.clear();
      localAttributesImpl.addAttribute("", "", "id", "CDATA", "d7");
      localAttributesImpl.addAttribute("", "", "for", "CDATA", "node");
      localAttributesImpl.addAttribute("", "", "yfiles.type", "CDATA", "nodegraphics");
      localTransformerHandler.startElement("", "", "key", localAttributesImpl);
      localTransformerHandler.endElement("", "", "key");
    }
    if (this.edgeLabelProvider != null) {
      localAttributesImpl.clear();
      localAttributesImpl.addAttribute("", "", "id", "CDATA", "edge_label");
      localAttributesImpl.addAttribute("", "", "for", "CDATA", "edge");
      localAttributesImpl.addAttribute("", "", "attr.name", "CDATA", "Edge Label");
      localAttributesImpl.addAttribute("", "", "attr.type", "CDATA", "string");
      localTransformerHandler.startElement("", "", "key", localAttributesImpl);
      localTransformerHandler.endElement("", "", "key");
    }
    localAttributesImpl.clear();
    localAttributesImpl.addAttribute("", "", "edgedefault", "CDATA", (paramGraph instanceof DirectedGraph) ? "directed"
        : "undirected");
    localAttributesImpl.addAttribute("", "", "id", "CDATA", "G");
    localTransformerHandler.startElement("", "", "graph", localAttributesImpl);
    Iterator localIterator = paramGraph.vertexSet().iterator();
    Object localObject;
    String str;

    int groupID = 0;
    for (WindowModel w : eventsInWindow.keySet()) {

      localAttributesImpl.clear();
      localAttributesImpl.addAttribute("", "", "id", "CDATA", "g" + (groupID++));
      localAttributesImpl.addAttribute("", "", "yfiles.foldertype", "CDATA", "group");
      localTransformerHandler.startElement("", "", "node", localAttributesImpl);

      localAttributesImpl.clear();
      localAttributesImpl.addAttribute("", "", "key", "CDATA", "d7");
      localTransformerHandler.startElement("", "", "data", localAttributesImpl);

      localAttributesImpl.clear();
      localTransformerHandler.startElement("", "", "y:ProxyAutoBoundsNode", localAttributesImpl);

      localAttributesImpl.clear();
      localAttributesImpl.addAttribute("", "", "active", "CDATA", "0");
      localTransformerHandler.startElement("", "", "y:Realizers", localAttributesImpl);

      localAttributesImpl.clear();
      localTransformerHandler.startElement("", "", "y:GroupNode", localAttributesImpl);
      localAttributesImpl.clear();
      localTransformerHandler.startElement("", "", "y:NodeLabel", localAttributesImpl);

      str = w.get("title");
      localTransformerHandler.characters(str.toCharArray(), 0, str.length());

      localTransformerHandler.endElement("", "", "y:NodeLabel");

      localAttributesImpl.clear();
      localAttributesImpl.addAttribute("", "", "closed", "CDATA", "false");
      localTransformerHandler.startElement("", "", "y:State", localAttributesImpl);
      localTransformerHandler.endElement("", "", "y:State");

      localTransformerHandler.endElement("", "", "y:GroupNode");

      localAttributesImpl.clear();
      localTransformerHandler.startElement("", "", "y:GroupNode", localAttributesImpl);
      localAttributesImpl.clear();
      localTransformerHandler.startElement("", "", "y:NodeLabel", localAttributesImpl);

      str = w.get("title");
      localTransformerHandler.characters(str.toCharArray(), 0, str.length());

      localTransformerHandler.endElement("", "", "y:NodeLabel");

      localAttributesImpl.clear();
      localAttributesImpl.addAttribute("", "", "closed", "CDATA", "true");
      localTransformerHandler.startElement("", "", "y:State", localAttributesImpl);
      localTransformerHandler.endElement("", "", "y:State");

      localTransformerHandler.endElement("", "", "y:GroupNode");

      localTransformerHandler.endElement("", "", "y:Realizers");

      localTransformerHandler.endElement("", "", "y:ProxyAutoBoundsNode");

      localTransformerHandler.endElement("", "", "data");

      //
      //
      //
      //
      //
      // str=String.format("<data key=\"d7\"> <y:ProxyAutoBoundsNode> <y:Realizers active=\"0\"> <y:GroupNode> <y:NodeLabel >%s</y:NodeLabel></y:GroupNode><y:GroupNode><y:NodeLabel>%s</y:NodeLabel></y:GroupNode></y:Realizers></y:ProxyAutoBoundsNode></data>",
      // w.get("title"),w.get("title"));
      // localTransformerHandler.characters(str.toCharArray(), 0, str.length());
      //
      //

      localAttributesImpl.clear();
      localAttributesImpl.addAttribute("", "", "edgedefault", "CDATA",
          (paramGraph instanceof DirectedGraph) ? "directed" : "undirected");
      localAttributesImpl.addAttribute("", "", "id", "CDATA", "n" + groupID + ":");
      localTransformerHandler.startElement("", "", "graph", localAttributesImpl);

      HashSet<EventModel> events = eventsInWindow.get(w);
      for (EventModel event : events) {

        localObject = new EventNode(event, eventTypes.get(event));
        localAttributesImpl.clear();
        localAttributesImpl.addAttribute("", "", "id", "CDATA", this.vertexIDProvider.getVertexName((V) localObject));
        localTransformerHandler.startElement("", "", "node", localAttributesImpl);
        if (this.vertexLabelProvider != null) {
          localAttributesImpl.clear();
          localAttributesImpl.addAttribute("", "", "key", "CDATA", "vertex_label");
          localTransformerHandler.startElement("", "", "data", localAttributesImpl);
          str = this.vertexLabelProvider.getVertexName((V) localObject);
          localTransformerHandler.characters(str.toCharArray(), 0, str.length());
          localTransformerHandler.endElement("", "", "data");

          localAttributesImpl.clear();
          localAttributesImpl.addAttribute("", "", "key", "CDATA", "d7");
          localTransformerHandler.startElement("", "", "data", localAttributesImpl);

          localAttributesImpl2.clear();
          localTransformerHandler.startElement("", "", "y:ShapeNode", localAttributesImpl2);

          localAttributesImpl2.clear();
          localAttributesImpl2.addAttribute("", "", "height", "CDATA", "30");
          localAttributesImpl2.addAttribute("", "", "width", "CDATA", "300");
          localTransformerHandler.startElement("", "", "y:Geometry", localAttributesImpl2);
          localTransformerHandler.endElement("", "", "y:Geometry");

          localAttributesImpl2.clear();
          localAttributesImpl2.addAttribute("", "", "color", "CDATA", getFillColor(eventTypes.get(event)));
          localAttributesImpl2.addAttribute("", "", "transparent", "CDATA", "false");
          localTransformerHandler.startElement("", "", "y:Fill", localAttributesImpl2);
          localTransformerHandler.endElement("", "", "y:Fill");

          localAttributesImpl2.clear();
          localTransformerHandler.startElement("", "", "y:NodeLabel", localAttributesImpl2);
          localTransformerHandler.characters(str.toCharArray(), 0, str.length());

          localAttributesImpl2.clear();
          localTransformerHandler.startElement("", "", "y:LabelModel", localAttributesImpl2);
          localTransformerHandler.endElement("", "", "y:LabelModel");

          localTransformerHandler.endElement("", "", "y:NodeLabel");

          localAttributesImpl.clear();
          localAttributesImpl.addAttribute("", "", "type", "CDATA", "rectangle");
          localTransformerHandler.startElement("", "", "y:Shape", localAttributesImpl);
          localTransformerHandler.endElement("", "", "y:Shape");

          localTransformerHandler.endElement("", "", "y:ShapeNode");

          localTransformerHandler.endElement("", "", "data");

        }
        localTransformerHandler.endElement("", "", "node");
      }

      localTransformerHandler.endElement("", "", "graph");

      localTransformerHandler.endElement("", "", "node");

    }

    localIterator = paramGraph.edgeSet().iterator();
    while (localIterator.hasNext()) {
      localObject = localIterator.next();
      localAttributesImpl.clear();
      localAttributesImpl.addAttribute("", "", "id", "CDATA", this.edgeIDProvider.getEdgeName((E) localObject));
      localAttributesImpl.addAttribute("", "", "source", "CDATA",
          this.vertexIDProvider.getVertexName(paramGraph.getEdgeSource((E) localObject)));
      localAttributesImpl.addAttribute("", "", "target", "CDATA",
          this.vertexIDProvider.getVertexName(paramGraph.getEdgeTarget((E) localObject)));
      localTransformerHandler.startElement("", "", "edge", localAttributesImpl);
      if (this.edgeLabelProvider != null) {
        localAttributesImpl.clear();
        localAttributesImpl.addAttribute("", "", "key", "CDATA", "edge_label");
        localTransformerHandler.startElement("", "", "data", localAttributesImpl);
        str = this.edgeLabelProvider.getEdgeName((E) localObject);
        localTransformerHandler.characters(str.toCharArray(), 0, str.length());
        localTransformerHandler.endElement("", "", "data");
      }
      localTransformerHandler.endElement("", "", "edge");
    }
    localTransformerHandler.endElement("", "", "graph");
    localTransformerHandler.endElement("", "", "graphml");
    localTransformerHandler.endDocument();
    localPrintWriter.flush();
  }

  public void export2(Writer writer, Graph<V, E> g, HashMap<WindowModel, HashSet<EventModel>> eventsInWindow,
      HashMap<EventModel, Integer> eventTypes, HashSet<EventNode> rootNodes) {

    Theme theme = new Theme("themes", null, "cxml"); // a standard theme with no layers.

    // theme.setDefaultFileExtension("cxml");

    theme.setJarContext(this);

    Chunk graph = theme.makeChunk("EFGModelVisualization#graph");

    StringBuilder groupnode_info = new StringBuilder();

    int groupID = 0;
    for (WindowModel w : eventsInWindow.keySet()) {

      groupID++;
      String windowTitle = w.get("title");

      Chunk groupnode = theme.makeChunk("EFGModelVisualization#groupnode");
      groupnode.set("groupnode_id", groupID);
      groupnode.set("groupnode_name", windowTitle);

      StringBuilder subnode_info = new StringBuilder();

      HashSet<EventModel> events = eventsInWindow.get(w);
      EventNode localObject;
      for (EventModel event : events) {
        localObject = new EventNode(event, eventTypes.get(event));

        Chunk subnode = theme.makeChunk("EFGModelVisualization#subnode");
        subnode.set("subnode_id", this.vertexIDProvider.getVertexName((V) localObject));
        subnode.set("subnode_name",
            StringEscapeUtils.escapeHtml3(localObject.getName()).replace("@", "\n@").replace("[", "\n["));
        subnode.set("subnode_color", getFillColor(eventTypes.get(event)));
        if (rootNodes.contains(localObject))
          subnode.set("is_root", "true");
        else
          subnode.set("is_root", "false");

        subnode_info.append(subnode.toString());
      }

      groupnode.set("subnode_info", subnode_info);

      groupnode_info.append(groupnode.toString());
    }

    StringBuilder edge_info = new StringBuilder();

    Iterator<E> localIterator = g.edgeSet().iterator();
    while (localIterator.hasNext()) {
      E localObject = localIterator.next();
      Chunk edge = theme.makeChunk("EFGModelVisualization#edge");
      edge.set("edge_id", this.edgeIDProvider.getEdgeName((E) localObject));
      edge.set("edge_src", this.vertexIDProvider.getVertexName(g.getEdgeSource((E) localObject)));
      edge.set("edge_dst", this.vertexIDProvider.getVertexName(g.getEdgeTarget((E) localObject)));

      edge_info.append(edge.toString());
    }

    graph.set("groupnode_info", groupnode_info);
    graph.set("edge_info", edge_info);

    try {
      graph.render(writer);
      writer.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public void export3(Writer writer, Graph<V, E> g, HashMap<WindowModel, HashSet<EventModel>> eventsInWindow,
      HashMap<EventModel, Integer> eventTypes, HashSet<EventNode> rootNodes) {

    Theme theme = new Theme("themes", null, "cxml"); // a standard theme with no layers.

    // theme.setDefaultFileExtension("cxml");

    theme.setJarContext(this);

    Chunk graph = theme.makeChunk("EFGModelVisualization#graph");

    // ArrayList<EventNode> nonSystemInteractionEvents = new ArrayList<EventNode>();

    StringBuilder groupnode_info = new StringBuilder();

    HashMap<EventNode, Integer> eventGroupID = new HashMap<EventNode, Integer>();

    int groupID = 0;
    for (WindowModel w : eventsInWindow.keySet()) {

      groupID++;
      String windowTitle = w.get("title");

      Chunk groupnode = theme.makeChunk("EFGModelVisualization#groupnode");
      groupnode.set("groupnode_id", groupID);
      groupnode.set("groupnode_name", StringEscapeUtils.escapeHtml3(windowTitle));

      StringBuilder subnode_info = new StringBuilder();

      HashSet<EventModel> events = eventsInWindow.get(w);

      for (EventModel event : events) {
        EventNode localObject = new EventNode(event, eventTypes.get(event));

        eventGroupID.put(localObject, groupID);

        Chunk subnode = theme.makeChunk("EFGModelVisualization#subnode");
        subnode.set("subnode_id", this.vertexIDProvider.getVertexName((V) localObject));
        subnode.set("subnode_name",
            StringEscapeUtils.escapeHtml3(localObject.getName()).replace("@", "\n@").replace("[", "\n["));
        subnode.set("subnode_color", getFillColor(eventTypes.get(event)));
        if (rootNodes.contains(localObject))
          subnode.set("is_root", "true");
        else
          subnode.set("is_root", "false");

        // if (eventTypes.get(event) != SYSTEM_INTERACTION_EVENT) {
        // nonSystemInteractionEvents.add(localObject);
        // }

        subnode_info.append(subnode.toString());
      }

      groupnode.set("subnode_info", subnode_info);

      groupnode_info.append(groupnode.toString());
    }

    StringBuilder edge_info = new StringBuilder();

    // for (Object srcV : nonSystemInteractionEvents) {
    // Set<E> edges = g.edgesOf((V) srcV);
    // for (E e : edges) {
    // Chunk edge = theme.makeChunk("EFGModelVisualization#edge");
    // edge.set("edge_id", this.edgeIDProvider.getEdgeName(e));
    // edge.set("edge_src", this.vertexIDProvider.getVertexName(g.getEdgeSource(e)));
    // edge.set("edge_dst", this.vertexIDProvider.getVertexName(g.getEdgeTarget(e)));
    //
    // edge_info.append(edge.toString());
    // }
    //
    // }

    HashSet<Pair<Integer, Integer>> windowPair = new HashSet<Pair<Integer, Integer>>();
    Iterator<E> localIterator = g.edgeSet().iterator();
    while (localIterator.hasNext()) {
      E localObject = localIterator.next();

      int srcGroup = eventGroupID.get((EventNode) g.getEdgeSource((E) localObject));
      int dstGroup = eventGroupID.get((EventNode) g.getEdgeTarget((E) localObject));

      if (!windowPair.contains(new Pair(srcGroup, dstGroup))) {
        windowPair.add(new Pair(srcGroup, dstGroup));

        Chunk edge = theme.makeChunk("EFGModelVisualization#edge");
        edge.set("edge_id", this.edgeIDProvider.getEdgeName((E) localObject));
        edge.set("edge_src", srcGroup + ":");
        edge.set("edge_dst", dstGroup + ":");

        edge_info.append(edge.toString());

      }
    }

    graph.set("groupnode_info", groupnode_info);
    graph.set("edge_info", edge_info);

    try {
      graph.render(writer);
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public void export4(Writer writer, Graph<V, E> g, HashMap<WindowModel, HashSet<EventModel>> eventsInWindow,
      HashMap<EventModel, Integer> eventTypes, HashMap<EventModel, HashSet<WindowModel>> eventNodeWindowInvokeMap,
      HashSet<EventNode> rootNodes) {

    Theme theme = new Theme("themes", null, "cxml"); // a standard theme with no layers.
    theme.setJarContext(getClass());

    Chunk graph = theme.makeChunk("EFGModelVisualization#graph");

    // ArrayList<EventNode> nonSystemInteractionEvents = new ArrayList<EventNode>();

    StringBuilder groupnode_info = new StringBuilder();

    HashMap<EventNode, Integer> eventGroupID = new HashMap<EventNode, Integer>();

    HashMap<EventNode, HashSet<String>> eventWindowPair = new HashMap<EventNode, HashSet<String>>();
    HashMap<String, Integer> windowWindowIDMap = new HashMap<String, Integer>();

    int groupID = 0;
    for (WindowModel w : eventsInWindow.keySet()) {

      groupID++;
      String windowTitle = w.get("title");
      windowWindowIDMap.put(w.get("id"), groupID);

      Chunk groupnode = theme.makeChunk("EFGModelVisualization#groupnode");
      groupnode.set("groupnode_id", groupID);
      groupnode.set("groupnode_name", StringEscapeUtils.escapeHtml3(windowTitle));

      StringBuilder subnode_info = new StringBuilder();

      HashSet<EventModel> events = eventsInWindow.get(w);

      for (EventModel event : events) {
        EventNode localObject = new EventNode(event, eventTypes.get(event));
        HashSet<WindowModel> invokedWindows = eventNodeWindowInvokeMap.get(event);
        if (invokedWindows != null) {
          for (WindowModel invokedWin : invokedWindows) {
            if (!invokedWin.equals(w)) {
              HashSet<String> windowSet = eventWindowPair.get(localObject);
              if (windowSet == null) {
                windowSet = new HashSet<String>();
                eventWindowPair.put(localObject, windowSet);
              }
              windowSet.add(invokedWin.get("id"));
            }
          }
        }

        eventGroupID.put(localObject, groupID);

        Chunk subnode = theme.makeChunk("EFGModelVisualization#subnode");
        subnode.set("subnode_id", this.vertexIDProvider.getVertexName((V) localObject));
        subnode.set("subnode_name",
            StringEscapeUtils.escapeHtml3(localObject.getName()).replace("@", "\n@").replace("[", "\n["));
        subnode.set("subnode_color", getFillColor(eventTypes.get(event)));
        if (rootNodes.contains(localObject))
          subnode.set("is_root", "true");
        else
          subnode.set("is_root", "false");

        // if (eventTypes.get(event) != SYSTEM_INTERACTION_EVENT) {
        // nonSystemInteractionEvents.add(localObject);
        // }

        subnode_info.append(subnode.toString());
      }

      groupnode.set("subnode_info", subnode_info);

      groupnode_info.append(groupnode.toString());
    }

    StringBuilder edge_info = new StringBuilder();

    // for (Object srcV : nonSystemInteractionEvents) {
    // Set<E> edges = g.edgesOf((V) srcV);
    // for (E e : edges) {
    // Chunk edge = theme.makeChunk("EFGModelVisualization#edge");
    // edge.set("edge_id", this.edgeIDProvider.getEdgeName(e));
    // edge.set("edge_src", this.vertexIDProvider.getVertexName(g.getEdgeSource(e)));
    // edge.set("edge_dst", this.vertexIDProvider.getVertexName(g.getEdgeTarget(e)));
    //
    // edge_info.append(edge.toString());
    // }
    //
    // }

    Set<V> vertexSet = g.vertexSet();
    int edgeID = 0;
    for (V v : vertexSet) {
      EventNode srcEvent = (EventNode) v;

      HashSet<String> invokedWindows = eventWindowPair.get(srcEvent);
      if (invokedWindows != null
          && (srcEvent.getEventType() == RESTRICTED_FOCUS_EVENT || srcEvent.getEventType() == UNRESTRICTED_FOCUS_EVENT)) {
        for (String winID : invokedWindows) {
          if (winID == null || windowWindowIDMap.get(winID) == null) {
            System.out.println("@@@" + srcEvent + " " + winID);
          }
          int grpID = windowWindowIDMap.get(winID);

          edgeID++;
          Chunk edge = theme.makeChunk("EFGModelVisualization#edge");
          edge.set("edge_id", "e" + edgeID);
          edge.set("edge_src", this.vertexIDProvider.getVertexName((V) srcEvent));
          edge.set("edge_dst", grpID + ":");

          edge_info.append(edge.toString());

        }
      }
    }

    // HashSet<Pair<Integer, Integer>> windowPair = new HashSet<Pair<Integer, Integer>>();
    // Iterator<E> localIterator = g.edgeSet().iterator();
    // while (localIterator.hasNext()) {
    // E localObject = localIterator.next();
    //
    // int srcGroup = eventGroupID.get((EventNode) g.getEdgeSource((E) localObject));
    // int dstGroup = eventGroupID.get((EventNode) g.getEdgeTarget((E) localObject));
    //
    // if(((EventNode) g.getEdgeSource((E) localObject)).getEventType()==RESTRICTED_FOCUS_EVENT || ((EventNode)
    // g.getEdgeSource((E)
    // localObject)).getEventType()==UNRESTRICTED_FOCUS_EVENT)
    // {
    //
    //
    // if (!windowPair.contains(new Pair(srcGroup, dstGroup))) {
    // windowPair.add(new Pair(srcGroup, dstGroup));
    //
    // Chunk edge = theme.makeChunk("EFGModelVisualization#edge");
    // edge.set("edge_id", this.edgeIDProvider.getEdgeName((E) localObject));
    // edge.set("edge_src", srcGroup + ":");
    // edge.set("edge_dst", dstGroup + ":");
    //
    // edge_info.append(edge.toString());
    //
    // }
    // }
    // }

    graph.set("groupnode_info", groupnode_info);
    graph.set("edge_info", edge_info);

    try {
      graph.render(writer);
      writer.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  private String getFillColor(Integer colType) {

    int type = colType;

    switch (type) {
    case SYSTEM_INTERACTION_EVENT:
      return "#ffff00";

    case TERMINATION_EVENT:
      return "#ff0000";
    case UNRESTRICTED_FOCUS_EVENT:
      return "#99cc00";
    case RESTRICTED_FOCUS_EVENT:
      return "#00ccff";
    case INITIAL_EVENT:
      return "#ff00ff";
    }
    return "#ffffff";
  }

}
