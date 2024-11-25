package model.logic;

import model.data_structures.*;
import utils.Ordenamiento;

/**
 * Analyzer class for assessing the impact of landing points on countries
 */
public class CountryImpactAnalyzer {
  private final ModelData modelData;

  /**
   * Constructor for CountryImpactAnalyzer
   * 
   * @param modelData The model data containing graph and country information
   */
  public CountryImpactAnalyzer(ModelData modelData) {
    this.modelData = modelData;
  }

  /**
   * Analyze the impact of a landing point on countries
   * 
   * @param punto Landing point name
   * @return List of affected countries sorted by distance
   */
  public ILista analyzeCountryImpact(String punto) {
    try {
      // Get landing point code and vertices
      String codigo = (String) modelData.getNombreCodigo().get(punto);
      if (codigo == null) {
        return new ArregloDinamico<>(1); // Return empty list if point not found
      }

      ILista landingVertices = (ILista) modelData.getLandingIdTabla().get(codigo);
      if (landingVertices == null || landingVertices.isEmpty()) {
        return new ArregloDinamico<>(1);
      }

      // Collect and analyze affected countries
      ILista affectedCountries = collectAffectedCountries(landingVertices);
      return sortAndUnifyCountries(affectedCountries);

    } catch (Exception e) {
      System.err.println("Error in country impact analysis: " + e.getMessage());
      return new ArregloDinamico<>(1);
    }
  }

  /**
   * Get string representation of country impact analysis
   * 
   * @param punto Landing point name
   * @return Formatted string with analysis results
   */
  public String getCountryImpactString(String punto) {
    ILista affectedCountries = analyzeCountryImpact(punto);

    StringBuilder result = new StringBuilder();
    result.append("La cantidad de países afectados es: ")
        .append(affectedCountries.size())
        .append("\nLos países afectados son: ");

    formatCountryImpactResults(result, affectedCountries);

    return result.toString();
  }

  /**
   * Collect affected countries from landing point vertices
   * 
   * @param landingVertices List of vertices associated with landing point
   * @return List of affected countries with distances
   */
  private ILista collectAffectedCountries(ILista landingVertices)
      throws PosException, VacioException, NullException {
    ILista countries = new ArregloDinamico<>(1);

    // Add country of original landing point
    Landing originalLanding = (Landing) ((Vertex) landingVertices.getElement(1)).getInfo();
    Country originalCountry = (Country) modelData.getPaises().get(originalLanding.getPais());
    if (originalCountry != null) {
      countries.insertElement(originalCountry, countries.size() + 1);
    }

    // Process each landing point vertex
    for (int i = 1; i <= landingVertices.size(); i++) {
      Vertex vertex = (Vertex) landingVertices.getElement(i);
      processVertexConnections(vertex, countries, originalLanding);
    }

    return countries;
  }

  /**
   * Process connections from a vertex to find affected countries
   * 
   * @param vertex          Vertex to process
   * @param countries       List to store affected countries
   * @param originalLanding Original landing point
   */
  private void processVertexConnections(Vertex vertex, ILista countries, Landing originalLanding)
      throws PosException, VacioException, NullException {
    ILista edges = vertex.edges();

    for (int j = 1; j <= edges.size(); j++) {
      Edge edge = (Edge) edges.getElement(j);
      Vertex destinationVertex = edge.getDestination();

      if (destinationVertex.getInfo() instanceof Landing) {
        Landing destLanding = (Landing) destinationVertex.getInfo();
        Country country = (Country) modelData.getPaises().get(destLanding.getPais());

        if (country != null) {
          float distance = GeoCalculator.calculateDistance(
              country.getLongitude(), country.getLatitude(),
              destLanding.getLongitude(), destLanding.getLatitude());
          country.setDistlan(distance);
          countries.insertElement(country, countries.size() + 1);
        }
      } else if (destinationVertex.getInfo() instanceof Country) {
        Country country = (Country) destinationVertex.getInfo();
        float distance = GeoCalculator.calculateDistance(
            country.getLongitude(), country.getLatitude(),
            originalLanding.getLongitude(), originalLanding.getLatitude());
        country.setDistlan(distance);
        countries.insertElement(country, countries.size() + 1);
      }
    }
  }

  /**
   * Sort and unify list of countries
   * 
   * @param countries List of countries to process
   * @return Sorted and unified list of countries
   */
  private ILista sortAndUnifyCountries(ILista countries) {
    try {
      // First unify countries to remove duplicates
      ILista unified = unifyCountries(countries);

      // Sort by distance
      Ordenamiento<Country> sorter = new Ordenamiento<>();
      sorter.ordenarMergeSort(unified, new Country.ComparadorXKm(), true);

      return unified;
    } catch (Exception e) {
      System.err.println("Error sorting countries: " + e.getMessage());
      return countries;
    }
  }

