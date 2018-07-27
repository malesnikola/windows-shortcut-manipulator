package main.java.enums;

import javafx.scene.paint.Color;

/**
 * State of file:
 * UNKNOWN = targeting (original) file is unknown (not yet searched)
 * AVAILABLE = targeting (original) file is available
 * CASE_SENSITIVE = targeting (original) file is available, but have different letter case for some letters
 * UNAVAILABLE = targeting (original) file is unavailable
 */
public enum FileState {
    UNKNOWN ("unknown") {
        @Override
        public Color getColor() {
            return Color.BLACK;
        }
    },
    AVAILABLE ("available") {
        @Override
        public Color getColor() {
            return Color.GREEN;
        }
    },
    CASE_SENSITIVE ("case_sensitive") {
        @Override
        public Color getColor() {
            return Color.BLUE;
        }
    },
    UNAVAILABLE ("unavailable") {
        @Override
        public Color getColor() {
            return Color.RED;
        }
    };

    private String stateText;

    FileState(String stateText) {
        this.stateText = stateText;
    }

    public String getStateText() {
        return stateText;
    }

    /**
     * Get color for representing text for state.
     * @return JavaFX Color.
     */
    public abstract Color getColor();

    public static FileState fromString(String text) {
        for (FileState b : FileState.values()) {
            if (b.stateText.equalsIgnoreCase(text)) {
                return b;
            }
        }

        return null;
    }
}
