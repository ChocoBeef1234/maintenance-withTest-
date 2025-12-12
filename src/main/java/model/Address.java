package main.java.model;

// Represents mailing details for a staff member; kept independent from Staff
public class Address {
    private String street;
    private String postcode;
    private String region;
    private String state;
    
	public Address(){
		street = "";
		postcode = "";
		region = "";
		state = "";
	}
	
	public Address(String street, String postcode, String region, String state) {
	    this.street = street;
	    this.postcode = postcode;
	    this.region = region;
	    this.state = state;
	}
	
	public void setstreet(String street){
		this.street=street;
	}
	
	public void setpostcode(String postcode){
		this.postcode=postcode;
	}
	
	public void setregion(String region){
		this.region=region;
	}
	
	public void setstate(String state){
		this.state=state;
	}
	
	public String getstreet() {
	    return street;
	}
	
	public String getpostcode() {
	    return postcode;
	}
	
	public String getregion() {
	    return region;
	}
	
	public String getstate() {
	    return state;
	}
}
