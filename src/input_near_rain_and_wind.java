import java.sql.*;
import java.util.Calendar;

public class input_near_rain_and_wind
	{
		static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		static final String DB_URL = "jdbc:mysql://115.146.84.182:3306/project?characterEncoding=utf8&useSSL=true";
		static final String USER = "root";
		static final String PASS = "**********";

		public static void main(String[] args)
			{
				Connection con = null;
				Statement consultation = null;
				Statement windconsultation = null;
				Statement secondwindconsultation = null;
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
						windconsultation = con.createStatement();
						secondwindconsultation = con.createStatement();
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

				ResultSet resultSet = null;
				try
					{
						resultSet = consultation.executeQuery("select * from project.air where nearwind is null");
					}
				catch (SQLException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				System.out.println("get data from air.");

				try
					{
						while (resultSet.next())
							{
								try
									{
										int stationid = resultSet.getInt("stationID");
										int id = resultSet.getInt("ID");
										Calendar date = Calendar.getInstance();
										date.setTime(resultSet.getTimestamp("time"));
										int day = date.get(date.DAY_OF_MONTH);
										int hour = date.get(date.HOUR_OF_DAY) + 1;
										int month = date.get(date.MONTH) + 1;
										int year = date.get(date.YEAR);
										//System.out.println("id:" + id + "time:" + year + month + day + "h: " + hour + " stationid: " + stationid);
										if (hour >= 9 && hour <= 15)
											{

												ResultSet getwind = windconsultation.executeQuery("select * from project.Wind_sta " + "where project.Wind_sta.Date ='" + year + "-" + month + "-" + day + "' "
															+ "and stationID= (select project.stations.wind_station from project.stations where project.stations.PointID=" + stationid + ")");
												getwind.next();
												float ninevalue = getwind.getFloat("9am_wind_speed");
												float threevalue = getwind.getFloat("3pm_wind_speed");
												float backvalue = ninevalue + (threevalue - ninevalue) / 6 * (hour - 9);
												update.executeUpdate("update project.air set project.air.nearwind=" + backvalue + " where project.air.ID=" + id);
											}
										else if (hour < 9)
											{

												if (year == 2017 && month == 2 && day == 1)
													{

														ResultSet getwind = windconsultation.executeQuery("select * from project.Wind_sta " + "where project.Wind_sta.Date ='" + year + "-" + month + "-" + day + "' "
																	+ "and stationID= (select project.stations.wind_station from project.stations where project.stations.PointID=" + stationid + ")");
														getwind.next();
														float ninevalue = getwind.getFloat("9am_wind_speed");
														System.out.println(ninevalue);
														update.executeUpdate("update project.air set project.air.nearwind=" + ninevalue + " where project.air.ID=" + id);
													}
												else
													{
														date.add(Calendar.DAY_OF_MONTH, -1);

														ResultSet previouswind = windconsultation.executeQuery("select * from project.Wind_sta " + "where project.Wind_sta.Date ='" + date.get(Calendar.YEAR) + "-" + (date.get(Calendar.MONTH) +1)+ "-" + date.get(Calendar.DAY_OF_MONTH)+ "' "
																	+ "and stationID= (select project.stations.wind_station from project.stations where project.stations.PointID=" + stationid + ")");
														ResultSet getwind = secondwindconsultation.executeQuery("select * from project.Wind_sta " + "where project.Wind_sta.Date ='" + year + "-" + month + "-" + day + "' "
																	+ "and stationID= (select project.stations.wind_station from project.stations where project.stations.PointID=" + stationid + ")");
														previouswind.next();
														getwind.next();
														float ninevalue = getwind.getFloat("9am_wind_speed");

														float threevalue = previouswind.getFloat("3pm_wind_speed");
														float backvalue = threevalue + (ninevalue - threevalue) / 15 * (hour + 6);
														update.executeUpdate("update project.air set project.air.nearwind=" + backvalue + " where project.air.ID=" + id);
													}
											}
										else
											{

												if (year == 2018 && month == 2 && day == 28)
													{
														ResultSet getwind = windconsultation.executeQuery("select * from project.Wind_sta " + "where project.Wind_sta.Date ='" + year + "-" + month + "-" + day + "' "
																	+ "and stationID= (select project.stations.wind_station from project.stations where project.stations.PointID=" + stationid + ")");

														getwind.next();
														float threevalue = getwind.getFloat("3pm_wind_speed");
														update.executeUpdate("update project.air set project.air.nearwind=" + threevalue + " where project.air.ID=" + id);
													}
												else
													{
														date.add(Calendar.DAY_OF_MONTH, 1);
														ResultSet nextwind = windconsultation.executeQuery("select * from project.Wind_sta " + "where project.Wind_sta.Date ='" + date.get(Calendar.YEAR) + "-" + (date.get(Calendar.MONTH) +1)+ "-" + date.get(Calendar.DAY_OF_MONTH) + "' "
																	+ "and stationID= (select project.stations.wind_station from project.stations where project.stations.PointID=" + stationid + ")");
														ResultSet getwind = secondwindconsultation.executeQuery("select * from project.Wind_sta " + "where project.Wind_sta.Date ='" + year + "-" + month + "-" + day + "' "
																	+ "and stationID= (select project.stations.wind_station from project.stations where project.stations.PointID=" + stationid + ")");
														nextwind.next();
														getwind.next();
														float ninevalue = nextwind.getFloat("9am_wind_speed");
														float threevalue = getwind.getFloat("3pm_wind_speed");
														float backvalue = threevalue + (ninevalue - threevalue) / 15 * (hour - 15);
														update.executeUpdate("update project.air set project.air.nearwind=" + backvalue + " where project.air.ID=" + id);
													}
											}
									}
								catch (SQLException e)
									{
										e.printStackTrace();
										continue;
									}
							}
					}
				catch (SQLException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

//				try
//					{
//						resultSet = consultation.executeQuery("select * from project.rainfall");
//						while (resultSet.next())
//							{
//								System.out.print("update project.air set project.air.nearrain=" + resultSet.getFloat("rainfall") + " where project.air.ID=" + resultSet.getInt("ID"));
//								update.executeUpdate("update project.air set project.air.nearrain=" + resultSet.getFloat("rainfall") + " where project.air.ID=" + resultSet.getInt("ID"));
//							}
//					}
//				catch (SQLException e)
//					{
//						e.printStackTrace();
//					}

			}
	}
