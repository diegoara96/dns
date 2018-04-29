package es.uvigo.det.ro.simpledns;

public class TXTResourceRecord extends ResourceRecord {
	private final String txt;
	
	 protected TXTResourceRecord(ResourceRecord decoded, final byte[] message) {
	        super(decoded);
	        txt=new String( getRRData());
	    }



	public String gettxt() {
		return txt;
	}
}
