import java.sql.*;
import java.io.*;
import java.util.*;
import java.net.*;
import org.jdom2.*;
import org.jdom2.input.*;
/*
 * this program retrieve station information from epa to get available air station.
 */
public class Parsing
	{
		final static String host = "http://sciwebsvc.epa.vic.gov.au/aqapi/";
		final static String StationData = "StationData?pointId=";
		static ArrayList<Station> Stations = new ArrayList<Station>();
		static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		static final String DB_URL = "jdbc:mysql://localhost:3306/project?characterEncoding=utf8&useSSL=true";
		static final String USER = "root";
		static final String PASS = "allenguan01";

		public static void main(String[] args)
			{
				// : establish connection to database
				Connection con = null;
				PreparedStatement psql = null;
				try
					{
						// load driver
						Class.forName(JDBC_DRIVER);
						//establish connection with Database
						con = DriverManager.getConnection(DB_URL, USER, PASS);
						if (!con.isClosed())
							{
								System.out.println("Succeeded connecting to the Database!");
							}
						psql = con.prepareStatement("insert into stations (PointID,Name,Latitude,Longitude) " + "values(?,?,?,?)");
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
						// TODO: handle exception
						e.printStackTrace();
					}
                //start from station 100001
				int StationID = 10001;

				while (StationID < 10240)
					{
						Document aNewDocument = retrieveStation(StationID);
						try
							{
								if(aNewDocument!=null){
								parsing(aNewDocument, StationID, psql);}
							}
						catch (SQLException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						StationID++;
					}
				for (int i = 0; i < Stations.size(); i++)
					{
						System.out.println(Stations.get(i).getPointId() + " " + Stations.get(i).getName());
					}
			}

		// : retrieve xml document from particular url
		public static Document retrieveStation(int StationID)
			{
				URL targetUrl = null;
				Document document = null;

				try
					{
						targetUrl = new URL(host + StationData + (Integer) StationID);
						SAXBuilder builder = new SAXBuilder();
						document = builder.build(targetUrl);
					}
				catch (JDOMException | IOException e)
					{
						System.out.println("Retrieve Station error.Station ID"+StationID);
						e.printStackTrace();
						System.exit(0);
					}

				return document;
			}

		// : parsing xml document use Iterators
		public static void parsing(Document document, int StationID, PreparedStatement psql) throws SQLException
			{
				int index = 0;
				//get root element
				Element root = document.getRootElement();
				//get child
				List<Element> bookList = root.getChildren();
				//use boolean variable to distinguish if this is a available station
				Boolean GoodStation = false;
				for (Element book : bookList)
					{
						if (book.getName() == "HasPm25")
							{
								if (book.getValue() == "true")
									{
										Stations.add(new Station());
										index = Stations.size() - 1;
										Stations.get(index).setPointId(StationID);
										psql.setInt(1, 3212);
										GoodStation = true;
									}
								else
									{
										break;
									}
							}
						if (book.getName() == "Latitude")
							{
								Stations.get(index).setLatitude((long) Float.parseFloat(book.getValue()));
								psql.setFloat(3, Float.parseFloat(book.getValue()));
							}
						if (book.getName() == "Longitude")
							{
								Stations.get(index).setLongitude((long) Float.parseFloat(book.getValue()));
								psql.setFloat(4, Float.parseFloat(book.getValue()));
							}
						if (book.getName() == "Station")
							{
								Stations.get(index).setName(book.getValue());
								psql.setString(2, book.getValue());
							}
					}
				if (GoodStation)
					{
						psql.executeUpdate();
					}
			}
	}
