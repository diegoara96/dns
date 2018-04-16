
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import es.uvigo.det.ro.simpledns.*;
import funciones.EnvioPaquetes;

public class dnsclient {

	public static void main(String[] args) throws Exception {
		String modoConexion;
		if (args.length != 2) {
			System.out.println("numero de argumentos incorrecto");
		}
		args[0] = args[0].toLowerCase();
		System.out.println(args[0]);
		if (!(args[0].equals("-t") || args[0].equals("-u"))) {
			System.out.println("modo de conexion distinto de -(t|u)");
		}
		if (args[0].equals("-t")) {
			modoConexion = "UDP";

			System.out.println("No se halla implementado el uso de TCP. Se usará UDP");
			args[0] = "-u";
		} else
			modoConexion = "UDP";

		String ipCachos[] = args[1].split("\\.");
		byte ip[] = new byte[4];

		if (ipCachos.length != 4) {
			System.out.println("Direcion ip DNS incorrecta");
			System.exit(1);
		}
		for (int i = 0; i < 4; i++) {

			byte b = (byte) Integer.parseInt(ipCachos[i]);
			ip[i] = (byte) (b & 0xFF);

		}

		BufferedReader algo = new BufferedReader(new InputStreamReader(System.in));
		String entrada;

		while ((entrada = algo.readLine()) != null) {
			System.out.println();
			Inet4Address ServerPregunta = (Inet4Address) InetAddress.getByAddress(ip);
			Inet4Address ipOriginal = (Inet4Address) InetAddress.getByAddress(ip);
			String partes[] = entrada.split("\\s+");
			if (partes.length != 2) {
				System.out.println("faltan parametros en la entrada");
				continue;
			}
			partes[0] = partes[0].toUpperCase().trim();
			if (partes[0].equals("A") || partes[0].equals("NS")|| partes[0].equals("AAAA")) {

				Message inicial = new Message(partes[1], RRType.valueOf(partes[0]), false);
				int o = 0;
				ArrayList<String> dnsConsultados = new ArrayList<>();

				while (o < 7) {

					System.out.println("Q " + modoConexion + " "
							+ ServerPregunta.toString().substring(1, ServerPregunta.toString().length()) + " "
							+ inicial.getQuestionType() + " " + partes[1]);
					Message respuesta;
					int contador = 0;
					do {
						contador++;
						respuesta = EnvioPaquetes.envioUDP(inicial, ServerPregunta);
					} while (respuesta == null && contador < 3);
					if (respuesta == null) {
						System.out.println("No hay respuesta del servidor consultado");
						System.exit(1);
					}
					if (!(respuesta.getAnswers().isEmpty())) {

						
						if (respuesta.getAnswers().get(0).getRRType().equals(RRType.CNAME)) {
							System.out.println(
									"A " + ServerPregunta.toString().substring(1, ServerPregunta.toString().length())
											+ " CNAME");
						//		DomainName consulta=((CNAMEResourceRecord)respuesta.getAnswers().get(0)).getNs();
							//	ServerPregunta=noDNS(consulta, ipOriginal, RRType.A, dnsConsultados);
								//continue;
							break;
						}
						
						else {
							answer (respuesta,ServerPregunta);
						}
						
						break;
					} else if ((!respuesta.getNameServers().isEmpty() && !respuesta.getAdditonalRecords().isEmpty())) {
						boolean a = false;
						
						
			bucle:			for (int b = 0; b < respuesta.getNameServers().size(); b++) {
							for (int i = 0; i < respuesta.getAdditonalRecords().size(); i++) {

								if (((respuesta.getAdditonalRecords().get(i))) instanceof AResourceRecord) {
									if (((NSResourceRecord) respuesta.getNameServers().get(b)).getNS().toString()
											.equals(((AResourceRecord) respuesta.getAdditonalRecords().get(i))
													.getDomain().toString())) {
										if (!dnsConsultados
												.contains(((AResourceRecord) (respuesta.getAdditonalRecords().get(i)))
														.getAddress().toString())) {

											System.out.println("A "
													+ ServerPregunta.toString().substring(1,
															ServerPregunta.toString().length())
													+ " " + respuesta.getNameServers().get(b).getRRType() + " "
													+ respuesta.getNameServers().get(b).getTTL() + " "
													+ ((NSResourceRecord) (respuesta.getNameServers().get(b))).getNS());
											System.out.println("A "
													+ ServerPregunta.toString().substring(1,
															ServerPregunta.toString().length())

													+ " " + respuesta.getAdditonalRecords().get(i).getRRType() + " "

													+ respuesta.getNameServers().get(b).getTTL() + " "
													+ ((AResourceRecord) (respuesta.getAdditonalRecords().get(i)))
															.getAddress().toString().substring(1,
																	((AResourceRecord) (respuesta.getAdditonalRecords()
																			.get(i))).getAddress().toString()
																					.length()));
											ServerPregunta = ((AResourceRecord) (respuesta.getAdditonalRecords()
													.get(i))).getAddress();
											
											dnsConsultados.add(ServerPregunta.toString());
											a = true;
											break bucle;
										}
						
									}
								}
							}
							if(a==true)break;
						}
						if (a == false) {
							System.out.println("ni puta");
							break;
						}
					} else if(respuesta.getAdditonalRecords().isEmpty()&&!respuesta.getNameServers().isEmpty()){
						// cambiar respeusta de tipo mensaje a tipo domain o string para poder usarlo tanto en cname como 
						//para la recursividad 
						System.out.println("El campo additonal está vacio");
						DomainName consulta = ((NSResourceRecord)respuesta.getNameServers().get(0)).getNS();
						RRType tipo= RRType.A;
						
					ServerPregunta=	noDNS(consulta, ipOriginal,tipo,dnsConsultados);
					
					if(ServerPregunta==null) {
						System.out.println("El campo additonal está vacio");
						System.exit(1);
					}
					dnsConsultados.add(ServerPregunta.toString());
					o++;
					System.out.println("sale");
					continue;
					//	break;
						
						
					}
					else {
						System.out.println("No hay respuesta");
						break;
					}
					o++;
				}

			} else {
				System.out.println("type no admitido");
				continue;
			}

		}

	}
	public static void answer (Message respuesta,Inet4Address ServerPregunta) {
		if ((respuesta.getAnswers().get(0)) instanceof AResourceRecord) {
			System.out.println("A "
					+ ServerPregunta.toString().substring(1, ServerPregunta.toString().length()) + " "
					+ ((AResourceRecord) (respuesta.getAnswers().get(0))).getAddress().toString()
							.substring(1, ((AResourceRecord) (respuesta.getAnswers().get(0)))
									.getAddress().toString().length()));
		}
		if ((respuesta.getAnswers().get(0)) instanceof NSResourceRecord) {
			for(int i=0;i<respuesta.getAnswers().size();i++) {
				
			
			System.out.println("A "
					+ ServerPregunta.toString().substring(1, ServerPregunta.toString().length()) + " "
					+ ((NSResourceRecord) (respuesta.getAnswers().get(i))).getNS().toString()
							.substring(1, (((NSResourceRecord) (respuesta.getAnswers().get(i))).getNS()
									.toString().length())));
		}
			}
		
		if ((respuesta.getAnswers().get(0)) instanceof AAAAResourceRecord) {
			System.out.println("A "
					+ ServerPregunta.toString().substring(1, ServerPregunta.toString().length()) + " "
					+ ((AAAAResourceRecord) (respuesta.getAnswers().get(0))).getAddress().toString()
							.substring(1, ((AAAAResourceRecord) (respuesta.getAnswers().get(0)))
									.getAddress().toString().length()));
		}
	}
	
	
	
	
	
	
	
