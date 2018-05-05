package es.uvigo.det.ro.simpledns;

public class PTRResourceRecord extends ResourceRecord {
	private DomainName ptr;
	protected PTRResourceRecord(ResourceRecord decoded, final byte[] message) {
        super(decoded);

        ptr = new DomainName(getRRData(), message);
    }



public DomainName getPtr() {
	return ptr;
}
}
