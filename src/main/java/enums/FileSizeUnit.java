package main.java.enums;

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

    public static final long KILOBYTE_IN_BYTES = 1024;
    public static final long MEGABYTE_IN_BYTES = 1024 * KILOBYTE_IN_BYTES;
    public static final long GIGABYTE_IN_BYTES = 1024 * MEGABYTE_IN_BYTES;
    public static final long TERABYTE_IN_BYTES = 1024 * GIGABYTE_IN_BYTES;
}
