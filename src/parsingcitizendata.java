import java.sql.*;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.*;
/*
 * this file is used to parse air pollution data from unofficial scientists resource.
 */
public class parsingcitizendata
	{
		static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		static final String DB_URL = "jdbc:mysql://localhost:3306/project?characterEncoding=utf8&useSSL=true";
		static final String USER = "root";
		static final String PASS = "**********";
        static final String filename="citizenairset/data349605631791785422.csv";
        final static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		public static void main(String[] args)
			{
				// : establish connection to database
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
				try{
					parsingcsv(filename,psql);
				}catch(IOException e){
					System.out.println("IOException");
				}

			}
		public static void parsingcsv(String filename, PreparedStatement psql) throws IOException
			{
				Scanner readcsv = new Scanner(new FileInputStream(filename));
				if (readcsv.nextLine().equals("time, sensor_name, ogc_fid, value, unit_symbol, longitude, latitude, unit_name"))
					{
						while (readcsv.hasNextLine())
							{
								String onepiecedata = readcsv.nextLine();
								StringTokenizer splitdata = new StringTokenizer(onepiecedata, ",");
								String date=splitdata.nextToken();
								date = date.replaceAll("T", " ");
								date=date.replaceAll("\"", "");
								date=date.replaceAll("\\.000\\+0000", "");
								java.sql.Timestamp time = new java.sql.Timestamp(System.currentTimeMillis());
								System.out.println(date);
								time = Timestamp.valueOf(date);
								splitdata.nextToken();
								int ogc_id=Integer.parseInt(splitdata.nextToken());
								double value=Double.parseDouble(splitdata.nextToken());
								splitdata.nextToken();
								double longitude=Double.parseDouble(splitdata.nextToken());
								double latitude=Double.parseDouble(splitdata.nextToken());
								try
									{
										psql.setTimestamp(1, time);
										psql.setInt(2, ogc_id);
										psql.setDouble(3, longitude);
										psql.setDouble(4, latitude);
										psql.setDouble(5, value);
										psql.executeUpdate();
									}
								catch (SQLException e)
									{
										e.printStackTrace();
									}
							}
						readcsv.close();
					}
				else
					{
						System.out.println("file head is not integrity" + filename);
						readcsv.close();
					}
			}
	}
