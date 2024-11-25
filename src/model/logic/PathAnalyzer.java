package model.logic;

import model.data_structures.*;

/**
 * Analyzer class for finding and analyzing paths between countries
 */
public class PathAnalyzer {
  private final ModelData modelData;

  /**
   * Constructor for PathAnalyzer
   * 
   * @param modelData The model data containing graph and country information
   */
  public PathAnalyzer(ModelData modelData) {
    this.modelData = modelData;
  }

  /**
   * Find and analyze the minimum path between two countries
   * 
   * @param pais1 First country name
   * @param pais2 Second country name
   * @return Detailed string description of the path
   */
  public String findMinimumPath(String pais1, String pais2) {
    try {
      Country country1 = (Country) modelData.getPaises().get(pais1);
      Country country2 = (Country) modelData.getPaises().get(pais2);

      if (country1 == null || country2 == null) {
        return "Error: One or both countries not found";
      }

      String capital1 = country1.getCapitalName();
      String capital2 = country2.getCapitalName();

      PilaEncadenada path = modelData.getGrafo().minPath(capital1, capital2);

      return formatPathResult(path);

    } catch (Exception e) {
      return "Error finding path: " + e.getMessage();
    }
  }

  /**
   * Format the path result into a readable string
   * 
   * @param path Stack containing the path edges
   * @return Formatted string describing the path
   */
  private String formatPathResult(PilaEncadenada path) {
    if (path == null || path.isEmpty()) {
      return "No path found between the specified countries";
    }

    StringBuilder result = new StringBuilder("Ruta: ");
    float totalDistance = 0;

    while (!path.isEmpty()) {
      try {
        Edge edge = (Edge) path.pop();
        PathSegment segment = createPathSegment(edge);

        float distance = GeoCalculator.calculateDistance(
            segment.sourceLong, segment.sourceLat,
            segment.destLong, segment.destLat);

        totalDistance += distance;

        result.append("\n\nOrigen: ").append(segment.sourceName)
            .append("  Destino: ").append(segment.destName)
            .append("  Distancia: ").append(String.format("%.2f", distance));

      } catch (Exception e) {
        result.append("\nError processing path segment: ").append(e.getMessage());
      }
    }

    result.append("\nDistancia total: ").append(String.format("%.2f", totalDistance));
    return result.toString();
  }

  /**
   * Helper class to store path segment information
   */
  private static class PathSegment {
    final double sourceLong;
    final double sourceLat;
    final double destLong;
    final double destLat;
    final String sourceName;
    final String destName;

    PathSegment(double sourceLong, double sourceLat, double destLong, double destLat,
        String sourceName, String destName) {
      this.sourceLong = sourceLong;
      this.sourceLat = sourceLat;
      this.destLong = destLong;
      this.destLat = destLat;
      this.sourceName = sourceName;
      this.destName = destName;
    }
  }

  /**
   * Create a path segment from an edge
   * 
   * @param edge The edge to process
   * @return PathSegment containing processed information
   */
  private PathSegment createPathSegment(Edge edge) {
    double sourceLong = 0, sourceLat = 0, destLong = 0, destLat = 0;
    String sourceName = "", destName = "";

    // Process source vertex
    Object sourceInfo = edge.getSource().getInfo();
    if (sourceInfo instanceof Landing) {
      Landing landing = (Landing) sourceInfo;
      sourceLong = landing.getLongitude();
      sourceLat = landing.getLatitude();
      sourceName = landing.getLandingId();
    } else if (sourceInfo instanceof Country) {
      Country country = (Country) sourceInfo;
      sourceLong = country.getLongitude();
      sourceLat = country.getLatitude();
      sourceName = country.getCapitalName();
    }

    // Process destination vertex
    Object destInfo = edge.getDestination().getInfo();
    if (destInfo instanceof Landing) {
      Landing landing = (Landing) destInfo;
      destLong = landing.getLongitude();
      destLat = landing.getLatitude();
      destName = landing.getLandingId();
    } else if (destInfo instanceof Country) {
      Country country = (Country) destInfo;
      destLong = country.getLongitude();
      destLat = country.getLatitude();
      destName = country.getCapitalName();
    }

    return new PathSegment(sourceLong, sourceLat, destLong, destLat, sourceName, destName);
  }

  /**
   * Calculate the total distance of a path
   * 
   * @param path Stack containing the path edges
   * @return Total distance of the path
   */
  public float calculateTotalDistance(PilaEncadenada path) {
    if (path == null || path.isEmpty()) {
      return 0;
    }

    float totalDistance = 0;
    PilaEncadenada tempPath = new PilaEncadenada();

    // Copy elements to temporary stack to preserve original
    while (!path.isEmpty()) {
      Edge edge = (Edge) path.pop();
      tempPath.push(edge);
    }

    // Calculate total distance
    while (!tempPath.isEmpty()) {
      Edge edge = (Edge) tempPath.pop();
      PathSegment segment = createPathSegment(edge);

      float distance = GeoCalculator.calculateDistance(
          segment.sourceLong, segment.sourceLat,
          segment.destLong, segment.destLat);

      totalDistance += distance;
      path.push(edge); // Restore original stack
    }

    return totalDistance;
  }

  /**
   * Validate if a path exists between two points
   * 
   * @param point1 Starting point
   * @param point2 Ending point
   * @return true if a path exists, false otherwise
   */
  public boolean validatePath(String point1, String point2) {
    try {
      PilaEncadenada path = modelData.getGrafo().minPath(point1, point2);
      return path != null && !path.isEmpty();
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Get the number of hops (edges) in a path
   * 
   * @param path Stack containing the path edges
   * @return Number of hops in the path
   */
  public int getPathHops(PilaEncadenada path) {
    if (path == null) {
      return 0;
    }

    int hops = 0;
    PilaEncadenada tempPath = new PilaEncadenada();

    while (!path.isEmpty()) {
      tempPath.push(path.pop());
      hops++;
    }

    // Restore original stack
    while (!tempPath.isEmpty()) {
      path.push(tempPath.pop());
    }

    return hops;
  }
}
