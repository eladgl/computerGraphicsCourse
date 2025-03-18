package app_interface;

import java.nio.IntBuffer;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.joml.Vector3f;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import your_code.ErrorLogger;
import your_code.WorldModel;
import your_code.YourSelectionEnum;

public class Main extends Application {
	// Constants
	private final int IMAGE_WIDTH = InterfaceDefaultParams.IMAGE_WIDTH;
	private final int IMAGE_HEIGHT = InterfaceDefaultParams.IMAGE_HEIGHT;

	// Unsaved parameters - can change during execution but are not saved between
	// runs.
	// loaded with their default value, and managed by the Main class during execution.

	// Saved parameters - these are managed by this class and persist between runs.
	// Managed by the InterfaceSavedParams class and not saved also by the Main class.
	private InterfaceSavedParams savedParams = new InterfaceSavedParams();

	// error logger
    private final ErrorLogger errorLogger = new ErrorLogger();
	private StringProperty labelOpenLogProperty = new SimpleStringProperty("");

	// The 3D model class that handles rendering
	private WorldModel worldModel = new WorldModel(IMAGE_WIDTH, IMAGE_HEIGHT, errorLogger);

	// User interface variables

	// Image variables
	private IntBuffer intBuffer;
	private IntBufferWrapper intBufferWrapper;
	private PixelBuffer<IntBuffer> pixelBuffer;
	private ImageView imageView;
	private long imageLastTimesUpdated = 0;
	private boolean imageLoaded;

	// labels
	private Label labelInfo1;
	private Label labelInfo2;
	private Label labelInfo3;
	private StringProperty labelInfo1StringProperty = new SimpleStringProperty("");
	private StringProperty labelInfo2StringProperty = new SimpleStringProperty("");
	private StringProperty labelInfo3StringProperty = new SimpleStringProperty("");
	private long labelLastUpdateTime = 0;

	// comboboxes
	private ComboBox<Integer> comboDepthOfRayTracing;
	private ComboBox<String> comboExercise;
	private ComboBox<YourSelectionEnum> comboYourSelection;

	// animation timer
	private AnimationTimer timer;

	// stage
	private Stage primaryStage;

	// measuring time
	private TimeMeasurement timeMeasurementRendering = new TimeMeasurement(10);
	private TimeMeasurement timeMeasurementDisplay = new TimeMeasurement(10);
	private int renderingPercentDone;

	
	// Loading the model and creating the window method
	//////////////////////////////////////////////////

	public void start(Stage primaryStage) throws Exception {


		// creating the application window layout
		//////////////////////////////////////////////////////

		// Buttons Dropdown menus row
		Label labelOpen = new Label(" ");
		Button buttonOpen = new Button("Open file...");
		VBox vboxButtonOpen = new VBox(labelOpen, buttonOpen);

		Label labelRender = new Label(" ");
		Button buttonRender = new Button("Start Rendering");
		VBox vboxButtonRender = new VBox(labelRender, buttonRender);

		Label labelStopRendering = new Label(" ");
		Button buttonStopRendering = new Button("Stop Rendering");
		VBox vboxButtonStopRendering = new VBox(labelStopRendering, buttonStopRendering);

		Label labelSave = new Label(" ");
		Button buttonSave = new Button("Save image ...");
		VBox vboxSave = new VBox(labelSave, buttonSave);

		Label labelDepthOfRayTracing = new Label("Depth of Ray-Tracing:");
		comboDepthOfRayTracing = new ComboBox<>();
		comboDepthOfRayTracing.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 30, 50, 100);
		comboDepthOfRayTracing.setValue(savedParams.getDepthOfRayTracing());
		VBox vboxDepthOfRayTracing = new VBox(labelDepthOfRayTracing, comboDepthOfRayTracing);

		Label labelSpace1 = new Label("      ");
		Label labelSpace2 = new Label("      ");
		VBox vSpace = new VBox(labelSpace1, labelSpace2);
		
		
		HBox buttonRow = new HBox(vboxButtonOpen, vboxButtonRender, vboxButtonStopRendering, vboxSave, vSpace, vboxDepthOfRayTracing);

