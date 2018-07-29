package main.java.workers;

/**
 * This is contract for all workers which have some waiting form.
 */
public interface Progress {
    /**
     * Set total size of task.
     * For example: If task is importing files, total sie of task should be number of files for importing.
     * @param size Total size of task.
     */
    void setTotalSizeOfTask(long size);

    /**
     * Update progress form by one.
     * This method should be called when one of total size of task is finished.
     * For example: If task is importing files, call this method after each file is imported and in this method update progress form by one.
     */
    void updateProgress();
}
