package ch.obermuhlner.salesman.viewer;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import ch.obermuhlner.salesman.distance.CartesianDistanceCalculator;
import ch.obermuhlner.salesman.distance.DistanceCalculator;
import ch.obermuhlner.salesman.distance.SphericalDistanceCalculator;
import ch.obermuhlner.salesman.model.CitiesLoader;
import ch.obermuhlner.salesman.model.City;
import ch.obermuhlner.salesman.model.Salesman;
import ch.obermuhlner.salesman.model.SalesmanListener;
import ch.obermuhlner.salesman.strategies.BestSalesman;
import ch.obermuhlner.salesman.strategies.BruteForceSalesman;
import ch.obermuhlner.salesman.strategies.GeneticSalesman;
import ch.obermuhlner.salesman.strategies.NearestSalesman;
import ch.obermuhlner.salesman.strategies.RandomSalesman;
import ch.obermuhlner.salesman.strategies.RepeatSalesman;
import ch.obermuhlner.salesman.util.MathUtil;
import ch.obermuhlner.salesman.util.ThreadInterruptedException;
import ch.obermuhlner.salesman.util.ThreadUtil;
import ch.obermuhlner.salesman.viewer.coordinate.CartesianScreenCoordinateCalculator;
import ch.obermuhlner.salesman.viewer.coordinate.EarthCoordinateCalculator;
import ch.obermuhlner.salesman.viewer.coordinate.ScreenCoordinateCalculator;
import ch.obermuhlner.salesman.viewer.coordinate.StandardSphereCoordinateCalculator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class SalesmanViewer extends Application {

	private static final Color BACKGROUND_COLOR = Color.BLACK;
	private static final Color GRID_COLOR = new Color(0.0, 0.7, 0.5, 0.05);
	private static final Color CITY_COLOR = Color.LIGHTBLUE;
	private static final Color SHADOW_COLOR = new Color(0.0, 0.0, 0.3, 1.0);
	private static final Color BEST_SOLUTION_COLOR = Color.YELLOW.brighter().brighter();
	private static final Color GOOD_SOLUTION_COLOR = Color.ORANGE;
	private static final Color MEDIUM_SOLUTION_COLOR = Color.DARKRED;
	private static final Color WORST_SOLUTION_COLOR = Color.TRANSPARENT;
	private static final Color DISCARDED_SOLUTION_COLOR = new Color(0.3, 0.3, 0.3, 1.0);
	
	private static final long SLEEP_TIME = 1;

	private enum SalesmanStrategy {
		Random,
		Nearest,
		Genetic,
		Bruteforce
	}

	private enum MapType {
		Cartesian,
		Earth,
		Moon,
		Mars
	}

	private static final int CANVAS_WIDTH = 1024;
	private static final int CANVAS_HEIGHT = 512;

	private static final DecimalFormat INTEGER_FORMAT = new DecimalFormat("##0");
	private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("##0.000");

	private Salesman salesman;
	private DistanceCalculator distanceCalculator;
	
	private GraphicsContext mapGc;
	private GraphicsContext citiesGc;

	private ObjectProperty<MapType> mapTypeProperty = new SimpleObjectProperty<>(MapType.Cartesian);
	private IntegerProperty cityCountProperty = new SimpleIntegerProperty(10);
	private DoubleProperty possibleSolutionCountProperty = new SimpleDoubleProperty(MathUtil.factorialAsDouble(cityCountProperty.get()));
	
	private ObjectProperty<SalesmanStrategy> salesmanStrategyProperty = new SimpleObjectProperty<>(SalesmanStrategy.Random);
	
	private BooleanProperty geneticInitialSimpleSalesmanPopulationProperty = new SimpleBooleanProperty(true);
	private IntegerProperty geneticInitialRandomPopulationCountProperty = new SimpleIntegerProperty(100);
	private IntegerProperty geneticMutationCountProperty = new SimpleIntegerProperty(3);
	private IntegerProperty geneticEvolutionGenerationCountProperty = new SimpleIntegerProperty(10);
	private IntegerProperty stepCountProperty = new SimpleIntegerProperty(10000);

	private BooleanProperty showDiscardedSolutionsProperty = new SimpleBooleanProperty(true);

	private IntegerProperty currentStepProperty = new SimpleIntegerProperty(0);
	private DoubleProperty currentBestDistanceProperty = new SimpleDoubleProperty(0);

	private ComboBox<MapType> mapTypeComboBox;
	private TextField cityCountTextField;
	private Button createMapButton;
	private Button startButton;
	private Button cancelButton;

	private ImageView backgroundImageView;
	private ScreenCoordinateCalculator screenCoordinateCalculator;

	private TextArea descriptionTextArea;

	private List<City> cities;
	private List<List<City>> improvedSolutions;
	private List<List<City>> discardedSolutions;
	
	private Thread backgroundThread;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setOnCloseRequest((event) -> {
			cancelSimulation();
		});
		
		Group root = new Group();
        Scene scene = new Scene(root);

        BorderPane mainBorderPane = new BorderPane();
        root.getChildren().add(mainBorderPane);

        StackPane stackPane = new StackPane();
        mainBorderPane.setCenter(stackPane);
        
        Rectangle blackRectangle = new Rectangle(CANVAS_WIDTH, CANVAS_HEIGHT);
        blackRectangle.setFill(BACKGROUND_COLOR);
		stackPane.getChildren().add(blackRectangle);
        
        backgroundImageView = new ImageView();
        stackPane.getChildren().add(backgroundImageView);
        
        Canvas mapCanvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        stackPane.getChildren().add(mapCanvas);
        mapGc = mapCanvas.getGraphicsContext2D();
        
        Canvas citiesCanvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        stackPane.getChildren().add(citiesCanvas);
        citiesGc = citiesCanvas.getGraphicsContext2D();
        
        descriptionTextArea = new TextArea();
        descriptionTextArea.setEditable(false);
        mainBorderPane.setBottom(descriptionTextArea);
        
        mainBorderPane.setLeft(createEditor());
        
		primaryStage.setScene(scene);
        primaryStage.show();
        
        drawMap(Collections.emptyList(), Collections.emptyList());
        drawCities(Collections.emptyList());
	}
	
	private Node createEditor() {
		GridPane gridPane = new GridPane();
        gridPane.setHgap(4);
        gridPane.setVgap(4);
        BorderPane.setMargin(gridPane, new Insets(4));

        int rowIndex = 0;
        
        mapTypeComboBox = addComboBox(gridPane, rowIndex++, "MapType", mapTypeProperty, Arrays.asList(MapType.values()));
        
        cityCountTextField = addTextField(gridPane, rowIndex++, "Cities", INTEGER_FORMAT, cityCountProperty);
        TextField possibleSolutionCountTextField = addTextField(gridPane, rowIndex++, "Possible Solutions", INTEGER_FORMAT, possibleSolutionCountProperty);
        possibleSolutionCountTextField.setEditable(false);

        createMapButton = new Button("Create Map");
    	gridPane.add(createMapButton, 0, rowIndex++);

    	addComboBox(gridPane, rowIndex++, "Salesman Strategy", salesmanStrategyProperty, Arrays.asList(SalesmanStrategy.values()));
    	
    	Collection<Node> geneticControls = new ArrayList<>();
    	geneticControls.add(addCheckBox(gridPane, rowIndex++, "Initial Simple Solutions", geneticInitialSimpleSalesmanPopulationProperty));
    	geneticControls.add(addTextField(gridPane, rowIndex++, "Initial Random Population", INTEGER_FORMAT, geneticInitialRandomPopulationCountProperty));
    	geneticControls.add(addTextField(gridPane, rowIndex++, "Mutation Count", INTEGER_FORMAT, geneticMutationCountProperty));
    	geneticControls.add(addTextField(gridPane, rowIndex++, "Generations", INTEGER_FORMAT, geneticEvolutionGenerationCountProperty));
        TextField stepCountTextField = addTextField(gridPane, rowIndex++, "Steps", INTEGER_FORMAT, stepCountProperty);
        
    	startButton = new Button("Start Simulation");
    	gridPane.add(startButton, 0, rowIndex++);
    	
    	cancelButton = new Button("Cancel Simulation");
    	gridPane.add(cancelButton, 0, rowIndex++);
    	
        addCheckBox(gridPane, rowIndex++, "Show Discarded Solutions", showDiscardedSolutionsProperty);

        TextField currentStepCountTextField = addTextField(gridPane, rowIndex++, "Current Steps", INTEGER_FORMAT, currentStepProperty);
        currentStepCountTextField.setEditable(false);
        TextField currentBestDistanceTextField = addTextField(gridPane, rowIndex++, "Current Best Distance", DOUBLE_FORMAT, currentBestDistanceProperty);
        currentBestDistanceTextField.setEditable(false);

        // listeners and events

        mapTypeProperty.addListener((observable, oldValue, newValue) -> {
        	if (newValue == MapType.Cartesian) {
        		backgroundImageView.setImage(null);
        	} else {
        		backgroundImageView.setImage(new Image("file:" + newValue.name() + ".jpg"));
        	}
        	
        	screenCoordinateCalculator = createScreenCoordinateCalculator(newValue);
        	
        	generateCities();
        });
        
    	createMapButton.addEventHandler(ActionEvent.ACTION, event -> {
        	generateCities();
    	});

    	startButton.addEventHandler(ActionEvent.ACTION, event -> {
        	setupSalesman();
        	
        	startSimulation((best) -> {
        		Platform.runLater(() -> {
        			setImprovedSolutions(Arrays.asList(best));
    				Platform.runLater(() -> {
    					if (!improvedSolutions.isEmpty()) {
    						currentBestDistanceProperty.set(distanceCalculator.distance(improvedSolutions.get(0)));
    					}
    					drawMap(improvedSolutions, Collections.emptyList());
    				});
        		});
        	});
        });
        
        cancelButton.addEventHandler(ActionEvent.ACTION, event -> {
        	cancelSimulation();
        });
        
        salesmanStrategyProperty.addListener((observable, oldValue, newValue) -> {
        	updateSalesmanStrategy(newValue, geneticControls, stepCountTextField, descriptionTextArea);
        });
        
        cityCountProperty.addListener((observable, oldValue, newValue) -> {
        	possibleSolutionCountProperty.set(MathUtil.factorialAsDouble(cityCountProperty.get()));
    		Platform.runLater(() -> {
            	if (newValue.intValue() > 99) {
            		cityCountProperty.set(99);
            	}
           		startButton.setDisable(newValue.intValue() < 3);
    			generateCities();
        	});
        });

        // initial state
        
    	screenCoordinateCalculator = createScreenCoordinateCalculator(mapTypeProperty.get());

        generateCities();

    	updateSalesmanStrategy(salesmanStrategyProperty.get(), geneticControls, stepCountTextField, descriptionTextArea);
        updateSimulationRunning(false);
        
        return gridPane;
	}

	private void updateSalesmanStrategy(SalesmanStrategy salesmanStrategy, Collection<Node> geneticControls,
			TextField stepCountTextField, TextArea descriptionTextArea) {
		switch (salesmanStrategy) {
		case Genetic:
			geneticControls.forEach(node -> node.setDisable(false));
			stepCountTextField.setDisable(false);
			descriptionTextArea.setText(
					"Uses a genetic algorithm to solve the traveling salesman problem.\n"
					+ "\n"
					+ "The algorithm maintains a population of best solutions.\n"
					+ "This population is allowed to have children with a determined mutation rate over several generations.\n"
					+ "Then the population is culled to the original size, killing the less optimal solutions.\n"
					+ "This cycle is repeated many times, hopefully finding better and better solutions.\n"
					+ "\n"
					+ "Genetic algorithms are not guaranteed to find the optimum solution but can be parametrized to find a good solution in a reasonable time.");
			break;
		case Bruteforce:
			geneticControls.forEach(node -> node.setDisable(true));
			stepCountTextField.setDisable(true);
			descriptionTextArea.setText(
					"Uses brute force to solve the traveling salesman problem.\n"
					+ "\n"
					+ "The brute force algorithm is guaranteed to find the optimum solution,"
					+ "but can take a long time to find it because the number of possible solutions grows very fast with the number of cities.");
			break;
		case Nearest:
			geneticControls.forEach(node -> node.setDisable(true));
			stepCountTextField.setDisable(true);
			descriptionTextArea.setText(
					"Uses a simple heuristic algorithm to solve the traveling salesman problem by connecting consecutively to the nearest city.\n"
					+ "\n"
					+ "Because of its greedy nature the nearest city algorithm is not guaranteed to find the optimum solution but it is very fast.");
			break;
		case Random:
			geneticControls.forEach(node -> node.setDisable(true));
			stepCountTextField.setDisable(false);
			descriptionTextArea.setText(
					"Randomly travels from one city to another to solve the traveling salesman problem.\n"
					+ "\n"
					+ "This algorithm is very inefficient and gives no guarantees when (and if) it will find a better solution.\n");
			break;
		}
	}

	private void updateSimulationRunning(boolean simulationRunning) {
		mapTypeComboBox.setDisable(simulationRunning);
		cityCountTextField.setDisable(simulationRunning);
		createMapButton.setDisable(simulationRunning);
		startButton.setDisable(simulationRunning);
		cancelButton.setDisable(!simulationRunning);
	}

	private final <T> TextField addTextField(GridPane gridPane, int rowIndex, String label, Format format, Property<T> property) {
		if (label != null) {
			gridPane.add(new Text(label), 0, rowIndex);
		}

		TextField valueTextField = new TextField();
		Bindings.bindBidirectional(valueTextField.textProperty(), property, format);
		gridPane.add(valueTextField, 1, rowIndex);
		
		return valueTextField;
	}

	private <T> ComboBox<T> addComboBox(GridPane gridPane, int rowIndex, String label, ObjectProperty<T> objectProperty, List<T> values) {
        gridPane.add(new Text(label), 0, rowIndex);
        
        ComboBox<T> valueComboBox = new ComboBox<>();
        valueComboBox.getItems().setAll(values);
        valueComboBox.setValue(objectProperty.get());
        Bindings.bindBidirectional(objectProperty, valueComboBox.valueProperty());
		gridPane.add(valueComboBox, 1, rowIndex);
		return valueComboBox;
	}

	private CheckBox addCheckBox(GridPane gridPane, int rowIndex, String label, BooleanProperty booleanProperty) {
        gridPane.add(new Text(label), 0, rowIndex);
        
        CheckBox valueCheckBox = new CheckBox();
        valueCheckBox.setSelected(booleanProperty.get());
        Bindings.bindBidirectional(booleanProperty, valueCheckBox.selectedProperty());
		gridPane.add(valueCheckBox, 1, rowIndex);
		return valueCheckBox;
	}

	private void setupSalesman() {
		SalesmanListener salesmanListener = new SalesmanListener() {
			@Override
			public void improvedSolutions(List<List<City>> improved) {
				setImprovedSolutions(deepCopy(improved));
				Platform.runLater(() -> {
					incrementCurrentStep();
					if (!improvedSolutions.isEmpty()) {
						currentBestDistanceProperty.set(distanceCalculator.distance(improvedSolutions.get(0)));
					}
					drawMap(improvedSolutions, discardedSolutions);
				});
				ThreadUtil.sleep(SLEEP_TIME);
			}
			
			@Override
			public void discardedSolutions(List<List<City>> discarded) {
				if (showDiscardedSolutionsProperty.get()) {
					setDiscardedSolutions(deepCopy(discarded));
					Platform.runLater(() -> {
						incrementCurrentStep();
						drawMap(improvedSolutions, discardedSolutions);
						clearDiscardedSolutions();
					});
					ThreadUtil.sleep(SLEEP_TIME);
				} else {
					Platform.runLater(() -> {
						incrementCurrentStep();
					});
				}
			}
		};
		
		SalesmanStrategy salesmanStrategy = salesmanStrategyProperty.get();
		salesman = createSalesman(salesmanStrategy, salesmanListener);
		
		distanceCalculator = createDistanceCalculator(mapTypeProperty.get());
	}

	private void incrementCurrentStep() {
		currentStepProperty.set(currentStepProperty.get() + 1);
	}

	private DistanceCalculator createDistanceCalculator(MapType mapType) {
		switch (mapType) {
		case Cartesian:
			return new CartesianDistanceCalculator();
		case Earth:
		case Moon:
		case Mars:
			return SphericalDistanceCalculator.earthKilometers();
		}
		
		throw new IllegalArgumentException("Unknown MapType: " + mapType);
	}

	private ScreenCoordinateCalculator createScreenCoordinateCalculator(MapType mapType) {
		switch (mapType) {
		case Cartesian:
			return new CartesianScreenCoordinateCalculator(CANVAS_WIDTH, CANVAS_HEIGHT);
		case Earth:
			return new EarthCoordinateCalculator(CANVAS_WIDTH, CANVAS_HEIGHT);
		case Mars:
		case Moon:
			return new StandardSphereCoordinateCalculator(CANVAS_WIDTH, CANVAS_HEIGHT);
		}
		
		throw new IllegalArgumentException("Unknown MapType: " + mapType);
	}

	private Salesman createSalesman(SalesmanStrategy salesmanStrategy, SalesmanListener salesmanListener) {
		switch (salesmanStrategy) {
			case Genetic: {
				GeneticSalesman geneticSalesman = new GeneticSalesman();
				geneticSalesman.setListener(salesmanListener);
				geneticSalesman.setInitialSimpleSalesmanPopulation(geneticInitialSimpleSalesmanPopulationProperty.get());
				geneticSalesman.setInitialRandomPopulationCount(geneticInitialRandomPopulationCountProperty.get());
				geneticSalesman.setMutationCount(geneticMutationCountProperty.get());
				geneticSalesman.setEvolutionGenerationCount(geneticEvolutionGenerationCountProperty.get());
				geneticSalesman.setEvolutionStepCount(stepCountProperty.get());
				return geneticSalesman;
			}
			case Random: {
				RepeatSalesman repeatSalesman = new RepeatSalesman(new RandomSalesman());
				repeatSalesman.setListener(salesmanListener);
				repeatSalesman.setRepeatCount(stepCountProperty.get()); // TODO its own property
				return repeatSalesman;
			}
			case Nearest: {
				BestSalesman bestSalesman = new BestSalesman();
				bestSalesman.setListener(salesmanListener);
				for (int i = 0; i < cities.size(); i++) {
					bestSalesman.add(new NearestSalesman(i));
				}
				return bestSalesman;
			}
			case Bruteforce: {
				BruteForceSalesman bruteForceSalesman = new BruteForceSalesman();
				bruteForceSalesman.setListener(salesmanListener);
				return bruteForceSalesman;
			}
		}
		
		throw new RuntimeException("Unknown SalesmanStrategy: " + salesmanStrategy);
	}
	
	private synchronized void setImprovedSolutions(List<List<City>> improved) {
		improvedSolutions = improved;
	}
	
	private synchronized void setDiscardedSolutions(List<List<City>> discarded) {
		discardedSolutions = discarded;
	}

	private synchronized void clearDiscardedSolutions() {
		discardedSolutions = null;
	}
	
	private void generateCities() {
		int count = cityCountProperty.get();
		
		MapType mapType = mapTypeProperty.get();
		if (mapType == MapType.Cartesian) {
			cities = new ArrayList<>();
			Random random = new Random();
			for (int i = 0; i < count; i++) {
				cities.add(createCity(mapTypeProperty.get(), random, i));
			}
		} else {
			cities = CitiesLoader.load(mapType.name() + ".csv", new Random(), count);
		}
		
		drawMap(Collections.emptyList(), Collections.emptyList());
        drawCities(cities);
	}

	private City createCity(MapType mapType, Random random, int index) {
		switch (mapType) {
		case Cartesian:
			return new City("City" + index, random.nextInt(CANVAS_WIDTH), random.nextInt(CANVAS_HEIGHT));
		case Earth:
		case Moon:
		case Mars:
			return new City("City" + index, random.nextDouble() * 360 - 180, random.nextDouble() * 180 - 90);
		}

		throw new IllegalArgumentException("Unknown MapType: " + mapType);
	}
	
	private synchronized void startSimulation(Consumer<List<City>> finishedCallback) {
		updateSimulationRunning(true);
		currentStepProperty.set(0);
		currentBestDistanceProperty.set(0);
		
		
		backgroundThread = new Thread(() -> {
			try {
				List<City> bestPath = salesman.bestPath(cities, distanceCalculator);
				finishedCallback.accept(bestPath);
			} catch (ThreadInterruptedException exception) {
				// ignore
			} finally {
				backgroundThread = null;
				updateSimulationRunning(false);
			}
		});
		
		backgroundThread.start();
	}
	
	private synchronized void cancelSimulation() {
		if (backgroundThread != null) {
			backgroundThread.interrupt();
		}
	}
	
	private synchronized void drawMap(List<List<City>> improved, List<List<City>> discarded) {
		mapGc.clearRect(0, 0, mapGc.getCanvas().getWidth(), mapGc.getCanvas().getHeight());
		
		if (discarded != null) {
			int offset = improved != null ? improved.size() : 0;
			for (int i = 0; i < discarded.size(); i++) {
				drawSolution(mapGc, discarded.get(i), i + offset, DISCARDED_SOLUTION_COLOR);
			}
		}
		
		if (improved != null && !improved.isEmpty()) {
			// draw solution paths
			for (int i = improved.size() - 1; i >= 0; i--) {
				Color color = getColor(i, improved.size());

				drawSolution(mapGc, improved.get(i), i, color);
			}
		}
	}
	
	private synchronized void drawCities(List<City> cities) {
		citiesGc.clearRect(0, 0, citiesGc.getCanvas().getWidth(), citiesGc.getCanvas().getHeight());

		citiesGc.setStroke(GRID_COLOR);
		double gridStepX = (screenCoordinateCalculator.getEndX() - screenCoordinateCalculator.getStartX()) / 10.0;
		double gridStepY = (screenCoordinateCalculator.getEndY() - screenCoordinateCalculator.getStartY()) / 10.0;
		for (double gridX = screenCoordinateCalculator.getStartX(); gridX < screenCoordinateCalculator.getEndX() + gridStepX/2; gridX += gridStepX) {
			for (double gridY = screenCoordinateCalculator.getStartY(); gridY < screenCoordinateCalculator.getEndY() + gridStepY/2; gridY += gridStepY) {
				citiesGc.strokeLine(
						screenCoordinateCalculator.toScreenX(gridX), screenCoordinateCalculator.toScreenY(screenCoordinateCalculator.getStartY()),
						screenCoordinateCalculator.toScreenX(gridX), screenCoordinateCalculator.toScreenY(screenCoordinateCalculator.getEndY()));
				citiesGc.strokeLine(
						screenCoordinateCalculator.toScreenX(screenCoordinateCalculator.getStartX()), screenCoordinateCalculator.toScreenY(gridY),
						screenCoordinateCalculator.toScreenX(screenCoordinateCalculator.getEndX()), screenCoordinateCalculator.toScreenY(gridY));
			}
		}
		
		citiesGc.setStroke(CITY_COLOR);
		// draw cities
		for (City city : cities) {
			double x = screenCoordinateCalculator.toScreenX(city.x);
			double y = screenCoordinateCalculator.toScreenY(city.y);
			
			double size = 10;
			citiesGc.setLineWidth(2);
			strokeOvalWithShadow(citiesGc, x-size/2, y-size/2, size, size, SHADOW_COLOR);
			
			citiesGc.setLineWidth(1);
			strokeTextWithShadow(citiesGc, city.name, x+5, y, SHADOW_COLOR);
		}
	}

	private void strokeOvalWithShadow(GraphicsContext gc, double x, double y, double width, double height, Paint shadow) {
		Paint originalStroke = gc.getStroke();

		gc.setStroke(shadow);
		gc.strokeOval(x-1, y-1, width, height);
		gc.strokeOval(x-1, y+1, width, height);
		gc.strokeOval(x+1, y+1, width, height);
		gc.strokeOval(x+1, y-1, width, height);

		gc.setStroke(originalStroke);
		gc.strokeOval(x, y, width, height);
	}
	
	private void strokeTextWithShadow(GraphicsContext gc, String text, double x, double y, Paint shadow) {
		Paint originalStroke = gc.getStroke();

		gc.setStroke(shadow);
		gc.strokeText(text, x-1, y-1);
		gc.strokeText(text, x-1, y+1);
		gc.strokeText(text, x+1, y+1);
		gc.strokeText(text, x+1, y-1);

		gc.setStroke(originalStroke);
		gc.strokeText(text, x, y);
	}
	
	private void drawSolution(GraphicsContext gc, List<City> solution, int offset, Color color) {
		gc.setFill(color);
		gc.setStroke(color);
		gc.setLineWidth(offset == 0 ? 2 : 1);
		
		City firstCity = null;
		double fromX = 0;
		double fromY = 0;
		for (City city : solution) {
			if (firstCity == null) {
				firstCity = city;
				fromX = screenCoordinateCalculator.toScreenX(city.x) + offset;
				fromY = screenCoordinateCalculator.toScreenY(city.y);
			} else {
				double toX = screenCoordinateCalculator.toScreenX(city.x) + offset;
				double toY = screenCoordinateCalculator.toScreenY(city.y);
				gc.strokeLine(fromX, fromY, toX, toY);
				fromX = toX;
				fromY = toY;
			}
		}
		if (firstCity != null) {
			double toX = screenCoordinateCalculator.toScreenX(firstCity.x) + offset;
			double toY = screenCoordinateCalculator.toScreenY(firstCity.y);
			gc.strokeLine(fromX, fromY, toX, toY);
		}
	}
	
	private Color getColor(int index, int count) {
		if (index == 0) {
			return BEST_SOLUTION_COLOR;
		}
		
		int half = count / 2;
		if (index <= half) {
			return GOOD_SOLUTION_COLOR.interpolate(MEDIUM_SOLUTION_COLOR, ((double) index) / half);
		} else {
			int otherHalf = count - half;
			return MEDIUM_SOLUTION_COLOR.interpolate(WORST_SOLUTION_COLOR, ((double) index - otherHalf) / otherHalf);
		}
	}

	private List<List<City>> deepCopy(List<List<City>> list) {
		return list.stream()
			.<List<City>> map(sublist -> sublist.stream().collect(Collectors.toList()))
			.collect(Collectors.toList());
	}

	public static void main(String[] args) {
		launch(args);
	}
}
