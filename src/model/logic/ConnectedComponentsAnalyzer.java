package model.logic;

import model.data_structures.*;

public class ConnectedComponentsAnalyzer implements IComponentAnalyzer {
  private final ModelData modelData;

  public ConnectedComponentsAnalyzer(ModelData modelData) {
    this.modelData = modelData;
  }

  @Override
  public String analyzeComponents(String punto1, String punto2) {
    ITablaSimbolos tabla = modelData.getGrafo().getSSC();
    int maxComponent = findMaxComponent(tabla);

    String result = "La cantidad de componentes conectados es: " + maxComponent;

    try {
      boolean sameCluster = checkSameCluster(punto1, punto2, tabla);
      result += "\n Los landing points " +
          (sameCluster ? "pertenecen" : "no pertenecen") +
          " al mismo clúster";
    } catch (Exception e) {
      result += "\n Error analyzing clusters: " + e.getMessage();
    }

    return result;
  }

  private int findMaxComponent(ITablaSimbolos tabla) {
    ILista lista = tabla.valueSet();
    int max = 0;

    try {
      for (int i = 1; i <= lista.size(); i++) {
        int value = (int) lista.getElement(i);
        if (value > max) {
          max = value;
        }
      }
    } catch (Exception e) {
      // Handle exception
    }

    return max;
  }

  private boolean checkSameCluster(String punto1, String punto2, ITablaSimbolos tabla)
      throws PosException, VacioException {
    String codigo1 = (String) modelData.getNombreCodigo().get(punto1);
    String codigo2 = (String) modelData.getNombreCodigo().get(punto2);

    Vertex vertice1 = (Vertex) ((ILista) modelData.getLandingIdTabla().get(codigo1)).getElement(1);
    Vertex vertice2 = (Vertex) ((ILista) modelData.getLandingIdTabla().get(codigo2)).getElement(1);

    int elemento1 = (int) tabla.get(vertice1.getId());
    int elemento2 = (int) tabla.get(vertice2.getId());

    return elemento1 == elemento2;
  }
}
