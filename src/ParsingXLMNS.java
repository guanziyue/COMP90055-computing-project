import java.sql.*;
import java.io.*;
import java.util.*;
import java.net.*;
import org.json.simple.*;
import org.json.simple.parser.*;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
/*
 this java is used to parse JSONObject from EPA to get air pollution data for each day.
 */
public class ParsingXLMNS
	{

		final static String host = "http://sciwebsvc.epa.vic.gov.au/aqapi/";
		final static String StationData = "Measurements?siteId=";
		final static String MeasureData = "Measurements?siteId=";
		final static String MonitorOption = "&monitorId=BPM2.5&timebasisid=1HR_AV";
		final static String TimePeriod = "&fromDate=2017123111&toDate=2018022812";
		static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		static final String DB_URL = "jdbc:mysql://115.146.84.182:3306/project?characterEncoding=utf8&useSSL=true";
		static final String USER = "root";
		static final String PASS = "allenguan01";

		public static void main(String[] args)
			{
				// : establish connection to database
				Connection con = null;
				PreparedStatement psql = null;
				Statement consultation = null;
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
						psql = con.prepareStatement("insert into air (time,stationID,Longitude,Latitude,value) " + "values(?,?,?,?,?)");
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
				catch (Exception e)
					{
						e.printStackTrace();
					}
				// get stationid from database

				ResultSet IDsets = null;
				try
					{
						IDsets = consultation.executeQuery("SELECT * FROM project.stations;");
						/*
						 * 4System.out.println(IDsets); while(IDsets.next()){
						 * System.out.println(IDsets.getInt("PointID")); }
						 * System.exit(0);
						 */
						while (IDsets.next())
							{
								int queryid = IDsets.getInt("PointID");
								System.out.println(queryid);
								JSONObject aNewJSON = retrieveStation(queryid);
								if (aNewJSON != null)
									{
										parsing(aNewJSON, queryid, psql);
									}

							}
					}
				catch (SQLException e)
					{
						System.out.println("Retrieve ID from database failed");
						e.printStackTrace();
					}
			}

		// : retrieve xml document from particular url
		public static JSONObject retrieveStation(int StationID)
			{
				URL targetUrl = null;
				JSONObject jsonstring = null;
				try
					{
						targetUrl = new URL(host + MeasureData + (Integer) StationID + MonitorOption + TimePeriod);
						// System.out.println(targetUrl);
						HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
						connection.setConnectTimeout(100000);
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
						System.out.println("Retrieve Station error.Station ID" + StationID);
						// IOe.printStackTrace();
					}
				catch (ParseException e)
					{
						System.out.println("JSON parsing error" + StationID);
						e.printStackTrace();
					}
				return jsonstring;
			}

		// : parsing xml document use Iterators
		public static void parsing(JSONObject jsonO, int StationID, PreparedStatement psql) throws SQLException
			{
				JSONArray measurements = (JSONArray) jsonO.get("Measurements");
				System.out.println(measurements);
				Iterator<Object> itOfMeasurements = measurements.iterator();
				while (itOfMeasurements.hasNext())
					{
						try
							{
								JSONObject thismeasurement = (JSONObject) itOfMeasurements.next();
								// parse the time stamp. the format is
								// YYYY-MM-DD
								// HH-MM-SS note that the webservice return
								// string
								// like"2016-01-01T01:00:00"
								String timestring = (String) thismeasurement.get("DateTimeStart");
								timestring = timestring.replaceAll("T", " ");
								if(timestring.equals("2017-12-31 12:00:00"))
									{timestring="2018-01-01 00:00:00";}
								java.sql.Timestamp time = new java.sql.Timestamp(System.currentTimeMillis());
								time = Timestamp.valueOf(timestring);
								psql.setTimestamp(1, time);
								// parse Station id
								psql.setInt(2, StationID);
								// parse position
								psql.setDouble(3, ((Number) thismeasurement.get("Latitude")).doubleValue());
								psql.setDouble(4, ((Number) thismeasurement.get("Longitude")).doubleValue());
								// parse value
								psql.setDouble(5, Double.parseDouble((String) thismeasurement.get("Value")));
								System.out.println(psql);
								psql.executeUpdate();
							}
						catch (MySQLIntegrityConstraintViolationException e)
							{
								System.out.println(e.getMessage());
							}
					}

			}
	}
