import java.sql.*;
public class Station
	{
		private int ID;
		private Date date;
		private int PointId;
		private String name;
		private double latitude;
		private double Longitude;
		private int NNid = 0;
		private int _0 = 0;
		private double value = 0;

		public int getPointId()
			{
				return PointId;
			}

		public void setPointId(int pointId)
			{
				PointId = pointId;
			}

		public String getName()
			{
				return name;
			}

		public void setName(String name)
			{
				this.name = name;
			}

		public double getLatitude()
			{
				return latitude;
			}

		public void setLatitude(double latitude)
			{
				this.latitude = latitude;
			}

		public double getLongitude()
			{
				return Longitude;
			}

		public void setLongitude(double longtitude)
			{
				this.Longitude = longtitude;
			}

		public int getID()
			{
				return ID;
			}

		public void setID(int iD)
			{
				ID = iD;
			}

		public int getNNid()
			{
				return NNid;
			}

		public void setNNid(int nNid)
			{
				NNid = nNid;
			}

		public double getValue()
			{
				return value;
			}

		public void setValue(double value)
			{
				this.value = value;
			}
	}