		// Image
		intBuffer = IntBuffer.allocate(IMAGE_WIDTH * IMAGE_HEIGHT);
		intBufferWrapper = new IntBufferWrapper(intBuffer, IMAGE_WIDTH, IMAGE_HEIGHT);
		pixelBuffer = new PixelBuffer<>(IMAGE_WIDTH, IMAGE_HEIGHT, intBuffer, PixelFormat.getIntArgbPreInstance());
		imageView = new ImageView(new WritableImage(pixelBuffer));

		// labels
		labelInfo1 = new Label("");
		labelInfo2 = new Label("");
		labelInfo3 = new Label("");
		// Bind labels to the properties
		labelInfo1.textProperty().bind(labelInfo1StringProperty);
		labelInfo2.textProperty().bind(labelInfo2StringProperty);
		labelInfo3.textProperty().bind(labelInfo3StringProperty);

		// Exercise label and combobox
		Label labelExercise = new Label("Exercise: ");
		comboExercise = new ComboBox<>();
		for (ExerciseEnum ex : ExerciseEnum.values())
			comboExercise.getItems().add(ex.getDescription());
		// comboExercise.getItems().addAll(ExerciseEnum.values());
		comboExercise.setValue(savedParams.getExercise().getDescription());

		// Your selection label and combobox
		Label labelYourSelection = new Label("  Your selection: ");
		comboYourSelection = new ComboBox<>();
		comboYourSelection.getItems().addAll(YourSelectionEnum.values());
		comboYourSelection.setValue(YourSelectionEnum.values()[0]);

		HBox hboxExercise = new HBox(labelExercise, comboExercise, labelYourSelection, comboYourSelection);

		Label labelOpenLog = new Label(" ");
		labelOpenLog.setStyle("-fx-text-fill: red;");
		Button buttonOpenLog = new Button("Open Log ...");
		HBox hboxOpenLog = new HBox(buttonOpenLog, labelOpenLog);
		labelOpenLog.textProperty().bind(labelOpenLogProperty);
				
		// combining button and combobox row with image and lables
		VBox vbox = new VBox(buttonRow, imageView, labelInfo1, labelInfo2, labelInfo3, hboxExercise, hboxOpenLog);

		// creating scene setting the stage
		Scene scene = new Scene(vbox);
		primaryStage.setScene(scene);
		primaryStage.setTitle("3D Rasterization App");

		// Add event listeners
		//////////////////////////////////////////////////////

		// buttons
		buttonOpen.setOnAction(this::handleOpenFile);
		buttonRender.setOnAction(this::handleRender);
		buttonStopRendering.setOnAction(this::handleStopRendering);
		buttonSave.setOnAction(this::handleSaveFile);
		buttonOpenLog.setOnAction(this::handleOpenLog);

		comboDepthOfRayTracing.setOnAction(this::handleDepthOfRayTracing);
		comboExercise.setOnAction(this::handleExerciseChange);
		comboYourSelection.setOnAction(this::handleYourSelectionChange);

		// keyboard
		scene.setOnKeyPressed(this::handleKeyPressed);

		// mouse
		scene.setOnMousePressed(this::handleMousePress);
		scene.setOnMouseReleased(this::handleMouseReleases);
		scene.setOnMouseDragged(this::handleMouseDragged);
		scene.addEventFilter(ScrollEvent.SCROLL, this::handleMouseWheelScrolling);

