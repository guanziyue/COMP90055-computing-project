public class volume
	{
       private int SiteID;
       private String date;
       private int[] value=new int[24];
   
	public int getSiteID()
		{
				return SiteID;
		}
	public void setSiteID(int siteID)
		{
				SiteID = siteID;
		}
	public String getDate()
		{
				return date;
		}
	public void setDate(String date)
		{
				this.date = date;
		}
	
	public void setValue(int i,int external)
		{
				this.value[i] = external;
		}
	public int getValue(int i){
		return this.value[i];
	}
	}
