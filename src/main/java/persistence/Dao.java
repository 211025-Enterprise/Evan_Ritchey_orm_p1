package persistence;

import java.util.List;

/**
 * @author Evan Ritchey
 * @since 11|12|2021
 * Data Access Object Interface for CRUD (Create Read Update Delete) operations on the database
 */
public interface Dao<T> {

    // create
    void create(T t);

    // read
    T[] get(T t);

    // update
    boolean update(T t);

    //delete
    boolean delete(T t);
}