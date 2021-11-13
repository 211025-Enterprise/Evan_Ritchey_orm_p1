package persistence;
/**
 * @author Evan Ritchey
 * @since 11|12|2021
 * Data Access Object Interface for CRUD (Create Read Update Delete) operations on the database
 */
public interface Dao<T> {

    // create
    void create(T t);

    // read
    T getById(long id);
    //List<T> getAll(); //can't use java collections for this project

    // update
    boolean update(T t);

    //delete
    boolean deleteById(long id);
}