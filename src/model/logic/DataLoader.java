package model.logic;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import model.data_structures.*;

public class DataLoader {
  public static ModelData loadData(String countriesPath, String landingPointsPath, String connectionsPath)
      throws IOException, NullException {
    ITablaSimbolos paises = new TablaHashLinearProbing(2);
    ITablaSimbolos points = new TablaHashLinearProbing(2);
    GrafoListaAdyacencia grafo = new GrafoListaAdyacencia(2);

    loadCountries(countriesPath, paises, grafo);
    loadLandingPoints(landingPointsPath, points);
    loadConnections(connectionsPath, grafo, paises, points);

    return new ModelData(grafo, paises, points);
  }

  private static void loadCountries(String path, ITablaSimbolos paises, GrafoListaAdyacencia grafo) throws IOException {
    Reader in = new FileReader(path);
    Iterable<CSVRecord> records = CSVFormat.RFC4180.withHeader().parse(in);

    for (CSVRecord record : records) {
      if (!record.get(0).isEmpty()) {
        Country country = createCountryFromRecord(record);
        grafo.insertVertex(country.getCapitalName(), country);
        paises.put(country.getCountryName(), country);
      }
    }
  }

  private static Country createCountryFromRecord(CSVRecord record) {
    return new Country(
        record.get(0), // countryName
        record.get(1), // capitalName
        Double.parseDouble(record.get(2)), // latitude
        Double.parseDouble(record.get(3)), // longitude
        record.get(4), // code
        record.get(5), // continentName
        Float.parseFloat(record.get(6).replace(".", "")), // population
        Double.parseDouble(record.get(7).replace(".", ""))// users
    );
  }

  private static void loadLandingPoints(String path, ITablaSimbolos points) throws IOException {
    Reader in = new FileReader(path);
    Iterable<CSVRecord> records = CSVFormat.RFC4180.withHeader().parse(in);

    for (CSVRecord record : records) {
      Landing landing = createLandingFromRecord(record);
      points.put(landing.getLandingId(), landing);
    }
  }

  private static Landing createLandingFromRecord(CSVRecord record) {
    String[] locationParts = record.get(2).split(", ");
    String name = locationParts[0];
    String paisnombre = locationParts[locationParts.length - 1];

    return new Landing(
        record.get(0), // landingId
        record.get(1), // id
        name, // name
        paisnombre, // country
        Double.parseDouble(record.get(3)), // latitude
        Double.parseDouble(record.get(4)) // longitude
    );
  }

  private static void loadConnections(String path, GrafoListaAdyacencia grafo,
      ITablaSimbolos paises, ITablaSimbolos points) throws IOException {
    Reader in = new FileReader(path);
    Iterable<CSVRecord> records = CSVFormat.RFC4180.withHeader().parse(in);

    for (CSVRecord record : records) {
      processConnection(record, grafo, paises, points);
    }
  }

  private static void processConnection(CSVRecord record, GrafoListaAdyacencia grafo,
      ITablaSimbolos paises, ITablaSimbolos points) {
    String origin = record.get(0);
    String destination = record.get(1);
    String cableId = record.get(3);

    Landing landing1 = (Landing) points.get(origin);
    Landing landing2 = (Landing) points.get(destination);

    if (landing1 != null && landing2 != null) {
      addConnectionToGraph(grafo, landing1, landing2, cableId, paises);
    }
  }

  private static void addConnectionToGraph(GrafoListaAdyacencia grafo, Landing landing1,
      Landing landing2, String cableId, ITablaSimbolos paises) {
    String vertex1Id = landing1.getLandingId() + cableId;
    String vertex2Id = landing2.getLandingId() + cableId;

    grafo.insertVertex(vertex1Id, landing1);
    grafo.insertVertex(vertex2Id, landing2);

    float weight = GeoCalculator.calculateDistance(
        landing1.getLongitude(), landing1.getLatitude(),
        landing2.getLongitude(), landing2.getLatitude());

    grafo.addEdge(vertex1Id, vertex2Id, weight);

    connectLandingToCountry(grafo, landing1, vertex1Id, paises);
    connectLandingToCountry(grafo, landing2, vertex2Id, paises);
  }

  private static void connectLandingToCountry(GrafoListaAdyacencia grafo, Landing landing,
      String vertexId, ITablaSimbolos paises) {
    Country country = (Country) paises.get(landing.getPais());
    if (country != null) {
      float weight = GeoCalculator.calculateDistance(
          country.getLongitude(), country.getLatitude(),
          landing.getLongitude(), landing.getLatitude());
      grafo.addEdge(country.getCapitalName(), vertexId, weight);
    }
  }
}
