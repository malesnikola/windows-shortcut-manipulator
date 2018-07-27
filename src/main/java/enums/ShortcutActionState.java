package main.java.enums;

import javafx.scene.paint.Color;

/**
 * Shortcut action state is the last user action on shortcut file:
 * NONE = no action at all
 * SAVED = user successfully saved copies of original file
 * MODIFIED = user successfully change parents (path) of shortcut file
 * FAILED_MODIFIED = user failed change parents (path) of shortcut file
 * FAILED_SAVED = user failed saved copies of original file
 */
public enum ShortcutActionState {
    NONE ("none") {
        @Override
        public Color getColor() {
            return Color.BLACK;
        }
    },
    SAVED ("saved") {
        @Override
        public Color getColor() {
            return Color.GREEN;
        }
    },
    MODIFIED("modified") {
        @Override
        public Color getColor() {
            return Color.BLUE;
        }
    },
    FAILED_MODIFIED("failed_modified") {
        @Override
        public Color getColor() {
            return Color.RED;
        }
    },
    FAILED_SAVED("failed_saved") {
        @Override
        public Color getColor() {
            return Color.RED;
        }
    };

    private String stateText;

    ShortcutActionState(String stateText) {
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

    public static ShortcutActionState fromString(String text) {
        for (ShortcutActionState b : ShortcutActionState.values()) {
            if (b.stateText.equalsIgnoreCase(text)) {
                return b;
            }
        }

        return null;
    }
}
