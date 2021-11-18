package persistence.orm;

import annotations.SaveFieldMMM;
import persistence.Dao;
import util.ConnectionUtility;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Evan Ritchey
 * @since 11|15|2021
 * essentially a more generic DAO implementation, that generates annotated fields as autonomously as possible
 */
public class SessionMMM implements Dao {

    private List<String> recordedTables;
    public SessionMMM(){
        recordedTables = new ArrayList<>();
    }

    /**
     * insert a given Object into the table
     * create a new table if the object hasn't been encountered before
     * @param o any given object whose fields we want to extract and store
     */
    @Override
    public void create(Object o) {
        //only keep fields marked for saving
        Field[] fields = o.getClass().getDeclaredFields();
        List<Field> annotatedFields;
        annotatedFields = Arrays.stream(fields)
                .filter(field -> field.isAnnotationPresent(SaveFieldMMM.class))
                .collect(Collectors.toList());

        //create the table if it doesn't exist
        if(!recordedTables.contains(o.getClass().getSimpleName())) {
            recordedTables.add(o.getClass().getSimpleName());//record the table
            createTable(o, annotatedFields);
        }
        //then create (sql insert) the object's annotated fields (record)
        insertRecord(o,annotatedFields);
    }

    /**
     * generate a table (if not exists)
     */
    private void createTable(Object o,List<Field> annotatedFields) {
        //start constructing the query
        StringBuilder sql_query = new StringBuilder("create table if not exists "+o.getClass().getSimpleName()+"(");

        for(Field field : annotatedFields){ //[var name] [postgres type] ,
            sql_query.append(field.getName()).append(" ");
            switch (field.getType().getTypeName()){//map data types, accepting only primitive types & Strings for now
                //TODO: do I eventually want to account for enums?
                //non-floating points
                case "byte":
                case "short":
                    sql_query.append("smallint");//2 bytes
                    break;
                //2 bytes
                case "int":
                    sql_query.append("integer");//4 bytes
                    break;
                case "long":
                    sql_query.append("bigint");//8 bytes
                    break;
                //floating points
                case "float":
                    sql_query.append("real");//4 bytes floating
                    break;
                case "double":
                    sql_query.append("double precision");//8 bytes floating
                    break;
                //non-numbers
                case "char":
                    sql_query.append("varchar(1)");//limits char len. to just 1
                    break;
                case "boolean":
                    sql_query.append("bool");
                    break;
                //String
                case "String":
                    sql_query.append("varchar(100)");//limits char len. to 100
                    break;
                default:
                    System.out.println("Type encountered cannot be accounted for in table mapping. you are screwed.");
                    break;
            }
            sql_query.append(",");
        }
        sql_query.deleteCharAt(sql_query.length()-1); //remove that last trailing comma

        sql_query.append(");");

        //actually make the query
        try(Connection connection = ConnectionUtility.getConnection()){
            assert connection != null;////make SURE we're actually operating on a valid connection
            PreparedStatement stmt = connection.prepareStatement(sql_query.toString());
            //TODO: stmt.setString ? refactor for question marks for prepared statements
            stmt.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * insert a record
     * @param o into the table corresponding to this object
     * @param annotatedFields defines the record contents
     */
    private void insertRecord(Object o, List<Field> annotatedFields) {
        //start constructing the query
        StringBuilder sql_query = new StringBuilder("insert into "+o.getClass().getSimpleName());
        StringBuilder columns = new StringBuilder("(");
        StringBuilder values = new StringBuilder("(");

        //build the query
        for(Field field : annotatedFields){
            columns.append(field.getName()).append(",");
            values.append("?,");
        }
        //remove trailing commas
        columns.deleteCharAt(columns.length()-1);
        values.deleteCharAt(values.length()-1);
        //"cap"
        columns.append(")");
        values.append(")");
        //concat.
        sql_query.append(columns).append(" values ").append(values);

        try(Connection connection = ConnectionUtility.getConnection()){
            assert connection != null;
            PreparedStatement stmt = connection.prepareStatement(sql_query.toString());
            int index = 1;//since 1 indexed
            for(Field field : annotatedFields){//prepare the queries
                field.setAccessible(true);//override any accessibility modifiers
                stmt.setObject(index++,field.get(o));//retrieve value of the field
            }
            stmt.executeUpdate();
        } catch (SQLException | IllegalAccessException throwables) {
            throwables.printStackTrace();
        }

    }

    /**
     * retrieve a database record base on
     * @param o defines the table and the specific fields
     * @return a record associated with the object o we're passing in
     */
    @Override
    public Object get(Object o) {
        //Start constructing the query
        StringBuilder sql_query = new StringBuilder("select * from "+o.getClass().getSimpleName()+"where ");
        //retrieve fields //TODO build fields
        //build query | column_name = ? AND ...
        //make query

        return null;
    }

    @Override
    public List getAll() {
        return null;
    }

    @Override
    public boolean update(Object o) {
        return false;
    }

    @Override
    public boolean delete(Object o) {
        return false;
    }

    // ========== Utility Methods ========== //
    private Field[] getAnnotatedFields(Object o){
        return null;
    }

}
