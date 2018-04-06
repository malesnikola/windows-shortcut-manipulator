package main.java.domain;

/**
 * Contains information about file and error.
 */
public class FailedFileDetails {
    private String filePath;
    private String errorMessage;

    /**
     * Create new FailedFileDetails.
     * @param filePath Full path of the file.
     * @param errorMessage Error message related to the file.
     */
    public FailedFileDetails(String filePath, String errorMessage) {
        this.filePath = filePath;
        this.errorMessage = errorMessage;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
