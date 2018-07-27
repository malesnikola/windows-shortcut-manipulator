package main.java.enums;

/**
 * This enum contains all possible units of file size:
 */
public enum FileSizeUnit {
    BYTE ("byte"),
    KILOBYTE ("KB"),
    MEGABYTE ("MB"),
    GIGABYTE ("GB"),
    TERABYTE ("TB");

    private String unitText;

    FileSizeUnit(String unitText) {
        this.unitText = unitText;
    }

    public String getUnitText() {
        return unitText;
    }

    @Override
    public String toString() {
        return unitText;
    }

    public static final long ONE_KILOBYTE_IN_BYTES = 1024;
    public static final long ONE_MEGABYTE_IN_BYTES = 1024 * ONE_KILOBYTE_IN_BYTES;
    public static final long ONE_GIGABYTE_IN_BYTES = 1024 * ONE_MEGABYTE_IN_BYTES;
    public static final long ONE_TERABYTE_IN_BYTES = 1024 * ONE_GIGABYTE_IN_BYTES;
}