  /**
   * Unify countries list to remove duplicates
   * 
   * @param countries List of countries to unify
   * @return Unified list without duplicates
   * @throws NullException
   */
  private ILista unifyCountries(ILista countries) throws PosException, VacioException, NullException {
    ILista unified = new ArregloDinamico<>(1);
    ITablaSimbolos processedCountries = new TablaHashSeparteChaining<>(2);

    for (int i = 1; i <= countries.size(); i++) {
      Country country = (Country) countries.getElement(i);
      if (!processedCountries.contains(country.getCountryName())) {
        processedCountries.put(country.getCountryName(), country);
        unified.insertElement(country, unified.size() + 1);
      }
    }

    return unified;
  }

  /**
   * Format country impact results into string
   * 
   * @param result    StringBuilder to append results to
   * @param countries List of affected countries
   */
  private void formatCountryImpactResults(StringBuilder result, ILista countries) {
    try {
      for (int i = 1; i <= countries.size(); i++) {
        Country country = (Country) countries.getElement(i);
        result.append("\nNombre: ").append(country.getCountryName())
            .append("\nDistancia al landing point: ")
            .append(String.format("%.2f", country.getDistlan()));
      }
    } catch (Exception e) {
      result.append("\nError formatting country results: ").append(e.getMessage());
    }
  }

  /**
   * Get detailed impact metrics for a country
   */
  public CountryImpactMetrics getCountryImpactMetrics(String countryName) {
    try {
      Country country = (Country) modelData.getPaises().get(countryName);
      if (country == null) {
        return null;
      }

      return calculateCountryMetrics(country);
    } catch (Exception e) {
      System.err.println("Error calculating country metrics: " + e.getMessage());
      return null;
    }
  }

  /**
   * Calculate detailed metrics for a country
   */
  private CountryImpactMetrics calculateCountryMetrics(Country country) {
    CountryImpactMetrics metrics = new CountryImpactMetrics();
    metrics.countryName = country.getCountryName();
    metrics.population = country.getPopulation();
    metrics.internetUsers = country.getUsers();
    metrics.connectedLandingPoints = countConnectedLandingPoints(country);
    metrics.averageDistance = calculateAverageDistance(country);

    return metrics;
  }

  /**
   * Count number of landing points connected to a country
   */
  private int countConnectedLandingPoints(Country country) {
    int count = 0;
    ILista vertices = modelData.getGrafo().vertices();

    try {
      for (int i = 1; i <= vertices.size(); i++) {
        Vertex vertex = (Vertex) vertices.getElement(i);
        if (vertex.getInfo() instanceof Landing) {
          Landing landing = (Landing) vertex.getInfo();
          if (landing.getPais().equals(country.getCountryName())) {
            count++;
          }
        }
      }
    } catch (Exception e) {
      System.err.println("Error counting landing points: " + e.getMessage());
    }

    return count;
  }

  /**
   * Calculate average distance to connected landing points
   */
  private float calculateAverageDistance(Country country) {
    float totalDistance = 0;
    int count = 0;
    ILista vertices = modelData.getGrafo().vertices();

    try {
      for (int i = 1; i <= vertices.size(); i++) {
        Vertex vertex = (Vertex) vertices.getElement(i);
        if (vertex.getInfo() instanceof Landing) {
          Landing landing = (Landing) vertex.getInfo();
          if (landing.getPais().equals(country.getCountryName())) {
            float distance = GeoCalculator.calculateDistance(
                country.getLongitude(), country.getLatitude(),
                landing.getLongitude(), landing.getLatitude());
            totalDistance += distance;
            count++;
          }
        }
      }
    } catch (Exception e) {
      System.err.println("Error calculating average distance: " + e.getMessage());
    }

    return count > 0 ? totalDistance / count : 0;
  }

  /**
   * Class to hold detailed country impact metrics
   */
  public static class CountryImpactMetrics {
    public String countryName;
    public float population;
    public double internetUsers;
    public int connectedLandingPoints;
    public float averageDistance;

    @Override
    public String toString() {
      return String.format(
          "Country: %s\n" +
              "Population: %.0f\n" +
              "Internet Users: %.0f\n" +
              "Connected Landing Points: %d\n" +
              "Average Distance to Landing Points: %.2f km",
          countryName, population, internetUsers,
          connectedLandingPoints, averageDistance);
    }
  }
}
