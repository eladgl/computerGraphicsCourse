package your_code;

import java.util.Arrays;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ErrorLogger {
    public enum ErrorType {
        EXAMPLE_ERROR_1("One example of error that can happen."),
        EXAMPLE_ERROR_2("Second example of error that can happen.");

        private final String description;
        private String firstOccurrence;
        private String lastOccurrence;

        ErrorType(String description) {
            this.description = description;
            this.firstOccurrence = "";
            this.lastOccurrence = "";
        }

        public String getDescription() {
            return description;
        }

        public String getFirstOccurrence() {
            return firstOccurrence;
        }

        public String getLastOccurrence() {
            return lastOccurrence;
        }

        public void setFirstOccurrence(String firstOccurrence) {
            this.firstOccurrence = firstOccurrence;
        }

        public void setLastOccurrence(String lastOccurrence) {
            this.lastOccurrence = lastOccurrence;
        }
    }

    private int[] errorCounts;
    private int totalCount;

    public ErrorLogger() {
        this.errorCounts = new int[ErrorType.values().length];
    }

    public void report(ErrorType errorType) {
    	totalCount++;
        errorCounts[errorType.ordinal()]++;
        String callerInfo = getCallerInfo();

        if (errorCounts[errorType.ordinal()] == 1) {
            errorType.setFirstOccurrence(callerInfo);
        }

        errorType.setLastOccurrence(callerInfo);
    }

    private String getCallerInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement element = stackTrace[3];
        return element.getFileName() + ":" + element.getLineNumber();
    }

    @Override
    public String toString() {
        StringBuilder report = new StringBuilder("Error Report:\n");
        report.append("Total errors number: " + totalCount + "\n\n");

        for (int i = 0; i < errorCounts.length; i++) {
            if (errorCounts[i] > 0) {
                ErrorType errorType = ErrorType.values()[i];
                report.append(String.format("%s: %d occurrences - %s\n",
                        errorType, errorCounts[i], errorType.getDescription()));
                report.append(String.format("   First Occurrence: %s\n", errorType.getFirstOccurrence()));
                report.append(String.format("   Last Occurrence: %s\n\n", errorType.getLastOccurrence()));
            }
        }
        return report.toString();
    }

    public int getTotalCount() {
    	return totalCount;
    }
    
    public void resetCounts() {
    	totalCount = 0;
        Arrays.fill(errorCounts, 0);
    }

    public void showErrorWindow() {
        Stage stage = new Stage();
        VBox layout = new VBox(10);
        Label errorLabel = new Label(toString());
 
        layout.getChildren().addAll(errorLabel);
        Scene scene = new Scene(layout, 500, 600);
        stage.setScene(scene);
        stage.setTitle("Error Report");
        stage.show();
    }

    public static void main(String[] args) {
        ErrorLogger logger = new ErrorLogger();
        logger.report(ErrorType.EXAMPLE_ERROR_1);
        logger.report(ErrorType.EXAMPLE_ERROR_2);
        logger.report(ErrorType.EXAMPLE_ERROR_1);
        System.out.println(logger);
    }
}
