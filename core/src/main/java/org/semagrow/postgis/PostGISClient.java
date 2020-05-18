package org.semagrow.postgis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PostGISClient {

	private final Logger logger = LoggerFactory.getLogger(PostGISClient.class);
    
    private String url;
    private String user;
    private String password;

    private Connection database;
    private Statement statement;

    private static PostGISClient instance = null;

    private PostGISClient() {}

    public static PostGISClient getInstance(String url, String user, String password) {
        if (instance == null) {
            instance = new PostGISClient();
            instance.setCredentials(url, user, password);
            instance.connect();
        }
        return instance;
    }
    
    private void setCredentials(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private void connect() {
    	try {
			Class.forName("org.postgresql.Driver");
			database = DriverManager.getConnection(url, user, password);
			statement = database.createStatement();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    private void close() {
//        session.close();
//        cluster.close();
        try {
        	statement.close();
			database.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public ResultSet execute(String query) {
        logger.info("Sending query: {}", query);
        ResultSet results = null;
		try {
			results = statement.executeQuery(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return results;
    }
}