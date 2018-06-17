package main.java.enums;

public enum FileState {
    UNKNOWN ("unknown"),
    AVAILABLE ("available"),
    CASE_SENSITIVE ("case_sensitive"),
    UNAVAILABLE ("unavailable");

    private String stateText;

    FileState(String stateText) {
        this.stateText = stateText;
    }

    public String getStateText() {
        return stateText;
    }

    public static FileState fromString(String text) {
        for (FileState b : FileState.values()) {
            if (b.stateText.equalsIgnoreCase(text)) {
                return b;
            }
        }

        return null;
    }
}
