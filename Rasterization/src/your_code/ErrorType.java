package your_code;

public enum ErrorType {
    BOUNDING_BOX_INCORRECT_ORDER("Bounding box field order incorrect"),
    BOUNDING_BOX_COLLINEAR_FAILED("Bounding box of collinear points incorrect");
	

    private final String message;

    ErrorType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
