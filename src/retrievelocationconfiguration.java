import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
/*
 this program get location of scats from arcgis with JSONObject according to id
 */
public class retrievelocationconfiguration
	{
		// query part information
		final static String queryhead = "https://services2.arcgis.com/18ajPSI0b3ppsmMt/arcgis/rest/services/Traffic_Count_Locations/FeatureServer/0/query?where=FID%3D";
		final static String querytail = "&outFields=*&outSR=4326&f=json";
		// SQL username, password and database name
		static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		static final String DB_URL = "jdbc:mysql://localhost:3306/project?characterEncoding=utf8&useSSL=true";
		static final String USER = "root";
		static final String PASS = "allenguan01";
		static int failnumber = 0;

		public static void main(String[] args)
			{
				// initialize database
				Connection con = null;
				Statement consultation = null;
				Statement update = null;
				try
					{
						// load driver
						Class.forName(JDBC_DRIVER);
						// establish connection with Database
						con = DriverManager.getConnection(DB_URL, USER, PASS);
						if (!con.isClosed())
							{
								System.out.println("Succeeded connecting to the Database!");
							}
						consultation = con.createStatement();
						update = con.createStatement();
					}
				catch (ClassNotFoundException e)
					{
						// Driver exception
						System.out.println("Driver Exception");
						e.printStackTrace();
					}
				catch (SQLException e)
					{
						// SQL exception
						System.out.println("SQL Exception");
						e.printStackTrace();
					}

				// get id from sever
				ResultSet IDsets = null;
				try
					{
						IDsets = consultation.executeQuery("SELECT * FROM project.scatslocation;");
						while (IDsets.next())
							{
								int queryid = IDsets.getInt("Scatsid");
								System.out.println(queryid);
								JSONObject aNewJSON = retrievelocation(queryid);
								if (aNewJSON != null)
									{
										parsing(aNewJSON, queryid, update);
									}

							}
					}
				catch (SQLException e)
					{
						System.out.println("Retrieve ID from database failed");
						e.printStackTrace();
					}
				System.out.println("Totally, fail:" + failnumber);
			}

		public static JSONObject retrievelocation(int FID)
			{
				URL targetUrl = null;
				JSONObject jsonstring = null;
				try
					{
						targetUrl = new URL(queryhead + FID + querytail);
						// System.out.println(targetUrl);
						HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
						connection.setConnectTimeout(10000);
						connection.setRequestMethod("GET");
						InputStream getxml = connection.getInputStream();
						BufferedReader reader = new BufferedReader(new InputStreamReader(getxml));
						String xmlString = reader.readLine();
						// System.out.println(xmlString);
						JSONParser parser = new JSONParser();
						jsonstring = (JSONObject) parser.parse(xmlString);
						// System.out.println(jsonstring);
					}
				catch (IOException IOe)
					{
						System.out.println("Retrieve Station error.Station ID" + FID);
						// IOe.printStackTrace();
					}
				catch (ParseException e)
					{
						System.out.println("JSON parsing error" + FID);
						e.printStackTrace();
					}
				return jsonstring;
			}

		public static void parsing(JSONObject JSONO, int FID, Statement update)
			{
				JSONArray features = (JSONArray) JSONO.get("features");
				JSONObject info = (JSONObject) features.get(0);
				JSONObject attributes = (JSONObject) info.get("attributes");
				JSONObject geo = (JSONObject) info.get("geometry");
				Double latitude = ((Number) geo.get("x")).doubleValue();
				Double longitude = ((Number) geo.get("y")).doubleValue();
				String description = (String) attributes.get("TFM_DESC");
				String sqlupdate = "update scatslocation set longitude='" + longitude + "',latitude=" + latitude + ",Description='" + description + "' where Scatsid=" + FID;
				// System.out.println(sqlupdate);
				try
					{
						int result = update.executeUpdate(sqlupdate);
						if (result == 0)
							{
								System.out.println("Update " + FID + " failed");
								failnumber++;
							}
					}
				catch (SQLException e)
					{
						System.out.println(e.getMessage());
						// System.out.println(e.getSQLState());
					}
			}

	}
