package es.uvigo.det.ro.simpledns;

public class CNAMEResourceRecord extends ResourceRecord {
	  private final DomainName ns;
	  
	  
	  
	  protected CNAMEResourceRecord(ResourceRecord decoded, final byte[] message) {
	        super(decoded);

	        ns = new DomainName(getRRData(), message);
	    }



	public DomainName getNs() {
		return ns;
	}
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
}
