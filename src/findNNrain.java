import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
/*
 this java is used to find nearest rain measurement station and wind measurement station for each station in database.
 */
public class findNNrain
	{
		static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		static final String DB_URL = "jdbc:mysql://115.146.84.182:3306/project?characterEncoding=utf8&useSSL=true";
		static final String USER = "root";
		static final String PASS = "allenguan01";

		public static void main(String[] args)
			{
				// initialize database
				Connection con = null;
				PreparedStatement psql = null;
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
				counter[] rainset = new counter[188];
				counter[] windset=new counter[92];
				Station[] Stationset = new Station[17];

				// retrieve station info from database
				ResultSet Staset = null;
				try
					{
						int StationIndex = 0;
						Staset = consultation.executeQuery("SELECT * FROM project.stations;");
						while (Staset.next())
							{
								Stationset[StationIndex] = new Station();
								Stationset[StationIndex].setPointId(Staset.getInt("PointID"));
								Stationset[StationIndex].setLatitude(Staset.getDouble("Latitude"));
								Stationset[StationIndex].setLongitude(Staset.getDouble("Longitude"));
								StationIndex++;
							}
					}
				catch (SQLException e)
					{
						e.printStackTrace();
					}
				// retrieve rain and wind station info from databse
				ResultSet couset = null;
				try
					{
						int countIndex = 0;
						couset = consultation.executeQuery("SELECT * FROM project.rain_station;");
						while (couset.next())
							{
								rainset[countIndex] = new counter();
								rainset[countIndex].setId(couset.getInt("ID"));
								rainset[countIndex].setLatitude(couset.getDouble("Latitude"));
								rainset[countIndex].setLongitude(couset.getDouble("Longitude"));
								countIndex++;
							}
					}
				catch (SQLException e)
					{
						e.printStackTrace();
					}
				couset = null;
				try
					{
						int countIndex = 0;
						couset = consultation.executeQuery("SELECT * FROM project.wind_station;");
						while (couset.next())
							{
								windset[countIndex] = new counter();
								windset[countIndex].setId(couset.getInt("ID"));
								windset[countIndex].setLatitude(couset.getDouble("Latitude"));
								windset[countIndex].setLongitude(couset.getDouble("Longitude"));
								countIndex++;
							}
					}
				catch (SQLException e)
					{
						e.printStackTrace();
					}

				for (int i = 0; i < Stationset.length; i++)
					{
						double targetlat = Stationset[i].getLatitude();
						double targetlong = Stationset[i].getLongitude();
						double mindistance = 999;
						int minid = -1;
						for (int j = 0; j < rainset.length; j++)
							{
								double tempdis = Math.sqrt((Math.pow((rainset[j].getLatitude() - targetlat),2) +0.64*Math.pow((rainset[j].getLongitude() - targetlong),2)));
								if (tempdis < mindistance)
									{
										mindistance = tempdis;
										minid = rainset[j].getId();
									}
							}
						if (mindistance > 0.000001)
							{
								System.out.println(Stationset[i].getPointId() + " has distance " + mindistance);
							}
						String updatesql = "update stations set rain_station='" + minid + "' where PointID=" + Stationset[i].getPointId();
						System.out.println(updatesql);
						try
							{
								int result = update.executeUpdate(updatesql);
							}
						catch (SQLException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			
					}
				for (int i = 0; i < Stationset.length; i++)
					{
						double targetlat = Stationset[i].getLatitude();
						double targetlong = Stationset[i].getLongitude();
						double mindistance = 999;
						int minid = -1;
						for (int j = 0; j < windset.length; j++)
							{
								double tempdis = Math.sqrt((Math.pow((windset[j].getLatitude() - targetlat),2) +0.64*Math.pow((windset[j].getLongitude() - targetlong),2)));
								if (tempdis < mindistance)
									{
										mindistance = tempdis;
										minid = windset[j].getId();
									}
							}
						if (mindistance > 0.000001)
							{
								System.out.println(Stationset[i].getPointId() + " has distance " + mindistance);
							}
						String updatesql = "update stations set wind_station='" + minid + "' where PointID=" + Stationset[i].getPointId();
						System.out.println(updatesql);
						try
							{
								int result = update.executeUpdate(updatesql);
							}
						catch (SQLException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

			}

	}
	}
