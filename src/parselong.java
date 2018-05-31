import java.sql.*;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.*;
import java.util.Date;
import java.net.*;
import org.json.simple.*;
import org.json.simple.parser.*;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
/*
 * this class is used to generate sequence PM2.5 value.
 */
public class parselong
	{
		final static String host = "http://sciwebsvc.epa.vic.gov.au/aqapi/";
		final static String MeasureData = "Measurements?siteId=";
		final static String MonitorOption = "&monitorId=BPM2.5&timebasisid=1HR_AV";
		final static String TimePeriod = "&fromDate=2015123123&toDate=2017010100";
		static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		static final String DB_URL = "jdbc:mysql://115.146.84.182:3306/LSTM?characterEncoding=utf8&useSSL=true";
		static final String USER = "root";
		static final String PASS = "**********";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		static final int stationID=10239;
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
						psql = con.prepareStatement("insert into `LSTM`.`"+(Integer) stationID+"` (`time`,`stationID`,`0`,`1`,`2`,`3`,`4`,`5`,`6`,`7`,`8`,`9`,`10`,`11`,`12`,`13`,`14`,`15`,`16`,`17`,`18`,`19`,`20`,`21`,`22`,`23`) " + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
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
				JSONObject aNewJSON = retrieveStation(stationID);
				if (aNewJSON != null)
					{
						try
							{
								parsing(aNewJSON, stationID, psql);
							}
						catch (SQLException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
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
						connection.setConnectTimeout(600000000);
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
						IOe.printStackTrace();
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
				System.out.println("JSON string get");
				Iterator<Object> itOfMeasurements = measurements.iterator();
				station_one_day[] one_station = new station_one_day[366];
				while (itOfMeasurements.hasNext())
					{
						System.out.println("parsing begin");
						JSONObject thismeasurement = (JSONObject) itOfMeasurements.next();
						// parse the time stamp. the format is
						// YYYY-MM-DD
						// HH-MM-SS note that the webservice return
						// string
						// like"2016-01-01T01:00:00"
						String timestring = (String) thismeasurement.get("DateTimeStart");
						timestring = timestring.replaceAll("T", " ");
						java.sql.Timestamp time = new java.sql.Timestamp(System.currentTimeMillis());
						time = Timestamp.valueOf(timestring);
						Calendar date = Calendar.getInstance();
						date.setTime(time);
						Date thisdate = date.getTime();
						int hour = date.get(date.HOUR_OF_DAY);
						int daynum = date.get(date.DAY_OF_YEAR);
						if (one_station[daynum - 1]==null)
							{
								one_station[daynum - 1] = new station_one_day();
								double[] values = new double[24];
								values[hour] = Double.parseDouble((String) thismeasurement.get("Value"));
								one_station[daynum - 1].setDate(thisdate);
								one_station[daynum - 1].setPointId(StationID);
								one_station[daynum - 1].setValues(values);
								int newcount=one_station[daynum - 1].getCount()+1;
								one_station[daynum - 1].setCount(newcount);
							}
						else
							{
								double[] values = one_station[daynum - 1].getValues();
								values[hour] = Double.parseDouble((String) thismeasurement.get("Value"));
								one_station[daynum - 1].setValues(values);
								int newcount=one_station[daynum - 1].getCount()+1;
								one_station[daynum - 1].setCount(newcount);
							}
						// if (one_station.getDate() == null)
						// {
						// double[] values = new double[24];
						// values[hour] = Double.parseDouble((String)
						// thismeasurement.get("Value"));
						// one_station.setDate(thisdate);
						// one_station.setPointId(StationID);
						// one_station.setValues(values);
						// }
						// else if (!one_station.getDate().equals(thisdate))
						// {
						// java.sql.Date sql_date = new
						// java.sql.Date(one_station.getDate().getTime());
						// psql.setDate(1, sql_date);
						// psql.setInt(2, StationID);
						// double[] seqvalues = one_station.getValues();
						// for (int i = 3; i < 27; i++)
						// {
						// psql.setDouble(i, seqvalues[i - 3]);
						// }
						// psql.executeUpdate();
						// System.out.println(psql);
						// System.out.print(sql_date + "updated");
						// one_station = new station_one_day();
						// }
						// else if (one_station.getDate().equals(thisdate))
						// {
						// double[] values = one_station.getValues();
						// values[hour] = Double.parseDouble((String)
						// thismeasurement.get("Value"));
						// one_station.setValues(values);
						// }
					}
		System.out.println("begin input");		
		for (int i = 0; i < one_station.length; i++)
			{
				if (one_station[i]!=null&&one_station[i].getCount()==24) {
				try {
				java.sql.Date sql_date = new java.sql.Date(one_station[i].getDate().getTime());
				psql.setDate(1, sql_date);
				psql.setInt(2, StationID);
				double[] seqvalues = one_station[i].getValues();
				for (int k = 3; k < 27; k++)
					{
						psql.setDouble(k, seqvalues[k - 3]);
					}
				System.out.println(psql);
				psql.executeUpdate();
				}catch(NullPointerException e)
					{
						System.out.println(one_station[i].getDate());
						System.out.println(one_station[i].getValues());
						System.exit(0);
					}}
			}
	}}