		// Create animation timer
		timer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				timerRender();
			}
		};

		// Force rendering and label update after the stage is visible
		Platform.runLater(() -> {
			intBufferWrapper.fillImageWithColor(50f/255, 50f/255, 50f/255);
			intBufferWrapper.writeText("Loading model...", IMAGE_WIDTH/2-150, IMAGE_HEIGHT/2-20, 40, 100f/255, 200f/255, 100f/255);
			updateDisplay();

			ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
			executor.schedule(() -> {
				// Loading the model and initialize display parameters
				imageLoaded = worldModel.load(savedParams.getModelFileName());
				worldModel.setRenderingParams(savedParams.getDepthOfRayTracing());
				worldModel.setExercise(savedParams.getExercise());

				startRender();
			}, 0, TimeUnit.SECONDS);			

		});

		// Show the stage
		// scene.getRoot().requestFocus();
		primaryStage.setResizable(false);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	    System.exit(0); // Ensure the JVM terminates
	}

	// Updating window method -
	// image and labels are updated when parameters change
	//////////////////////////////////////////////////

	// Buttons handlers
	//////////////////////////////////////////////////

	// Open file
	private void handleOpenFile(ActionEvent event) {
		handleOpenFile();
	}
	private void handleOpenFile() {
		String filePath = Utilities.openFileChooser(primaryStage, "model",
				Paths.get(savedParams.getModelFileName()).getParent().toString());
		if (filePath != null) {
			intBufferWrapper.writeText("Loading model...", IMAGE_WIDTH/2-150, IMAGE_HEIGHT/2-20, 40, 100f/255, 200f/255, 100f/255);
			updateDisplay();

			ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
			executor.schedule(() -> {
				imageLoaded = worldModel.load(filePath);
				savedParams.setModelFileName(filePath);
				startRender();
			}, 1, TimeUnit.SECONDS);			
		}
	}

	
	
	private void handleRender(ActionEvent event) {
		startRender();
	}

	private void handleStopRendering(ActionEvent event) {
		stopRedering();
	}

	private void handleSaveFile(ActionEvent event) {
		String filePath = Utilities.saveFileChooser(primaryStage, "bmp", savedParams.getSaveImagePath());
		if (filePath != null) {
			intBufferWrapper.saveToBMP(filePath);
			savedParams.setSaveImagePath(Paths.get(filePath).getParent().toString());
		}
	}

	private void handleOpenLog(ActionEvent event) {
      	errorLogger.showErrorWindow();
	}
	
	
	int counter;
	RandomPixelIterable randomPixelIterable;
	Iterator<Integer[]> iterator;

	private void startRender() {
		if (imageLoaded || savedParams.getExercise().ordinal() <= ExerciseEnum.EX_1_2_Colors_Color_space.ordinal()) {
			counter = 0;
			randomPixelIterable = new RandomPixelIterable(IMAGE_WIDTH, IMAGE_HEIGHT);
			iterator = randomPixelIterable.iterator();
			intBufferWrapper.imageClear();
			timer.start();
		} else {
			Platform.runLater(() -> {
				//System.err.println("Fail to load file.");
				setDescriptionStrings();
				intBufferWrapper.fillImageWithColor(50f/255, 50f/255, 50f/255);
				intBufferWrapper.writeText("Fail to load model file !", IMAGE_WIDTH/2-200, IMAGE_HEIGHT/2-20, 40, 1f, 0, 0);
				updateDisplay();
				handleOpenFile();
			});
		}
		timeMeasurementRendering.start();
	}

	private void stopRedering() {
		timer.stop();
	}

	private void updateDisplay() {
		pixelBuffer.updateBuffer(b -> null);
		imageView.setImage(new WritableImage(pixelBuffer));


		imageLastTimesUpdated = System.nanoTime();
		imageView.requestFocus();
	}
		
	boolean firstTime = true;
	private void timerRender() {

		// Rendering
		while (System.nanoTime() - imageLastTimesUpdated < InterfaceDefaultParams.IMAGE_UPDATE_INTERVAL_IN_MS * 1_000_000) {
			for (int i = 0; i < IMAGE_WIDTH; i++) {
				if (!iterator.hasNext()) {
					if (!imageLoaded && savedParams.getExercise().ordinal() <= ExerciseEnum.EX_1_2_Colors_Color_space.ordinal()) 
						intBufferWrapper.writeText("Fail to load model file !", IMAGE_WIDTH/2-200, IMAGE_HEIGHT/2-20, 40, 1f, 0, 0);
					timer.stop();
					setDescriptionStrings();
					break;
				} else {
					Integer[] indexPercentArray = iterator.next();
					int imageIndex = indexPercentArray[0];
					renderingPercentDone = indexPercentArray[1];
					int imageX = imageIndex % IMAGE_WIDTH;
					int imageY = imageIndex / IMAGE_WIDTH;
					Vector3f pixelColor = worldModel.renderPixel(imageX, imageY);
					intBufferWrapper.setPixel(imageX, imageY, pixelColor);
				}
			}
		}

		// Update labels
		if (System.nanoTime() - labelLastUpdateTime > InterfaceDefaultParams.LABELS_UPDATE_INTERVAL_IN_MS * 1_000_000) {
			setDescriptionStrings();
			if(firstTime) {
				setPixelDescriptionString();
				firstTime = false;
			}
			labelLastUpdateTime = System.nanoTime();
		}
		
		// updating display
		updateDisplay();
	}

	private void calcPixelAndUpdateDescriptionString(MouseEvent event) {
		int cursorX = (int) event.getX();
		int cursorY = IMAGE_HEIGHT + InterfaceDefaultParams.SIZE_OF_UPPER_INTERFACE_ROW_IN_PIXELS - (int) event.getY();
		Vector3f pixelColor = worldModel.renderPixel(cursorX, cursorY);
		setPixelDescriptionString(cursorX, cursorY, pixelColor);
	}

	
	
	// Mouse press
	private void handleMousePress(MouseEvent event) {
		calcPixelAndUpdateDescriptionString(event);
	}

	// Mouse releases
	private void handleMouseReleases(MouseEvent event) {
	}

	// Mouse move
	private void handleMouseDragged(MouseEvent event) {
		calcPixelAndUpdateDescriptionString(event);
	}

	// Mouse move
	private void handleMouseWheelScrolling(ScrollEvent event) {
		event.consume();
	}


	private String descriptionString1() {
		String str = savedParams.getModelFileName();
		int maxStringLength = IMAGE_WIDTH/8;
		str = (str.length() > maxStringLength)
				? Paths.get(savedParams.getModelFileName()).getRoot() + " ... " + str.substring(str.length() - maxStringLength)
				: str;
		if (!imageLoaded)
			str += " - loading failed !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
		return String.format("Model file: %s", str);
	}

	private String descriptionString2() {
		return String.format(
				"Rendered %d%%. time: %.3f / %.3f seconds",
				renderingPercentDone, 
				(float)timeMeasurementRendering.getTimeFromStart()/1000,
				100f/(float)renderingPercentDone*timeMeasurementRendering.getTimeFromStart()/1000);
	}

	private void setDescriptionStrings() {
		labelInfo1StringProperty.set(descriptionString1());
		labelInfo2StringProperty.set(descriptionString2());
		
		if(errorLogger.getTotalCount()>0)
			labelOpenLogProperty.set(" Errors count: "+errorLogger.getTotalCount());
	}
	
	private void setPixelDescriptionString() {
		labelInfo3StringProperty.set("Press the mouse to calculate the color of a specific pixel in the image.");
	}
	private void setPixelDescriptionString(double x, double y, Vector3f pixelColor) {
		labelInfo3StringProperty.set(String.format("pixel %.0f,%.0f - color %.3f, %.3f, %.3f", x, y,
				pixelColor.x, pixelColor.y, pixelColor.z));
	}
	
	// Mouse and keybouard handlers
	//////////////////////////////////////////////////

	// Keyboord
	private void handleKeyPressed(KeyEvent event) {
		// For each key changing the state, updating the window acordingly and Consume
		// the event to prevent further propagation
		KeyCode keyCode = event.getCode();
		if (keyCode == KeyCode.UP) {
			event.consume();
		} else if (keyCode == KeyCode.DOWN) {
			event.consume();
		} else if (keyCode == KeyCode.LEFT) {
			event.consume();
		} else if (keyCode == KeyCode.RIGHT) {
			event.consume();
		}
	}

	// ComboBox handlers
	//////////////////////////////////////////////////
	
	private void handleDepthOfRayTracing(ActionEvent e) {
		savedParams.setDepthOfRayTracing(comboDepthOfRayTracing.getValue());
		worldModel.setRenderingParams(savedParams.getDepthOfRayTracing());
		stopRedering();
		startRender();
	}

	private void handleExerciseChange(ActionEvent e) {
		ExerciseEnum selectedExercise = ExerciseEnum.values()[comboExercise.getSelectionModel().getSelectedIndex()];
		savedParams.setExercise(selectedExercise);
		worldModel.setExercise(selectedExercise);
		stopRedering();
		startRender();
	}

	private void handleYourSelectionChange(ActionEvent e) {
		YourSelectionEnum yourSelection = comboYourSelection.getValue();
		worldModel.setYourSelection(yourSelection);
		stopRedering();
		startRender();
	}

	// methods for setting parameters of model rendering
	//////////////////////////////////////////////////


}
