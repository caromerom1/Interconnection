package model.logic;

import java.io.IOException;
import model.data_structures.*;
import utils.Ordenamiento;

/**
 * Main model class for the application
 */
public class Modelo {
	/**
	 * Model attributes
	 */
	private final ModelData modelData;
	private final ConnectedComponentsAnalyzer componentAnalyzer;
	private final LandingPointAnalyzer landingAnalyzer;
	private final PathAnalyzer pathAnalyzer;
	private final NetworkExpansionAnalyzer networkAnalyzer;
	private final CountryImpactAnalyzer countryAnalyzer;
	private final ILista datos;

	/**
	 * Constructor for the model with given capacity
	 * 
	 * @param capacidad Initial capacity for data structures
	 * @throws NullException
	 */
	public Modelo(int capacidad) throws NullException {
		datos = new ArregloDinamico<>(capacidad);
		try {
			this.modelData = DataLoader.loadData(
					"./data/countries.csv",
					"./data/landing_points.csv",
					"./data/connections.csv");
			this.componentAnalyzer = new ConnectedComponentsAnalyzer(modelData);
			this.landingAnalyzer = new LandingPointAnalyzer(modelData);
			this.pathAnalyzer = new PathAnalyzer(modelData);
			this.networkAnalyzer = new NetworkExpansionAnalyzer(modelData);
			this.countryAnalyzer = new CountryImpactAnalyzer(modelData);
		} catch (IOException e) {
			throw new RuntimeException("Failed to initialize model", e);
		}
	}

	/**
	 * Returns the size of the model's data
	 * 
	 * @return number of elements in the model
	 */
	public int darTamano() {
		return datos.size();
	}

	/**
	 * Get a specific element from the data
	 * 
	 * @param i index of the element
	 * @return YoutubeVideo object at the specified index
	 * @throws PosException   if position is invalid
	 * @throws VacioException if the data structure is empty
	 */
	public YoutubeVideo getElement(int i) throws PosException, VacioException {
		return (YoutubeVideo) datos.getElement(i);
	}

	/**
	 * Requirement 1: Analyze connected components between two points
	 * 
	 * @param punto1 first point name
	 * @param punto2 second point name
	 * @return analysis result as string
	 */
	public String req1String(String punto1, String punto2) {
		return componentAnalyzer.analyzeComponents(punto1, punto2);
	}

	/**
	 * Requirement 2: Analyze landing points
	 * 
	 * @return analysis result as string
	 */
	public String req2String() {
		return landingAnalyzer.analyzeLandingPoints();
	}

	/**
	 * Requirement 3: Find minimum path between two countries
	 * 
	 * @param pais1 first country name
	 * @param pais2 second country name
	 * @return path analysis result as string
	 */
	public String req3String(String pais1, String pais2) {
		return pathAnalyzer.findMinimumPath(pais1, pais2);
	}

	/**
	 * Requirement 4: Analyze network expansion
	 * 
	 * @return network expansion analysis result as string
	 */
	public String req4String() {
		return networkAnalyzer.analyzeNetworkExpansion();
	}

	/**
	 * Requirement 5: Analyze country impact for a specific point
	 * 
	 * @param punto landing point name
	 * @return list of affected countries
	 */
	public ILista req5(String punto) {
		return countryAnalyzer.analyzeCountryImpact(punto);
	}

	/**
	 * Requirement 5: Get string representation of country impact analysis
	 * 
	 * @param punto landing point name
	 * @return analysis result as string
	 */
	public String req5String(String punto) {
		return countryAnalyzer.getCountryImpactString(punto);
	}

	/**
	 * Load data from files
	 * 
	 * @throws IOException   if there's an error reading files
	 * @throws NullException
	 */
	public void cargar() throws IOException, NullException {
		DataLoader.loadData(
				"./data/countries.csv",
				"./data/landing_points.csv",
				"./data/connections.csv");
	}

	/**
	 * Helper method to unify lists based on a criterion
	 * 
	 * @param lista    list to unify
	 * @param criterio criterion for unification
	 * @return unified list
	 */
	public ILista unificar(ILista lista, String criterio) {
		if (criterio.equals("Vertice")) {
			return unificarVertices(lista);
		} else {
			return unificarCountries(lista);
		}
	}

