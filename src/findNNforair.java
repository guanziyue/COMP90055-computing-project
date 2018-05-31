import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
/*
 this java is used to find nearest air site for each station in database.
 */
public class findNNforair
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
				counter[] counterset = new counter[3801];
				air[] Stationset = new air[53931];

				// retrieve station info from database
				ResultSet Staset = null;
				try
					{
						int StationIndex = 0;
						Staset = consultation.executeQuery("SELECT * FROM project.air;");
						while (Staset.next())
							{
								Stationset[StationIndex] = new air();
								Stationset[StationIndex].setID(Staset.getInt("ID"));
								Stationset[StationIndex].setPointId(Staset.getInt("stationID"));
								Stationset[StationIndex].setTime(Staset.getString("time"));
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
						double mindistance = 9999999;
						int minid = -1;
						for (int j = 0; j < counterset.length; j++)
							{
								double tempdis = Math.sqrt((Math.pow((counterset[j].getLatitude() - targetlat),2) +0.64*Math.pow((counterset[j].getLongitude() - targetlong),2)));
								if (tempdis < mindistance)
									{
										mindistance = tempdis;
										minid = counterset[j].getId();
									}
							}
						
						String updatesql = "update project.air set nearscats='" + minid + "' where ID=" + Stationset[i].getID()+";";
						System.out.println(updatesql);
						try
							{
								update.executeUpdate(updatesql);
							}
						catch (SQLException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

					}

			}

	}

