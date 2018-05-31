import java.io.*;
import java.sql.*;

/*
 this program is to read traffic volume from csv and store in database
 */
public class finaldata
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
				PrintWriter outputvalue = null;

				try
					{
						outputvalue = new PrintWriter(new FileOutputStream("airvalue.txt"));
					}
				catch (FileNotFoundException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

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
				air[] Stationset = new air[53931];

				// retrieve station info from database
				ResultSet Staset = null;
				try
					{
						int StationIndex = 0;
						Staset = consultation.executeQuery("SELECT *,hour(time) as HH,minute(time) as MM FROM project.air");
						while (Staset.next())
							{
								Stationset[StationIndex] = new air();
								Stationset[StationIndex].setID(Staset.getInt("ID"));
								Stationset[StationIndex].setPointId(Staset.getInt("stationID"));
								Stationset[StationIndex].setTime(Staset.getString("time"));
								Stationset[StationIndex].setLatitude(Staset.getDouble("Latitude"));
								Stationset[StationIndex].setLongitude(Staset.getDouble("Longitude"));
								Stationset[StationIndex].setNNid(Staset.getInt("nearscats"));
								Stationset[StationIndex].setValue(Staset.getDouble("value"));
								if (Staset.getInt("MM") > 0)
									{
										Stationset[StationIndex].setHH(Staset.getInt("HH") + 1);
									}
								else
									{
										Stationset[StationIndex].setHH(Staset.getInt("HH") + 1);
									}
								StationIndex++;
							}
					}
				catch (SQLException e)
					{
						e.printStackTrace();
					}

				for (int i = 0; i < Stationset.length; i++)
					{
						String time = Stationset[i].getTime().substring(0, 10) + " " + String.format("%02d", Stationset[i].getHH()) + ":00:00";
						java.sql.Timestamp newtime = new java.sql.Timestamp(System.currentTimeMillis());
						newtime = Timestamp.valueOf(time);
						String consultsql = "SELECT * FROM project.trafficvolume where ScatsID='" + Stationset[i].getNNid() + "' and Date='" + newtime + "';";
						System.out.println(consultsql);
						try
							{
								ResultSet result = consultation.executeQuery(consultsql);
								result.next();
								int trafficvalue = result.getInt("value");
								String updatesql = "update project.air set trafficvalue='" + trafficvalue + "' where ID=" + Stationset[i].getID() + ";";
								System.out.println(updatesql);
								update.executeUpdate(updatesql);
								outputvalue.println(trafficvalue + "," + Stationset[i].getValue() + ",");
							}
						catch (SQLException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					}
				outputvalue.close();
			}
	}
