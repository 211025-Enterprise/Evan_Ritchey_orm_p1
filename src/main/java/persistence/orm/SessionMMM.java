package persistence.orm;

import annotations.SaveFieldMMM;
import persistence.Dao;
import util.ConnectionUtility;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Evan Ritchey
 * @since 11|15|2021
 *
 */
public class SessionMMM implements Dao {

    public SessionMMM(){}

    /**
     * insert a given Object into the table
     * create a new table if the object hasn't been encountered before
     * @param o any given object whose fields we want to extract and store
     */
    @Override
    public void create(Object o) {
        //horribly inefficient, but it works:
        create(o.getClass());//create the table if it doesn't exist


    }

    /**
     * generate a table (if not exists)
     * @param clazz
     */
    private void createTable(Class clazz) {
        Field fields[] = clazz.getFields();

        //start constructing the query
        StringBuilder sql_query = new StringBuilder("create table if not exists "+clazz.getSimpleName()+"(");

        List<Field> annotatedFields;
        //only keep fields marked as such
        annotatedFields = Arrays.stream(fields)
                .filter(field -> field.isAnnotationPresent(SaveFieldMMM.class))
                .collect(Collectors.toList());

        for(Field field : annotatedFields){ //[var name] [postgres type] ,
            sql_query.append(field.getName()).append(" ");
            switch (field.getType().getTypeName()){//assuming only primitive types & Strings for now
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
            //TODO: stmt.set | question marks and all that
            stmt.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public Object get(Object o) {
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

}
