package dsd.dsdvansales.model;

public class TripUser {
	public String strRouteNumber = "";
	public String strDescription = "";
	public String strSecurity = "";
	public String strUserName = "";
	public String strTrip = "";
	
	public TripUser()
	{
		
	}
	
	@Override
	public String toString()
	{
		return strDescription;
	}
}
