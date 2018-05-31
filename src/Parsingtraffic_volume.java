import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import com.csvreader.*;

/*
 this program is to read traffic volume from csv and store in database
 */
public class Parsingtraffic_volume
	{
		static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		static final String DB_URL = "jdbc:mysql://localhost:3306/project?characterEncoding=utf8&useSSL=true";
		static final String USER = "root";
		static final String PASS = "**********";
		final static String Path = "trafficvolume/";
		final String[] Months = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };
		final static String secondfilename = "VSDATA_2018";
		final static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

		public static void main(String[] args)
			{
				PrintWriter log = null;
				try
					{
						log = new PrintWriter(new FileOutputStream("log.txt"));
					}
				catch (FileNotFoundException FILENOTFOUNDEXCE)
					{
						FILENOTFOUNDEXCE.printStackTrace();
					}
				Connection con = null;
				PreparedStatement psql = null;
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
						psql = con.prepareStatement("insert into trafficvolume (ScatsID,Date,value) " + "values(?,?,?)");
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
						log.println("1.SQL start with error:");
						log.println(e.getMessage());
					}
				// open files
				for (int month = 1; month < 3; month++)
					{
						for (int Day = 1; Day < 32; Day++)
							{

								try
									{
										String filename = secondfilename + String.format("%02d", month) + String.format("%02d", Day) + ".csv";
										String filePath = Path + filename;
										System.out.println("parsing: " + filename);
										parsingcsv(filePath, psql, log);
									}
								catch (IOException e)
									{
										System.out.println("Reading file error:" + month + Day);
										log.println("Reading file error:" + month + Day);
										log.println(e.getMessage());
									}

							}

					}
				log.close();
			}

		public static void parsingcsv(String filename, PreparedStatement psql, PrintWriter log) throws IOException
			{
				CsvReader csvReader = new CsvReader(filename);
				csvReader.readHeaders();
				csvReader.readRecord();
				// creat a new volume
				volume aVolume = new volume();
				int ScatsiD = 0;
				String dateString = null;
				// parsing scatsid
				try
					{
						ScatsiD = Integer.parseInt(csvReader.get("NB_SCATS_SITE"));
						aVolume.setSiteID(ScatsiD);
						// : find date and setDate
						dateString = csvReader.get("QT_INTERVAL_COUNT");
						dateString = dateString.substring(0, 10);
						dateString.replaceAll("///", ":");
						aVolume.setDate(dateString);
						for (int i = 0; i < 96; i++)
							{
								int readvalue = 0;
								if (csvReader.get("V" + String.format("%02d", i)).equals(""))
									{
									}
								else
									{
										readvalue = Integer.parseInt(csvReader.get("V" + String.format("%02d", i)));
									}

								if (readvalue <= 0)
									{
										continue;
									}
								else
									{
										aVolume.setValue(i / 4, readvalue + aVolume.getValue(i / 4));
									}
							}
					}
				catch (Exception e)
					{
						log.println("File: " + filename + " has error from beginning : ");
						log.println(e.getMessage());
					}
				while (csvReader.readRecord())
					{
							{
								ScatsiD = Integer.parseInt(csvReader.get("NB_SCATS_SITE"));
								if (ScatsiD != aVolume.getSiteID())
									{
										for (int i = 0; i < 24; i++)
											{
												String temptimestamp = null;
												try
													{
														// set siteID in sql
														psql.setInt(1, aVolume.getSiteID());
														// set timestamp in sql
														java.sql.Timestamp time = new java.sql.Timestamp(System.currentTimeMillis());

														temptimestamp = aVolume.getDate() + " " + String.format("%02d", i + 1) + ":00:00";
														System.out.println(temptimestamp + ScatsiD);
														time = Timestamp.valueOf(temptimestamp);
														psql.setTimestamp(2, time);
														int value = 0;
														value = aVolume.getValue(i);
														psql.setInt(3, value);
														psql.executeUpdate();
													}
												catch (SQLException e)
													{
														log.println("File: " + filename + " has error when update SQL at SiteID :" + aVolume.getSiteID() + " time: " + aVolume.getDate() + " column V" + i);
														log.println(e.getMessage());
													}

											}
										// setid
										aVolume = new volume();
										try
											{
												aVolume.setSiteID(ScatsiD);
												dateString = csvReader.get("QT_INTERVAL_COUNT");
												dateString = dateString.substring(0, 10);
												aVolume.setDate(dateString);
												for (int i = 0; i < 96; i++)
													{
														int readvalue = 0;
														if (csvReader.get("V" + String.format("%02d", i)).equals(""))
															{
															}
														else
															{
																readvalue = Integer.parseInt(csvReader.get("V" + String.format("%02d", i)));
															}
														if (readvalue < 0)
															{
																continue;
															}
														else
															{
																aVolume.setValue(i / 4, readvalue + aVolume.getValue(i / 4));
															}
													}
											}
										catch (Exception e)
											{
												log.println("File: " + filename + " has error when read a new site row: " + csvReader.getRawRecord());
												log.println(e.getMessage());
											}
									}
								else
									{
										try
											{
												for (int i = 0; i < 96; i++)
													{
														int readvalue = 0;
														if (csvReader.get("V" + String.format("%02d", i)).equals(""))
															{
															}
														else
															{
																readvalue = Integer.parseInt(csvReader.get("V" + String.format("%02d", i)));
															}
														if (readvalue < 0)
															{
																continue;
															}
														else
															{
																aVolume.setValue(i / 4, readvalue + aVolume.getValue(i / 4));
															}
													}
											}
										catch (Exception e)
											{
												log.println("File: " + filename + " has error when read a common site row: " + csvReader.getRawRecord());
												log.println(e.getMessage());
											}
									}
							}
					}
				for (int i = 0; i < 24; i++)
					{
						try
							{
								// set siteID in sql
								psql.setInt(1, aVolume.getSiteID());
								// set timestamp in sql
								java.sql.Timestamp time = new java.sql.Timestamp(System.currentTimeMillis());
								String temptimestamp = null;
								temptimestamp = aVolume.getDate() + " " + String.format("%02d", i + 1) + ":00:00";
								System.out.println(temptimestamp);
								time = Timestamp.valueOf(temptimestamp);
								psql.setTimestamp(2, time);
								int value = 0;
								value = aVolume.getValue(i);
								psql.setInt(3, value);
								psql.executeUpdate();
							}
						catch (SQLException e)
							{
								log.println("File: " + filename + " has error when update SQL at last of file SiteID :" + aVolume.getSiteID() + " time: " + aVolume.getDate() + " column V" + i);
								log.println(e.getMessage());
							}

					}
			}
	}
