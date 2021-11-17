package util;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionUtility {

    private static Properties properties;
    private static final String propertiesPath = "src/main/resources/application.properties";


    private static void loadProperties(){
        properties = new Properties();

        try {
            InputStream stream = new FileInputStream(new File(propertiesPath).getAbsolutePath());
            properties.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection(){

        if(properties == null){
            loadProperties();
        }

        try{
            Class.forName("org.postgresql.Driver");

            Connection connection = DriverManager.getConnection(
                    properties.getProperty("url"),
                    properties.getProperty("username"),
                    properties.getProperty("password")
            );

            return connection;
        } catch (SQLException | ClassNotFoundException e  ){
            System.out.println(e.getMessage());
        }
        return null;
    }

}
