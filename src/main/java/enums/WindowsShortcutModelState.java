package main.java.enums;

/**
 * State of last action in Mp3Model.
 * NONE: Program is started.
 * IMPORTED: Files are imported.
 * REMOVED: Files are removed.
 * GENERATED: Tags for files are generated.
 * SAVED: Files are saved on disc.
 */
public enum WindowsShortcutModelState {
    NONE,
    IMPORTED,
    REMOVED,
    CHECKED_AVAILABILITY,
    CHECKED_DUPLICATES,
    CHANGED_ROOTS,
    CREATED_COPIES
}
