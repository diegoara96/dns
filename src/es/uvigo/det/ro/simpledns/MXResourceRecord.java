package es.uvigo.det.ro.simpledns;

import java.util.Arrays;

public class MXResourceRecord extends ResourceRecord {
	private final DomainName ns;
	private int prioridad;

	protected MXResourceRecord(ResourceRecord decoded, final byte[] message) {
		super(decoded);
		byte buffer[];
		byte numero[]=Arrays.copyOfRange(getRRData(), 0, 2);
		
		prioridad = Utils.int16fromByteArray(numero);
		buffer = Arrays.copyOfRange(getRRData(), 2, getRRData().length);
		ns = new DomainName(buffer, message);
	}

	public DomainName getNS() {
		return ns;
	}

	public int getPrioridad() {
		return prioridad;
	}

}
