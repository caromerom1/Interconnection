package model.logic;

import model.data_structures.*;

public class ModelData {
  private final GrafoListaAdyacencia grafo;
  private final ITablaSimbolos paises;
  private final ITablaSimbolos points;
  private final ITablaSimbolos landingIdTabla;
  private final ITablaSimbolos nombreCodigo;

  public ModelData(GrafoListaAdyacencia grafo, ITablaSimbolos paises, ITablaSimbolos points) throws NullException {
    this.grafo = grafo;
    this.paises = paises;
    this.points = points;
    this.landingIdTabla = new TablaHashSeparteChaining(2);
    this.nombreCodigo = new TablaHashSeparteChaining(2);
    initializeTables();
  }

  private void initializeTables() throws NullException {
    try {
      ILista valueSet = points.valueSet();
      for (int i = 1; i <= valueSet.size(); i++) {
        Object point = valueSet.getElement(i);
        Landing landing = (Landing) point;
        ILista vertices = new ArregloDinamico<>(1);
        vertices.insertElement(grafo.getVertex(landing.getLandingId()), 1);
        landingIdTabla.put(landing.getLandingId(), vertices);
        nombreCodigo.put(landing.getName(), landing.getLandingId());
      }
    } catch (PosException | VacioException e) {
      System.err.println("Error initializing tables: " + e.getMessage());
    }
  }

  // Getters
  public GrafoListaAdyacencia getGrafo() {
    return grafo;
  }

  public ITablaSimbolos getPaises() {
    return paises;
  }

  public ITablaSimbolos getPoints() {
    return points;
  }

  public ITablaSimbolos getLandingIdTabla() {
    return landingIdTabla;
  }

  public ITablaSimbolos getNombreCodigo() {
    return nombreCodigo;
  }
}
