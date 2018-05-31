import java.io.*;
import java.sql.*;
import java.util.*;
/*
 this program read available scats station id and store into database. just id
 */
public class parsingscatterslocation
	{
		static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		static final String DB_URL = "jdbc:mysql://localhost:3306/project?characterEncoding=utf8&useSSL=true";
		static final String USER = "root";
		static final String PASS = "allenguan01";
		final static String firstfilename = "vsdata_2016";
		final static String secondfilename = "VSDATA_2016";
		final static String Path = "traffic volume/alltrafficdata/";

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
						psql = con.prepareStatement("insert into scatslocation (Scatsid) " + "values(?)");
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
				for (int month = 1; month < 13; month++)
					{
						for (int Day = 1; Day < 32; Day++)
							{
								if (month < 4)
									{
										try
											{
												String filename = firstfilename + String.format("%02d", month) + String.format("%02d", Day) + ".csv";
												String filePath = Path + filename;
												parsingcsv(filePath, psql);
											}
										catch (IOException e)
											{
												System.out.println("Reading file error:" + month + Day);
												System.out.println(e.getMessage());
											}
									}
								else
									{
										try
											{
												String filename = secondfilename + String.format("%02d", month) + String.format("%02d", Day) + ".csv";
												String filePath = Path + filename;
												parsingcsv(filePath, psql);
											}
										catch (IOException e)
											{
												System.out.println("Reading file error:" + month + Day);
												System.out.println(e.getMessage());
											}
									}
							}

					}

			}

		public static void parsingcsv(String filename, PreparedStatement psql) throws IOException
			{
				Scanner readcsv = new Scanner(new FileInputStream(filename));
				System.out.println(filename);
				if (readcsv.nextLine().equals(
							"NB_SCATS_SITE,QT_INTERVAL_COUNT,NB_DETECTOR,V00,V01,V02,V03,V04,V05,V06,V07,V08,V09,V10,V11,V12,V13,V14,V15,V16,V17,V18,V19,V20,V21,V22,V23,V24,V25,V26,V27,V28,V29,V30,V31,V32,V33,V34,V35,V36,V37,V38,V39,V40,V41,V42,V43,V44,V45,V46,V47,V48,V49,V50,V51,V52,V53,V54,V55,V56,V57,V58,V59,V60,V61,V62,V63,V64,V65,V66,V67,V68,V69,V70,V71,V72,V73,V74,V75,V76,V77,V78,V79,V80,V81,V82,V83,V84,V85,V86,V87,V88,V89,V90,V91,V92,V93,V94,V95,NM_REGION,CT_RECORDS,QT_VOLUME_24HOUR,CT_ALARM_24HOUR"))
					{
						while (readcsv.hasNextLine())
							{
								try
									{
										String onepiecedata = readcsv.nextLine();
										StringTokenizer splitdata = new StringTokenizer(onepiecedata, ",");
										int ScatsiD = Integer.parseInt(splitdata.nextToken());
										psql.setInt(1, ScatsiD);
										psql.executeUpdate();
									}
								catch (SQLException e)
									{
										System.out.println(e.getMessage());
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
