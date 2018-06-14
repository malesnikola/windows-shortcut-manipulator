package main.java.enums;

public enum ShortcutActionState {
    NONE ("none"),
    SAVED ("saved"),
    MODIFIED("modified"),
    FAILED_MODIFIED("failed modified"),
    FAILED_SAVED("failed saved");

    private String stateText;

    ShortcutActionState(String stateText) {
        this.stateText = stateText;
    }

    public String getStateText() {
        return stateText;
    }

    public static ShortcutActionState fromString(String text) {
        for (ShortcutActionState b : ShortcutActionState.values()) {
            if (b.stateText.equalsIgnoreCase(text)) {
                return b;
            }
        }

        return null;
    }
}
