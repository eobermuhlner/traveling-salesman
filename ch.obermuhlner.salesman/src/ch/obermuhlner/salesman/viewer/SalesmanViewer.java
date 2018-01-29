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
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class SalesmanViewer extends Application {

	private static final Color VERY_DARK_GRAY = new Color(0.3, 0.3, 0.3, 1.0);

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

	private Salesman salesman;
	private DistanceCalculator distanceCalculator;
	
	private GraphicsContext mapGc;

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

	private ComboBox<MapType> mapTypeComboBox;
	private TextField cityCountTextField;
	private Button createMapButton;
	private Button startButton;
	private Button cancelButton;

	private ImageView backgroundImageView;

	private List<City> cities;
	private List<List<City>> improvedSolutions;
	private List<List<City>> discardedSolutions;
	private int stepCount = 0;
	
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
        blackRectangle.setFill(Color.BLACK);
		stackPane.getChildren().add(blackRectangle);
        
        backgroundImageView = new ImageView();
        stackPane.getChildren().add(backgroundImageView);
        
        Canvas mapCanvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        stackPane.getChildren().add(mapCanvas);
        mapGc = mapCanvas.getGraphicsContext2D();
        drawMap(mapGc, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        
        mainBorderPane.setLeft(createEditor());
        
		primaryStage.setScene(scene);
        primaryStage.show();
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
        possibleSolutionCountTextField.setDisable(true);

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

        // listeners and events

        mapTypeProperty.addListener((observable, oldValue, newValue) -> {
        	if (newValue == MapType.Cartesian) {
        		backgroundImageView.setImage(null);
        	} else {
        		backgroundImageView.setImage(new Image("file:" + newValue.name() + ".jpg"));
        	}

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
    					drawMap(mapGc, improvedSolutions, Collections.emptyList(), cities);
    				});
        		});
        	});
        });
        
        cancelButton.addEventHandler(ActionEvent.ACTION, event -> {
        	cancelSimulation();
        });
        
        salesmanStrategyProperty.addListener((observable, oldValue, newValue) -> {
        	updateSalesmanControls(newValue, geneticControls, stepCountTextField);
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
        
        generateCities();

    	updateSalesmanControls(salesmanStrategyProperty.get(), geneticControls, stepCountTextField);
        updateWidgetStates(false);
        
        return gridPane;
	}

	private void updateSalesmanControls(SalesmanStrategy salesmanStrategy, Collection<Node> geneticControls,
			TextField stepCountTextField) {
		switch (salesmanStrategy) {
		case Genetic:
			geneticControls.forEach(node -> node.setDisable(false));
			stepCountTextField.setDisable(false);
			break;
		case Bruteforce:
			geneticControls.forEach(node -> node.setDisable(true));
			stepCountTextField.setDisable(true);
			break;
		case Nearest:
			geneticControls.forEach(node -> node.setDisable(true));
			stepCountTextField.setDisable(true);
			break;
		case Random:
			geneticControls.forEach(node -> node.setDisable(true));
			stepCountTextField.setDisable(false);
			break;
		}
	}

	private void updateWidgetStates(boolean simulationRunning) {
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
					drawMap(mapGc, improvedSolutions, discardedSolutions, cities);
				});
				ThreadUtil.sleep(1);
			}
			
			@Override
			public void discardedSolutions(List<List<City>> discarded) {
				if (showDiscardedSolutionsProperty.get()) {
					setDiscardedSolutions(deepCopy(discarded));
					Platform.runLater(() -> {
						drawMap(mapGc, improvedSolutions, discardedSolutions, cities);
						clearDiscardedSolutions();
					});
					ThreadUtil.sleep(1);
				} else {
					stepCount++;
				}
			}
		};
		
		SalesmanStrategy salesmanStrategy = salesmanStrategyProperty.get();
		salesman = createSalesman(salesmanStrategy, salesmanListener);
		
		distanceCalculator = createDistanceCalculator(mapTypeProperty.get());
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
		stepCount++;
	}
	
	private synchronized void setDiscardedSolutions(List<List<City>> discarded) {
		discardedSolutions = discarded;
		stepCount++;
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
		
		
		drawMap(mapGc, Arrays.asList(), Collections.emptyList(), cities);
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
		updateWidgetStates(true);
		stepCount = 0;
		
		backgroundThread = new Thread(() -> {
			try {
				List<City> bestPath = salesman.bestPath(cities, distanceCalculator);
				finishedCallback.accept(bestPath);
			} catch (ThreadInterruptedException exception) {
				// ignore
			} finally {
				backgroundThread = null;
				updateWidgetStates(false);
			}
		});
		
		backgroundThread.start();
	}
	
	private synchronized void cancelSimulation() {
		if (backgroundThread != null) {
			backgroundThread.interrupt();
		}
	}
	
	private synchronized void drawMap(GraphicsContext gc, List<List<City>> improved, List<List<City>> discarded, List<City> cities) {
//		gc.setFill(Color.BLACK);
//		gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
		gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
		
		if (discarded != null) {
			int offset = improved != null ? improved.size() : 0;
			for (int i = 0; i < discarded.size(); i++) {
				drawSolution(gc, discarded.get(i), i + offset, VERY_DARK_GRAY);
			}
		}
		
		if (improved != null && !improved.isEmpty()) {
			// draw solution paths
			for (int i = improved.size() - 1; i >= 0; i--) {
				Color color = getColor(i, improved.size());

				drawSolution(gc, improved.get(i), i, color);
			}
		}

		// draw cities
		for (City city : cities) {
			double x = toScreenX(city);
			double y = toScreenY(city);
			
			double size = 10;
			gc.setStroke(Color.LIGHTBLUE);
			gc.setLineWidth(2);
			gc.strokeOval(x-size/2, y-size/2, size, size);
			
			gc.setLineWidth(1);
			gc.strokeText(city.name, x+5, y);
		}

		// draw texts
		gc.setLineWidth(1);
		double fontHeight = gc.getFont().getSize();
		double textY = fontHeight;
		gc.strokeText("Step: " + stepCount, 10, textY);
		textY += fontHeight;
		if (improvedSolutions != null && !improvedSolutions.isEmpty()) {
			gc.strokeText("Distance: " + distanceCalculator.distance(improvedSolutions.get(0)), 10, textY);
		}
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
				fromX = toScreenX(city) + offset;
				fromY = toScreenY(city);
			} else {
				double toX = toScreenX(city) + offset;
				double toY = toScreenY(city);
				gc.strokeLine(fromX, fromY, toX, toY);
				fromX = toX;
				fromY = toY;
			}
		}
		double toX = toScreenX(firstCity) + offset;
		double toY = toScreenY(firstCity);
		gc.strokeLine(fromX, fromY, toX, toY);
	}
	
	private Color getColor(int index, int count) {
		Color bestColor = Color.WHITE;
		Color goodColor = Color.ORANGE;
		Color mediumColor = Color.DARKRED;
		Color worstColor = Color.TRANSPARENT;
	
		if (index == 0) {
			return bestColor;
		}
		
		int half = count / 2;
		if (index <= half) {
			return goodColor.interpolate(mediumColor, ((double) index) / half);
		} else {
			int otherHalf = count - half;
			return mediumColor.interpolate(worstColor, ((double) index - otherHalf) / otherHalf);
		}
	}

	private double toScreenX(City city) {
		MapType mapType = mapTypeProperty.get();
		switch (mapType) {
		case Cartesian:
			return city.x;
		case Earth:
			return (city.x + 180) / 360 * CANVAS_WIDTH;
		case Moon:
		case Mars:
			return city.x / 360 * CANVAS_WIDTH;
		}
		
		throw new IllegalArgumentException("Unknown MapType: " + mapType);
	}

	private double toScreenY(City city) {
		MapType mapType = mapTypeProperty.get();
		switch (mapType) {
		case Cartesian:
			return city.y;
		case Earth:
		case Moon:
		case Mars:
			return (180 - (city.y + 90)) / 180 * CANVAS_HEIGHT;
		}
		
		throw new IllegalArgumentException("Unknown MapType: " + mapType);
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
