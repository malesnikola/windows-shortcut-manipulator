package main.java.domain;

import com.sun.javaws.exceptions.InvalidArgumentException;
import main.java.enums.FileSizeUnit;

import java.io.File;

public class FileSize {
    private double size;
    private FileSizeUnit fileSizeUnit;

    private FileSize() {
        this(0, FileSizeUnit.KILOBYTE);
    }

    private FileSize(double size, FileSizeUnit fileSizeUnit) {
        this.size = size;
        this.fileSizeUnit = fileSizeUnit;
    }

    public static FileSize fromLong(long sizeInBytes) {
        double size;
        FileSizeUnit fileSizeUnit;
        if (sizeInBytes >= FileSizeUnit.TERABYTE_IN_BYTES) {
            size = ((double) sizeInBytes / FileSizeUnit.TERABYTE_IN_BYTES);
            fileSizeUnit = FileSizeUnit.TERABYTE;
        } else if (sizeInBytes >= FileSizeUnit.GIGABYTE_IN_BYTES) {
            size = ((double) sizeInBytes / FileSizeUnit.GIGABYTE_IN_BYTES);
            fileSizeUnit = FileSizeUnit.GIGABYTE;
        } else if (sizeInBytes >= FileSizeUnit.MEGABYTE_IN_BYTES) {
            size = ((double) sizeInBytes / FileSizeUnit.MEGABYTE_IN_BYTES);
            fileSizeUnit = FileSizeUnit.MEGABYTE;
        } else if (sizeInBytes >= FileSizeUnit.KILOBYTE_IN_BYTES) {
            size = ((double) sizeInBytes / FileSizeUnit.KILOBYTE_IN_BYTES);
            fileSizeUnit = FileSizeUnit.KILOBYTE;
        } else {
            size = sizeInBytes;
            fileSizeUnit = FileSizeUnit.BYTE;
        }

        return new FileSize(size, fileSizeUnit);
    }

    public static FileSize getFileSize(File file) {
        if (file.exists()) {
            return fromLong(file.length());
        } else {
            return new FileSize();
        }
    }

    public static FileSize getFreeDiskSpace(String path) throws IllegalAccessException {
        File file = new File(path);
        return getFreeDiskSpace(file);
    }

    public static FileSize getFreeDiskSpace(File file) throws IllegalAccessException {
        if (!file.exists()) {
            throw new IllegalAccessException("Bad format of path or disk no exist.");
        }

        return fromLong(file.getFreeSpace());
    }
}
