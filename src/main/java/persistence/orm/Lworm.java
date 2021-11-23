package persistence.orm;

import annotations.SaveFieldMMM;
import persistence.Dao;
import util.ConnectionUtility;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//TODO: ideally refactor and fix all methods to have prepared stmts instead

/**
 * @author Evan Ritchey
 * @since 11|15|2021
 * essentially a more generic DAO implementation, that generates annotated fields as autonomously as possible
 */
public class Lworm implements Dao {

    private List<String> recordedTables;//TODO replace with a .txt cache instead
    public Lworm(){
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
        List<Field> annotatedFields = getAnnotatedFields(o);
        //create the table if it doesn't exist
        if(!recordedTables.contains(o.getClass().getSimpleName())) {
            recordedTables.add(o.getClass().getSimpleName());//record the table
            createTable(o,annotatedFields);
        }
        //then create (sql insert) the object's annotated fields (record)
        insertRecord(o,annotatedFields);
    }

    /**
     * generate a table (if not exists)
     */
    private void createTable(Object o,List<Field> annotatedFields) {
        //start constructing the query
        StringBuilder sql_query = new StringBuilder("create table if not exists \""+o.getClass().getSimpleName()+"\"(");

        for(Field field : annotatedFields){ //[var name] [postgres type] ,
            sql_query.append("\"").append(field.getName()).append("\" ");
            switch (field.getType().getTypeName()){//map data types, accepting only primitive types & Strings for now
                //TODO: do I eventually want to account for enums?
                //non-floating points
                case "byte":
                case "short":
                    sql_query.append("smallint");//2 bytes
                    break;
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
                case "java.lang.String": //full path since it's not a primitive
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
//            System.out.println(sql_query);
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
        StringBuilder sql_query = new StringBuilder("insert into \""+o.getClass().getSimpleName()+"\"");
        StringBuilder columns = new StringBuilder("(");
        StringBuilder values = new StringBuilder("(");

        //build the query
        for(Field field : annotatedFields){
            columns.append("\"").append(field.getName()).append("\"").append(",");
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
//            System.out.println(sql_query);
            stmt.executeUpdate();
        } catch (SQLException | IllegalAccessException throwables) {
            throwables.printStackTrace();
        }

    }

    /**
     * retrieve a database record based on the following
     * @param o the object whose associated table we want to access
     * @param fvp the values we want to constrain the query fields to (only primitives and Strings)
     * @return a record associated with the object o we're passing in
     */
    public Object[] get(Object o, FieldValuePair[] fvp) {
        //Start constructing the query
        StringBuilder sql_query = new StringBuilder("select * from \""+o.getClass().getSimpleName()+"\" where ");
        //build query
        for(FieldValuePair f : fvp){
            //column_name = value
            sql_query.append(String.format("\"%s\"",f.getField())).append(" = ");
            if(f.getValue().getClass() == String.class || f.getValue().getClass() == Character.class) //account for String formatting
                sql_query.append("\'").append(f.getValue()).append("\'");
            else
                sql_query.append(f.getValue());

            sql_query.append(" AND ");//account for further constraints
        }
        sql_query.delete(sql_query.length()-5,sql_query.length()-1); //remove the last AND

//        System.out.println(sql_query);
        //make query
        List<Object> records = new ArrayList<>();
        try(Connection connection = ConnectionUtility.getConnection()){
            assert connection != null;////make SURE we're actually operating on a valid connection
            PreparedStatement stmt = connection.prepareStatement(sql_query.toString());
            ResultSet rs = stmt.executeQuery();

            while(rs.next()){ //for every row retrieved
                Object newObject = ClassCreator.getInstance(o.getClass());
                if(newObject == null)
                    throw new ExceptionInInitializerError("Class "+o.getClass().getSimpleName()+" is missing a zero args constructor.");

                List<Field> annotatedFields = getAnnotatedFields(o);
                int i = 1;
                for(Field field:annotatedFields){//put our retrieved values into a new object instance
                    field.setAccessible(true);//override any private accessors
                    //LYNCH PIN:
                    Field f = newObject.getClass().getDeclaredField(field.getName());
                    f.setAccessible(true);
                    f.set(newObject,rs.getObject(i++));
                }

                records.add(newObject);
            }
        } catch (SQLException
                | ExceptionInInitializerError
                | IllegalAccessException
                | InstantiationException
                | InvocationTargetException
                | NoSuchFieldException throwables) {
            throwables.printStackTrace();
        }

        return records.toArray();
    }

    /**
     * just get the whole table associated with Object o, without constraining the query to any values
     * @param o describe the table we want to pull from
     * @return a list of objects of the same type as o, containing all the entries from the associated table
     */
    @Override
    public Object[] get(Object o){

        return null;
    }

    /**
     * Pass in an object that defines the corresponding table you want updated with the FieldValuePair
     * @param o object that defines the corresponding table you want updated
     * @param fvpValues the fields of Object o you want to update
     * @param fvpConstraints any constraints we want to define
     */
    public boolean update(Object o,FieldValuePair[] fvpValues,FieldValuePair[] fvpConstraints) {//TODO refactor use the values in o instead as the replacement values
        //Start constructing the query
        StringBuilder sql_query = new StringBuilder("update \""+o.getClass().getSimpleName()+"\" set ");
        //build query
        //SET
        StringBuilder value_query = new StringBuilder();
        for(int i = 0; i < fvpValues.length; i++){
            value_query.append(String.format("\"%s\"",fvpValues[i].getField())).append(" = ");
            if(fvpValues[i].getValue().getClass() == String.class || fvpValues[i].getValue().getClass() == Character.class) //account for String formatting
                value_query.append("\'").append(fvpValues[i].getValue()).append("\'");
            else
                value_query.append(fvpValues[i].getValue());
            value_query.append(",");
        }
        value_query.deleteCharAt(value_query.length()-1);//remove that last comma

        //WHERE
        StringBuilder constraint_query = new StringBuilder(" where ");
        for(int i = 0; i < fvpConstraints.length; i++){
            constraint_query.append(String.format("\"%s\"",fvpConstraints[i].getField())).append(" = ");
            if(fvpConstraints[i].getValue().getClass() == String.class || fvpConstraints[i].getValue().getClass() == Character.class) //account for String formatting
                constraint_query.append("\'").append(fvpConstraints[i].getValue()).append("\'");
            else
                constraint_query.append(fvpConstraints[i].getValue());
            constraint_query.append(" AND ");
        }
        constraint_query.delete(constraint_query.length()-5,constraint_query.length()-1);//remove that last and

        sql_query.append(value_query).append(constraint_query);
//        System.out.println(sql_query);
        try(Connection connection = ConnectionUtility.getConnection()){
            assert connection != null;
            PreparedStatement stmt = connection.prepareStatement(sql_query.toString());
            stmt.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false; //update failed
        }

        return true; //update passed an succeeded
    }

    @Override
    public boolean update(Object o){
        return false;
    }

    /**
     * delete records in our table defined in our object o
     * @param o contains table name and constraint values
     * @return true if the delete was successful
     */
    @Override
    public boolean delete(Object o) {
        //Start constructing the query
        StringBuilder sql_query = new StringBuilder("delete from \""+o.getClass().getSimpleName()+"\" where");

        return false;
    }

    public boolean delete(Object o, FieldValuePair[] constraints){
        //Start constructing the query
        StringBuilder sql_query = new StringBuilder("delete from \""+o.getClass().getSimpleName()+"\" where ");
        for (FieldValuePair constraint : constraints) {
            sql_query.append(String.format("\"%s\"", constraint.getField())).append("=");
            if (constraint.getValue().getClass() == String.class || constraint.getValue().getClass() == Character.class) //account for String formatting
                sql_query.append("\'").append(constraint.getValue()).append("\'");
            else
                sql_query.append(constraint.getValue());
            sql_query.append(" AND ");
        }
        sql_query.delete(sql_query.length()-5,sql_query.length()-1);//remove that last and

//        System.out.println(sql_query);
        try(Connection connection = ConnectionUtility.getConnection()){
            assert connection != null;
            PreparedStatement stmt = connection.prepareStatement(sql_query.toString());
            stmt.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false; //update failed
        }

        return true;
    }

    // ========== Utility Methods ========== //

    private List<Field> getAnnotatedFields(Object o){
        Field[] fields = o.getClass().getDeclaredFields();
        List<Field> annotatedFields;
        annotatedFields = Arrays.stream(fields)
                .filter(field -> field.isAnnotationPresent(SaveFieldMMM.class))
                .collect(Collectors.toList());

        return annotatedFields;
    }

    // ========== Utility Classes ========== //

    //TODO refactor; replace w/ https://www.javatuples.org/
    //or I could just replace with a layer of abstraction and build a utility class of var args which will process this automatically
    /**
     * a primitive tuple implementation. a bit clunky, but usable
     * e.g. {new FieldValuePair("fieldName1", fieldValue1),new FieldValuePair("fieldNameN", fieldValueN),...}
     */
    public static class FieldValuePair{
        private final String field;
        private final Object value;

        public FieldValuePair(String field, Object value){
            this.field = field;
            this.value = value;
        }

        public String getField() {
            return field;
        }

        public Object getValue() {
            return value;
        }
    }

    /**
     * @author bpinkerton
     * invoke a new instance of a class to retrieve an object
     */
    private static class ClassCreator{

        public static Object getInstance(Class<?> clazz, Object... args) throws InvocationTargetException, InstantiationException, IllegalAccessException {
            Constructor<?> noArgsConstructor = null;

            //TODO generify, so that if args are past in, it will return a newInstance using the matching constructor

            //retrieve the constructor with 0 args
            noArgsConstructor = Arrays.stream(clazz.getDeclaredConstructors())
                    .filter(c->c.getParameterCount() == 0)
                    .findFirst()
                    .orElse(null);

            if(noArgsConstructor != null) {
                noArgsConstructor.setAccessible(true);
                return noArgsConstructor.newInstance();
            }

            return null;
        }
    }

}
