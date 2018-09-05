package Lib;

public interface Persistable {

    /**
     * Perform a save operation.
     * @return true if the save is successful.
     */
    boolean save();

    /**
     * Perform a load operation.
     * @return true if the load is successful.
     */
    boolean load();

}
