package model.logic;

import model.data_structures.*;

/**
 * Analyzer class for network expansion analysis
 * Handles minimum spanning tree calculations and network connectivity analysis
 */
public class NetworkExpansionAnalyzer {
  private final ModelData modelData;
  private static final int MAX_BRANCH_DISPLAY = 10;

  public NetworkExpansionAnalyzer(ModelData modelData) {
    this.modelData = modelData;
  }

  /**
   * Analyze network expansion using minimum spanning tree
   * 
   * @return Analysis result as formatted string
   */
  public String analyzeNetworkExpansion() {
    try {
      // Find the landing point with maximum connections
      String startPoint = findMaxConnectionLandingPoint();
      if (startPoint == null) {
        return "No valid starting point found for network analysis";
      }

      // Get minimum spanning tree
      ILista mstEdges = modelData.getGrafo().mstPrimLazy(startPoint);
      if (mstEdges == null || mstEdges.isEmpty()) {
        return "No minimum spanning tree found";
      }

      return formatAnalysisResults(mstEdges);

    } catch (Exception e) {
      return "Error in network expansion analysis: " + e.getMessage();
    }
  }

  /**
   * Find landing point with maximum connections
   * 
   * @return ID of landing point with most connections
   */
  private String findMaxConnectionLandingPoint() throws PosException, VacioException {
    ILista landingPoints = modelData.getLandingIdTabla().valueSet();
    int maxConnections = 0;
    String maxConnectionPoint = null;

    for (int i = 1; i <= landingPoints.size(); i++) {
      ILista currentList = (ILista) landingPoints.getElement(i);
      if (currentList.size() > maxConnections) {
        maxConnections = currentList.size();
        maxConnectionPoint = (String) ((Vertex) currentList.getElement(1)).getId();
      }
    }

    return maxConnectionPoint;
  }

  /**
   * Format analysis results into readable string
   * 
   * @param mstEdges List of edges in minimum spanning tree
   * @return Formatted analysis results
   */
  private String formatAnalysisResults(ILista mstEdges) throws PosException, VacioException, NullException {
    StringBuilder result = new StringBuilder();

    // Calculate basic metrics
    NetworkMetrics metrics = calculateNetworkMetrics(mstEdges);

    // Format basic information
    result.append("La cantidad de nodos conectada a la red de expansión mínima es: ")
        .append(metrics.connectedNodes)
        .append("\nEl costo total es de: ")
        .append(String.format("%.2f", metrics.totalCost));

    // Find and format longest branch
    PilaEncadenada longestBranch = findLongestBranch(mstEdges);
    result.append("\nLa rama más larga está dada por los vértices: ");
    formatLongestBranch(result, longestBranch);

    return result.toString();
  }

  /**
   * Calculate network metrics from MST edges
   */
  private NetworkMetrics calculateNetworkMetrics(ILista mstEdges) throws PosException, VacioException {
    NetworkMetrics metrics = new NetworkMetrics();
    ITablaSimbolos processedNodes = new TablaHashSeparteChaining<>(2);

    for (int i = 1; i <= mstEdges.size(); i++) {
      Edge edge = (Edge) mstEdges.getElement(i);
      metrics.totalCost += edge.getWeight();

      // Count unique nodes
      if (!processedNodes.contains(edge.getSource().getId())) {
        processedNodes.put(edge.getSource().getId(), edge.getSource());
        metrics.connectedNodes++;
      }
      if (!processedNodes.contains(edge.getDestination().getId())) {
        processedNodes.put(edge.getDestination().getId(), edge.getDestination());
        metrics.connectedNodes++;
      }
    }

    return metrics;
  }

  /**
   * Find the longest branch in the MST
   * 
   * @throws NullException
   */
  private PilaEncadenada findLongestBranch(ILista mstEdges) throws PosException, VacioException, NullException {
    ITablaSimbolos nodeConnections = buildNodeConnectionsMap(mstEdges);
    ILista startNodes = findStartNodes(nodeConnections);

    PilaEncadenada longestBranch = new PilaEncadenada();
    int maxLength = 0;

    // Try paths from each starting node
    for (int i = 1; i <= startNodes.size(); i++) {
      Vertex startNode = (Vertex) startNodes.getElement(i);
      PilaEncadenada currentPath = new PilaEncadenada();
      ITablaSimbolos visited = new TablaHashSeparteChaining<>(2);

      explorePath(startNode, nodeConnections, visited, currentPath);

      if (currentPath.size() > maxLength) {
        maxLength = currentPath.size();
        longestBranch = currentPath;
      }
    }

    return longestBranch;
  }

  /**
   * Build map of node connections from MST edges
   * 
   * @throws NullException
   */
  private ITablaSimbolos buildNodeConnectionsMap(ILista mstEdges) throws PosException, VacioException, NullException {
    ITablaSimbolos nodeConnections = new TablaHashSeparteChaining<>(2);

    for (int i = 1; i <= mstEdges.size(); i++) {
      Edge edge = (Edge) mstEdges.getElement(i);

      // Add both directions of connection
      addConnection(nodeConnections, edge.getSource(), edge.getDestination());
      addConnection(nodeConnections, edge.getDestination(), edge.getSource());
    }

    return nodeConnections;
  }

  /**
   * Add a connection to the node connections map
   * 
   * @throws NullException
   * @throws PosException
   */
  private void addConnection(ITablaSimbolos nodeConnections, Vertex from, Vertex to)
      throws PosException, NullException {
    ILista connections = (ILista) nodeConnections.get(from.getId());
    if (connections == null) {
      connections = new ArregloDinamico<>(1);
      nodeConnections.put(from.getId(), connections);
    }
    connections.insertElement(to, connections.size() + 1);
  }

  /**
   * Find nodes with single connection (leaf nodes)
   */
  private ILista findStartNodes(ITablaSimbolos nodeConnections) {
    ILista startNodes = new ArregloDinamico<>(1);
    ILista nodes = nodeConnections.valueSet();

    try {
      for (int i = 1; i <= nodes.size(); i++) {
        ILista connections = (ILista) nodes.getElement(i);
        if (connections.size() == 1) {
          startNodes.insertElement(connections.getElement(1), startNodes.size() + 1);
        }
      }
    } catch (Exception e) {
      // Handle exception
    }

    return startNodes;
  }

  /**
   * Explore path recursively from a node
   */
  private void explorePath(Vertex current, ITablaSimbolos nodeConnections,
      ITablaSimbolos visited, PilaEncadenada currentPath) {
    visited.put(current.getId(), current);
    currentPath.push(current);

    ILista connections = (ILista) nodeConnections.get(current.getId());
    if (connections != null) {
      try {
        for (int i = 1; i <= connections.size(); i++) {
          Vertex next = (Vertex) connections.getElement(i);
          if (!visited.contains(next.getId())) {
            explorePath(next, nodeConnections, visited, currentPath);
          }
        }
      } catch (Exception e) {
        // Handle exception
      }
    }
  }

  /**
   * Format the longest branch information
   */
  private void formatLongestBranch(StringBuilder result, PilaEncadenada branch) {
    int count = 1;
    while (!branch.isEmpty() && count <= MAX_BRANCH_DISPLAY) {
      Vertex vertex = (Vertex) branch.pop();
      result.append("\nId ").append(count).append(" : ").append(vertex.getId());
      count++;
    }
  }

  /**
   * Helper class to store network metrics
   */
  private static class NetworkMetrics {
    int connectedNodes = 0;
    float totalCost = 0;
  }
}
