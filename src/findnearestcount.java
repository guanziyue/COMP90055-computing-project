import java.sql.*;
/*
 this java is used to find nearest scats site for each station in database.
 */
public class findnearestcount
	{
		static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		static final String DB_URL = "jdbc:mysql://115.146.84.182:3306/project?characterEncoding=utf8&useSSL=true";
		static final String USER = "root";
		static final String PASS = "**********";
		public static double distance(double lat1, double lat2, double lon1,
			        double lon2, double el1, double el2) {

			    final int R = 6371; // Radius of the earth

			    double latDistance = Math.toRadians(lat2 - lat1);
			    double lonDistance = Math.toRadians(lon2 - lon1);
			    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
			            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
			            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
			    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
			    double distance = R * c * 1000; // convert to meters

			    double height = el1 - el2;

			    distance = Math.pow(distance, 2) + Math.pow(height, 2);

			    return Math.sqrt(distance);
			}
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
				counter[] counterset = new counter[3801];
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
				// retrieve count info from databse
				ResultSet couset = null;
				try
					{
						int countIndex = 0;
						couset = consultation.executeQuery("SELECT * FROM project.scatslocation;");
						while (couset.next())
							{
								counterset[countIndex] = new counter();
								counterset[countIndex].setId(couset.getInt("Scatsid"));

								counterset[countIndex].setLatitude(couset.getDouble("latitude"));
								counterset[countIndex].setLongitude(couset.getDouble("longitude"));
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
						double mindistance = 999999;
						int minid = -1;
						for (int j = 0; j < counterset.length; j++)
							{
								double tempdis = distance(counterset[j].getLatitude(),targetlat,counterset[j].getLongitude(),targetlong,0,0);
								if (tempdis < mindistance)
									{
										mindistance = tempdis;
										minid = counterset[j].getId();
									}
							}
						if (mindistance > 1000)
							{
								System.out.println(Stationset[i].getPointId() + " has distance " + mindistance);
							}
						String updatesql = "update stations set Street='" + minid + "' where PointID=" + Stationset[i].getPointId();
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
