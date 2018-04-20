package es.uvigo.det.ro.simpledns;

import java.util.Arrays;

public class MXResourceRecord extends ResourceRecord {
	private final DomainName ns;
	
	 protected MXResourceRecord(ResourceRecord decoded, final byte[] message) {
	        super(decoded);
	        byte  buffer[];
	        buffer = Arrays.copyOfRange(getRRData(), 2, getRRData().length);

	        ns = new DomainName(buffer, message);
	    }



	public DomainName getNS() {
		return ns;
	}
}
