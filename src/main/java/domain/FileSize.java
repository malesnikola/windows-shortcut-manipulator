package main.java.domain;

import main.java.enums.FileSizeUnit;

import java.io.File;

/**
 * Class contains two field which represent file size.
 * I.e. File with size of 7.5 MB is represented with: size = 7.5 and fileSizeUnit = MEGABYTE
 */
public class FileSize {
    private long sizeInBytes;
    private double size;                // size in converted unit
    private FileSizeUnit fileSizeUnit;  // unit (i.e. MB)

    private FileSize() {
        this(0, 0, FileSizeUnit.KILOBYTE);
    }

    private FileSize(long sizeInBytes, double size, FileSizeUnit fileSizeUnit) {
        this.sizeInBytes = sizeInBytes;
        this.size = size;
        this.fileSizeUnit = fileSizeUnit;
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public double getSize() {
        return size;
    }

    public FileSizeUnit getFileSizeUnit() {
        return fileSizeUnit;
    }

    @Override
    public String toString() {
        String value;
        // show two decimals for terabytes, one decimal for gigabytes and no decimals for others
        if (fileSizeUnit == FileSizeUnit.TERABYTE) {
            value = String.format("%1$,.2f", size);
        } else if (fileSizeUnit == FileSizeUnit.GIGABYTE) {
            value = String.format("%1$,.1f", size);
        } else {
            value = (int) size + "";
        }

        return value + " " + fileSizeUnit.toString();
    }

    /**
     * Create new FileSize object from size in bytes.
     * @param sizeInBytes Represents size in bytes.
     * @return Returns new FileSize object.
     */
    public static FileSize fromLong(long sizeInBytes) {
        double size;
        FileSizeUnit fileSizeUnit;
        if (sizeInBytes >= FileSizeUnit.ONE_TERABYTE_IN_BYTES) {
            size = ((double) sizeInBytes / FileSizeUnit.ONE_TERABYTE_IN_BYTES);
            fileSizeUnit = FileSizeUnit.TERABYTE;
        } else if (sizeInBytes >= FileSizeUnit.ONE_GIGABYTE_IN_BYTES) {
            size = ((double) sizeInBytes / FileSizeUnit.ONE_GIGABYTE_IN_BYTES);
            fileSizeUnit = FileSizeUnit.GIGABYTE;
        } else if (sizeInBytes >= FileSizeUnit.ONE_MEGABYTE_IN_BYTES) {
            size = ((double) sizeInBytes / FileSizeUnit.ONE_MEGABYTE_IN_BYTES);
            fileSizeUnit = FileSizeUnit.MEGABYTE;
        } else if (sizeInBytes >= FileSizeUnit.ONE_KILOBYTE_IN_BYTES) {
            size = ((double) sizeInBytes / FileSizeUnit.ONE_KILOBYTE_IN_BYTES);
            fileSizeUnit = FileSizeUnit.KILOBYTE;
        } else {
            size = sizeInBytes;
            fileSizeUnit = FileSizeUnit.BYTE;
        }

        return new FileSize(sizeInBytes, size, fileSizeUnit);
    }

    /**
     * Create new FileSize object from file.
     * @param file File.
     * @return Returns new FileSize object.
     */
    public static FileSize getFileSize(File file) {
        if (file.exists()) {
            return fromLong(file.length());
        } else {
            return new FileSize();
        }
    }

    /**
     * Create new FileSize object with information of free space size on disk where is file with forwarded path.
     * @param path Path of file.
     * @return Returns new FileSize object.
     * @throws IllegalAccessException If path is not valid.
     */
    public static FileSize getFreeDiskSpace(String path) throws IllegalAccessException {
        File file = new File(path);
        return getFreeDiskSpace(file);
    }

    /**
     * Create new FileSize object with information of free space size on disk where is forwarded file.
     * @param file File.
     * @return Returns new FileSize object.
     * @throws IllegalAccessException If file doesn't exist.
     */
    public static FileSize getFreeDiskSpace(File file) throws IllegalAccessException {
        if (!file.exists()) {
            throw new IllegalAccessException("Bad format of path or disk no exist.");
        }

        return fromLong(file.getFreeSpace());
    }
}
