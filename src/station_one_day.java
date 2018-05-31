import java.util.Date;
import java.util.Calendar;

public class station_one_day
	{
		private int PointId;
		private Date date;
		private double[] values=new double[24];
		private int count=0;
		public int getPointId()
			{
					return PointId;
			}
		public void setPointId(int pointId)
			{
					PointId = pointId;
			}
		public Date getDate()
			{
					return date;
			}
		public void setDate(Date date)
			{
					this.date = date;
			}
		public double[] getValues()
			{
					return values;
			}
		public void setValues(double[] values)
			{
					this.values = values;
			}
		public int getCount()
			{
					return count;
			}
		public void setCount(int count)
			{
					this.count = count;
			}
	}
