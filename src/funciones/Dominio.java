package funciones;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import es.uvigo.det.ro.simpledns.DomainName;
import es.uvigo.det.ro.simpledns.MXResourceRecord;
import es.uvigo.det.ro.simpledns.NSResourceRecord;
import es.uvigo.det.ro.simpledns.RRType;
import es.uvigo.det.ro.simpledns.ResourceRecord;

public class Dominio {
	private List<ResourceRecord> serverNS;
	private List<ResourceRecord> serverMX;
	private DomainName alias;
	private Inet4Address ip;
	private Inet6Address ipv6;
	private int TTL;

	public Dominio() {
		this.serverNS = new ArrayList<>();
		this.serverMX = new ArrayList<>();
	}
	// contructor para ipv4
	public Dominio(Inet4Address ip, int TTL) {
		this();
		this.ip = ip;
		this.TTL = TTL;

	}

	// contructor para ipv6
	public Dominio(Inet6Address ipv6, int TTL) {
		this();
		this.ipv6 = ipv6;
		this.TTL = TTL;
	}

	public Dominio(DomainName alias, int TTL) {
		this();
		this.alias = alias;
		this.TTL = TTL;
	}

	public Dominio(List<ResourceRecord> server, RRType tipo) {
		this();
		if (tipo.equals(RRType.NS)) {
			this.serverNS = server;
		} else {
			this.serverMX = server;
		}
	}

	public InetAddress getAddress(RRType tipo) {

		switch (tipo) {
		case A:
			return ip;
		case AAAA:
			return ipv6;
		default:
			return null;
		}
	}

	public List<ResourceRecord> listas(RRType tipo) {
		switch (tipo) {
		case NS:
			return serverNS;
		case MX:
			return serverMX;
		default:
			return null;
		}
	}

	public static void answerCache(Dominio respuesta, RRType tipo) {
		if (tipo.equals(RRType.A)) {
			System.out.println("A " + "CACHE" + " " + respuesta.getTTL() + " "
					+ respuesta.getAddress(tipo).toString().substring(1));
		}

		if (tipo.equals(RRType.NS)) {
			for (int i = 0; i < respuesta.serverNS.size(); i++) {

				System.out.println("A " + "CACHE" + " " + respuesta.serverNS.get(i).getTTL() + " "
						+ ((NSResourceRecord) (respuesta.serverNS.get(i))).getNS().toString());

			}
		}

		if (tipo.equals(RRType.AAAA)) {
			System.out.println("A " + "CACHE" + " " + respuesta.getTTL() + " "
					+ respuesta.getAddress(tipo).toString().substring(1));
		}

		if (tipo.equals(RRType.MX)) {
			for (int i = 0; i < respuesta.serverMX.size(); i++) {
				System.out.println("A " + "CACHE" + " " + respuesta.serverMX.get(i).getTTL() + " "
						+ ((MXResourceRecord) (respuesta.serverMX.get(i))).getNS().toString());
			}
		}

		if (tipo.equals(RRType.CNAME)) {

			System.out.println("A " + "CACHE" + " CNAME" + " " + respuesta.alias);
		}
	}

	public void setServerNS(List<ResourceRecord> serverNS) {

		this.serverNS = serverNS;
	}

	public void setServerMX(List<ResourceRecord> serverMX) {
		this.serverMX = serverMX;
	}

	public void setIp(Inet4Address ip) {
		this.ip = ip;
	}

	public void setIpv6(Inet6Address ipv6) {
		this.ipv6 = ipv6;
	}

	public void setTTL(int ttl) {
		this.TTL = ttl;
	}

	public List<ResourceRecord> getServerNS() {
		return serverNS;
	}

	public List<ResourceRecord> getServerMX() {
		return serverMX;
	}

	public Inet4Address getIp() {
		return ip;
	}

	public InetAddress getIpv6() {
		return ipv6;
	}

	public int getTTL() {
		return TTL;
	}

	public DomainName getAlias() {
		return alias;
	}

	public void setAlias(DomainName alias) {
		this.alias = alias;
	}

	public boolean rtype(RRType tipo) {
		if (tipo.equals(RRType.NS) && !serverNS.isEmpty()) {
			return true;
		} else if (tipo.equals(RRType.MX) && !serverMX.isEmpty()) {
			return true;
		} else if (tipo.equals(RRType.A) && ip != null) {
			return true;
		} else if (tipo.equals(RRType.AAAA) && ipv6 != null) {
			return true;
		} else if (tipo.equals(RRType.CNAME) && alias != null) {
			return true;
		} else
			return false;
	}

}
