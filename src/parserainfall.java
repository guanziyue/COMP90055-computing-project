import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;
import org.apache.commons.io.input.ReversedLinesFileReader;

public class parserainfall
	{
		// SQL username, password and database name
		static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		static final String DB_URL = "jdbc:mysql://115.146.84.182:3306/project?characterEncoding=utf8&useSSL=true";
		static final String USER = "root";
		static final String PASS = "allenguan01";
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
				File folder = new File("D:\\rain");
				File[] files = folder.listFiles();
				// System.out.println("files found");
				for (File file : files)
					{
						System.out.println(file);
						try
							{
								psql = con.prepareStatement("insert into project.rainfall_sta (Date,stationID,rainfall) " + "values(?,?,?)");
								parsecsv(file, psql);
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

		public static void parsecsv(File file, PreparedStatement psql) throws IOException
			{
				System.out.println("parsing start £º"+file);
				ReversedLinesFileReader filereader = new ReversedLinesFileReader(file, StandardCharsets.UTF_8);
				String record = filereader.readLine();
				System.out.println(record);
				String[] piece = record.split(",", -1);
				while (piece[2].equals("2017")|piece[2].equals("2018"))
					{
						int stationID = Integer.parseInt(piece[1]);
						String date_string = piece[2] + "-" + piece[3] + "-" + piece[4];
						java.sql.Date date = null;
						try
							{
								date = new java.sql.Date(dateformat.parse(date_string).getTime());
							}
						catch (ParseException e)
							{
								e.printStackTrace();
							}
						float measure = 0;
						if (Pattern.matches("^[0-9]+(\\.[0-9]+)?$", piece[5]))
							{
								measure = Float.parseFloat(piece[5]);
							}
						else
							{
								try
									{
										psql.setDate(1, date);
										psql.setInt(2, stationID);
										psql.setObject(3, null);
										psql.execute();
										record = filereader.readLine();
										piece = record.split(",", -1);
										continue;
									}
								catch (SQLException e)
									{
										System.out.println(psql);
										e.printStackTrace();
									}
							}
						int measure_period = 0;
						if (measure != 0)
							{
								measure_period = Integer.parseInt(piece[6]);
								measure = measure / measure_period;
								try
									{
										psql.setDate(1, date);
										psql.setInt(2, stationID);
										psql.setFloat(3, measure);
										psql.execute();
									}
								catch (SQLException e)
									{
										System.out.println(psql);
										e.printStackTrace();
									}
								if (measure_period > 1)
									{
										for (int i = measure_period - 1; i > 0; i--)
											{
												record = filereader.readLine();
												piece = record.split(",", -1);
												stationID = Integer.parseInt(piece[1]);
												date_string = piece[2] + "-" + piece[3] + "-" + piece[4];
												try
													{
														date = new java.sql.Date(dateformat.parse(date_string).getTime());
													}
												catch (ParseException e)
													{
														e.printStackTrace();
													}
												try
													{
														psql.setDate(1, date);
														psql.setInt(2, stationID);
														psql.setFloat(3, measure);
														psql.execute();
													}
												catch (SQLException e)
													{
														System.out.println(psql);
														e.printStackTrace();
													}
											}
									}
							}
						else
							{
								try
									{
										psql.setDate(1, date);
										psql.setInt(2, stationID);
										psql.setFloat(3, measure);
										psql.execute();
									}
								catch (SQLException e)
									{
										System.out.println(psql);
										e.printStackTrace();
									}
							}
						record = filereader.readLine();
						piece = record.split(",", -1);
					}

			}

	}