	/**
	 * Unify vertices in a list
	 * 
	 * @param lista list of vertices to unify
	 * @return unified list
	 */
	private ILista unificarVertices(ILista lista) {
		ILista unified = new ArregloDinamico<>(1);
		try {
			Ordenamiento<Vertex<String, Landing>> sorter = new Ordenamiento<>();
			sorter.ordenarMergeSort(lista, new Vertex.ComparadorXKey(), false);

			for (int i = 1; i <= lista.size(); i++) {
				Vertex actual = (Vertex) lista.getElement(i);
				Vertex siguiente = i < lista.size() ? (Vertex) lista.getElement(i + 1) : null;

				if (siguiente == null || !actual.getId().equals(siguiente.getId())) {
					unified.insertElement(actual, unified.size() + 1);
				}
			}
		} catch (Exception e) {
			System.err.println("Error unifying vertices: " + e.getMessage());
		}
		return unified;
	}

	/**
	 * Unify countries in a list
	 * 
	 * @param lista list of countries to unify
	 * @return unified list
	 */
	private ILista unificarCountries(ILista lista) {
		ILista unified = new ArregloDinamico<>(1);
		try {
			Ordenamiento<Country> sorter = new Ordenamiento<>();
			sorter.ordenarMergeSort(lista, new Country.ComparadorXNombre(), false);

			for (int i = 1; i <= lista.size(); i++) {
				Country actual = (Country) lista.getElement(i);
				Country siguiente = i < lista.size() ? (Country) lista.getElement(i + 1) : null;

				if (siguiente == null || !actual.getCountryName().equals(siguiente.getCountryName())) {
					unified.insertElement(actual, unified.size() + 1);
				}
			}
		} catch (Exception e) {
			System.err.println("Error unifying countries: " + e.getMessage());
		}
		return unified;
	}

	/**
	 * Unify hash tables
	 * 
	 * @param lista list to create hash table from
	 * @return unified hash table
	 */
	public ITablaSimbolos unificarHash(ILista lista) {
		ITablaSimbolos tabla = new TablaHashSeparteChaining<>(2);
		try {
			Ordenamiento<Vertex<String, Landing>> sorter = new Ordenamiento<>();
			sorter.ordenarMergeSort(lista, new Vertex.ComparadorXKey(), false);

			for (int i = 1; i <= lista.size(); i++) {
				Vertex actual = (Vertex) lista.getElement(i);
				Vertex siguiente = i < lista.size() ? (Vertex) lista.getElement(i + 1) : null;

				if (siguiente == null || !actual.getId().equals(siguiente.getId())) {
					tabla.put(actual.getId(), actual);
				}
			}
		} catch (Exception e) {
			System.err.println("Error unifying hash: " + e.getMessage());
		}
		return tabla;
	}

	@Override
	public String toString() {
		StringBuilder info = new StringBuilder();
		info.append("Info básica:\n");
		info.append("El número total de conexiones (arcos) en el grafo es: ")
				.append(modelData.getGrafo().edges().size()).append("\n");
		info.append("El número total de puntos de conexión (landing points) en el grafo: ")
				.append(modelData.getGrafo().vertices().size()).append("\n");
		info.append("La cantidad total de países es: ")
				.append(modelData.getPaises().size()).append("\n");

		appendFirstLandingPointInfo(info);
		appendLastCountryInfo(info);

		return info.toString();
	}

	/**
	 * Append first landing point information to string builder
	 * 
	 * @param info StringBuilder to append to
	 */
	private void appendFirstLandingPointInfo(StringBuilder info) {
		try {
			Landing landing = (Landing) ((NodoTS) modelData.getPoints()
					.darListaNodos().getElement(1)).getValue();

			info.append("Info primer landing point\n")
					.append("Identificador: ").append(landing.getId()).append("\n")
					.append("Nombre: ").append(landing.getName()).append("\n")
					.append("Latitud: ").append(landing.getLatitude()).append("\n")
					.append("Longitud: ").append(landing.getLongitude()).append("\n");
		} catch (Exception e) {
			info.append("Error getting first landing point info\n");
		}
	}

	/**
	 * Append last country information to string builder
	 * 
	 * @param info StringBuilder to append to
	 */
	private void appendLastCountryInfo(StringBuilder info) {
		try {
			ILista nodos = modelData.getPaises().darListaNodos();
			Country pais = (Country) ((NodoTS) nodos.getElement(nodos.size())).getValue();

			info.append("Info último país:\n")
					.append("Capital: ").append(pais.getCapitalName()).append("\n")
					.append("Población: ").append(pais.getPopulation()).append("\n")
					.append("Usuarios: ").append(pais.getUsers()).append("\n");
		} catch (Exception e) {
			info.append("Error getting last country info\n");
		}
	}
}
