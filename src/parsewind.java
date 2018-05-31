import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.*;
import java.util.regex.*;

import javafx.animation.KeyValue.Type;

/*
  this class is used to parse the wind data from BoM. Note that, wind speed may a int/calm. it may also be empty for the losing data.
 */
public class parsewind
	{
		// SQL username, password and database name
		static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		static final String DB_URL = "jdbc:mysql://115.146.84.182:3306/project?characterEncoding=utf8&useSSL=true";
		static final String USER = "root";
		static final String PASS = "**********";
		static final SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");

		public static void main(String[] args)
			{
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
				// : traversal all csv file in wind folder
				File folder = new File("D:\\wind");
				File[] files = folder.listFiles();
				// System.out.println("files found");
				for (File file : files)
					{
						System.out.println(file);
						try
							{
								psql = con.prepareStatement("insert into project.Wind_sta (Date,stationID,9am_wind_speed,9am_wind_direction,3pm_wind_speed,3pm_wind_direction) " + "values(?,?,?,?,?,?)");
								Statement conditional_statement = con.createStatement();
								parsecsv(file, psql, conditional_statement);
							}
						catch (IOException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						catch (SQLException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					}

			}

		public static void parsecsv(File csv, PreparedStatement psql, Statement constatement) throws IOException
			{
				Scanner readwindcsv = new Scanner(new FileReader(csv));
				// skip the copy right information and others
				for (int i = 1; i < 4; i++)
					{
						// System.out.println(i+" line have been skipped");
						readwindcsv.nextLine();
					}
				final Pattern patt = Pattern.compile("Observations were drawn from.*\\{station\\s\\d{6}\\}");
				final Pattern complexpatt = Pattern.compile("wind.*\\{station\\s\\d{6}\\}");
				boolean found = false;
				String text = readwindcsv.nextLine();
				int StationID = 0;
				while (text != null)
					{
						Matcher allm = patt.matcher(text);
						Matcher subm = complexpatt.matcher(text);

						if (allm.find())
							{
								Pattern number = Pattern.compile("\\d{6}");
								Matcher yes = number.matcher(text);
								yes.find();
								StationID = Integer.parseInt(text.substring(yes.start(), yes.end()));
								// System.out.println(StationID+" has been
								// found");
								found = true;
								break;
							}
						if (subm.find())
							{
								Pattern number = Pattern.compile("\\d{6}");
								Matcher yes = number.matcher(text);
								yes.find();
								StationID = Integer.parseInt(text.substring(yes.start(), yes.end()));
								// System.out.println(StationID+" has been
								// found");
								found = true;
								break;
							}
						text = readwindcsv.nextLine();

					}
				if (!found)
					{
						System.out.println(csv + " file has error. No station ID is found. Plz check!");
					}
				else
					{
						String record = readwindcsv.nextLine();
						Pattern datepattern = Pattern.compile("Date");
						Matcher dateMatcher = datepattern.matcher(record);
						while (!dateMatcher.find())
							{
								record = readwindcsv.nextLine();
								dateMatcher = datepattern.matcher(record);
							}
						// System.out.println("data found, begin");
						while (readwindcsv.hasNextLine())
							{

								record = readwindcsv.nextLine();
								String[] values = record.split(",", -1);
								java.sql.Date date = null;
								try
									{
										date = new java.sql.Date(dateformat.parse(values[1]).getTime());
										// System.out.println(date);
									}
								catch (ParseException e)
									{
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								String nineam_wind_direction = values[13];
								int nineam_wind_speed = 0;
								if (values[14].equals("Calm"))
									{
										nineam_wind_speed = 0;
										nineam_wind_direction = "calm";
									}
								else if (values[14].equals(""))
									{
										nineam_wind_direction = "NULL";
									}
								else
									{
										nineam_wind_speed = Integer.parseInt(values[14]);
									}
								String threepm_wind_direction = values[19];
								int threepm_wind_speed = 0;
								if (values[20].equals("Calm"))
									{
										threepm_wind_speed = 0;
										threepm_wind_direction = "calm";
									}
								else if (values[20].equals(""))
									{
										threepm_wind_direction = "NULL";

									}
								else
									{
										threepm_wind_speed = Integer.parseInt(values[20]);
									}
								try
									{
										psql.setDate(1, date);
										psql.setInt(2, StationID);
										if (values[14] == null)
											{
												psql.setObject(3, null);
												psql.setObject(4, null);
											}
										else
											{
												psql.setInt(3, nineam_wind_speed);
												psql.setString(4, nineam_wind_direction);
											}
										if (values[20] == null)
											{
												psql.setObject(5, null);
												psql.setObject(6, null);

											}
										else
											{
												psql.setInt(5, threepm_wind_speed);
												psql.setString(6, threepm_wind_direction);
											}
										// System.out.println(psql);
										psql.executeUpdate();

									}
								catch (SQLException e)
									{
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

							}
					}

			}

	}