	public static Inet4Address noDNS(DomainName consulta,Inet4Address ipOriginal,RRType tipo,ArrayList<String> dnsString) {
		int contador=0;
		Message respuesta=null;
		Message dns = new Message(consulta, RRType.A, false);
		Inet4Address ServerPregunta = ipOriginal;
		Inet4Address ip=null;
		String modoConexion="UDP";
		ArrayList<String> dnsConsultados = new ArrayList<>();
		for(int o=0;o<7;o++) {

			System.out.println("Q " + modoConexion + " "
					+ ServerPregunta.toString().substring(1, ServerPregunta.toString().length()) + " "
					+ tipo + " " + consulta.toString());
		do {
			
			contador++;
			respuesta = EnvioPaquetes.envioUDP(dns, ipOriginal);
		} while (respuesta == null && contador < 3);
		if (respuesta == null) {
			System.out.println("No hay respuesta del servidor consultado");
			System.exit(1);
		}
		
		
		if (!(respuesta.getAnswers().isEmpty())) {
				answer(respuesta, ipOriginal);
				ip = ((AResourceRecord) (respuesta.getAnswers()
						.get(0))).getAddress();
				
				return ip;
			}
		
			else if ((!respuesta.getNameServers().isEmpty() && !respuesta.getAdditonalRecords().isEmpty())) {
		boolean a = false;
		for (int b = 0; b < respuesta.getNameServers().size(); b++) {
			System.out.println("A "
					+ ServerPregunta.toString().substring(1,
							ServerPregunta.toString().length())
					+ " " + respuesta.getNameServers().get(b).getRRType() + " "
					+ respuesta.getNameServers().get(b).getTTL() + " "
					+ ((NSResourceRecord) (respuesta.getNameServers().get(b))).getNS());
		}
		
	bucle:	for (int b = 0; b < respuesta.getNameServers().size(); b++) {
		System.out.println("A "
				+ ServerPregunta.toString().substring(1,
						ServerPregunta.toString().length())
				+ " " + respuesta.getNameServers().get(b).getRRType() + " "
				+ respuesta.getNameServers().get(b).getTTL() + " "
				+ ((NSResourceRecord) (respuesta.getNameServers().get(b))).getNS());
			for (int i = 0; i < respuesta.getAdditonalRecords().size(); i++) {
				if (((respuesta.getAdditonalRecords().get(i))) instanceof AResourceRecord) {
					if (((NSResourceRecord) respuesta.getNameServers().get(b)).getNS().toString()
							.equals(((AResourceRecord) respuesta.getAdditonalRecords().get(i))
									.getDomain().toString())) {
						if (!dnsConsultados
								.contains(((AResourceRecord) (respuesta.getAdditonalRecords().get(i)))
										.getAddress().toString())) {
							
							
							System.out.println("A "
									+ ServerPregunta.toString().substring(1,
											ServerPregunta.toString().length())
									+ " " + respuesta.getNameServers().get(b).getRRType() + " "
									+ respuesta.getNameServers().get(b).getTTL() + " "
									+ ((NSResourceRecord) (respuesta.getNameServers().get(b))).getNS());
							System.out.println("A "
									+ ServerPregunta.toString().substring(1,
											ServerPregunta.toString().length())

									+ " " + respuesta.getAdditonalRecords().get(i).getRRType() + " "

									+ respuesta.getNameServers().get(b).getTTL() + " "
									+ ((AResourceRecord) (respuesta.getAdditonalRecords().get(i)))
											.getAddress().toString().substring(1,
													((AResourceRecord) (respuesta.getAdditonalRecords()
															.get(i))).getAddress().toString()
																	.length()));
							ServerPregunta = ((AResourceRecord) (respuesta.getAdditonalRecords()
									.get(i))).getAddress();
							System.out.println("por aqui anda");
							dnsConsultados.add(ServerPregunta.toString());
							ip= ServerPregunta;
							a=true;
							break bucle;
						}

					}
				}
			}
		//	if(a==true)break;
		}
		if (a == false) {
			System.out.println("ni puta");
			break;
		}
	}
		
	 else if(respuesta.getAdditonalRecords().isEmpty()){
		System.out.println("jooooder");
		return null;
	 }
	//	o++;
		}	
		
		
	return null;
	
	}
	
	
	
	
	
	
	
	
}
