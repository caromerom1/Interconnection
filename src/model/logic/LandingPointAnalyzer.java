package model.logic;

import model.data_structures.*;

public class LandingPointAnalyzer implements ILandingPointAnalyzer {
  private final ModelData modelData;
  private static final int MAX_RESULTS = 10;

  public LandingPointAnalyzer(ModelData modelData) {
    this.modelData = modelData;
  }

  @Override
  public String analyzeLandingPoints() {
    StringBuilder result = new StringBuilder();
    ILista lista = modelData.getLandingIdTabla().valueSet();
    int contador = 0;

    try {
      for (int i = 1; i <= lista.size() && contador < MAX_RESULTS; i++) {
        ILista landingList = (ILista) lista.getElement(i);
        if (landingList.size() > 1) {
          result.append(analyzeSingleLandingPoint(landingList));
          contador++;
        }
      }
    } catch (Exception e) {
      return "Error analyzing landing points: " + e.getMessage();
    }

    return result.toString();
  }

  private String analyzeSingleLandingPoint(ILista landingList) throws PosException, VacioException {
    Landing landing = (Landing) ((Vertex) landingList.getElement(1)).getInfo();
    int connections = countConnections(landingList);

    return String.format("\n Landing \n Nombre: %s\n País: %s\n Id: %s\n Cantidad: %d",
        landing.getName(), landing.getPais(), landing.getId(), connections);
  }

  private int countConnections(ILista landingList) throws PosException, VacioException {
    int cantidad = 0;
    for (int j = 1; j <= landingList.size(); j++) {
      cantidad += ((Vertex) landingList.getElement(j)).edges().size();
    }
    return cantidad;
  }
}
