
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import es.uvigo.det.ro.simpledns.*;
import funciones.Dominio;
import funciones.EnvioPaquetes;

public class dnsclient {
	public static HashMap<String, Dominio> cache = new HashMap<>();
/**
 * 
 * @param args 
 * @throws Exception
 */
	public static void main(String[] args)  {

		String modoConexion;

		if (args.length != 2) {
			System.out.println("numero de argumentos incorrecto");
		}

		args[0] = args[0].toLowerCase();
		if (!(args[0].equals("-t") || args[0].equals("-u"))) {
			System.out.println("modo de conexion distinto de -(t|u)");
			System.exit(1);
		}

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

		try {
			siguiente: 	while ((entrada = algo.readLine()) != null) {

				if (args[0].equals("-t")) {
					modoConexion = "TCP";
				} else {
					modoConexion = "UDP";
				}

				System.out.println();
				Inet4Address ServerPregunta = (Inet4Address) InetAddress.getByAddress(ip);
				Inet4Address ipOriginal = (Inet4Address) InetAddress.getByAddress(ip);
				String partes[] = entrada.split("\\s+");
				if (partes.length != 2) {
					System.out.println("faltan parametros en la entrada");
					continue;
				}

				partes[0] = partes[0].toUpperCase().trim();
				if (partes[0].equals("A") || partes[0].equals("NS") || partes[0].equals("AAAA") || partes[0].equals("MX")
						|| partes[0].equals("CNAME")|| partes[0].equals("TXT")) {

					Message inicial = new Message(partes[1], RRType.valueOf(partes[0]), false);
					int o = 0;
					ArrayList<String> dnsConsultados = new ArrayList<>();

					System.out.println("Q " + "CACHE" + " " + "CACHE" + " " + inicial.getQuestionType() + " " + partes[1]);

					if (cache.containsKey(inicial.getQuestion().toString())
							&& cache.get(inicial.getQuestion().toString()).rtype(RRType.valueOf(partes[0]))) {
						Dominio.answerCache(cache.get(inicial.getQuestion().toString()), RRType.valueOf(partes[0]));
						continue siguiente;
					} else {
						System.out.println("La cache no tiene la respuesta");
					}

					// se limita las consultas a 7 por defecto
					bucle: while (o < 7) {

						System.out.println("Q " + modoConexion + " "
								+ ServerPregunta.toString().substring(1, ServerPregunta.toString().length()) + " "
								+ inicial.getQuestionType() + " " + partes[1]);
						
						
						
						Message respuesta = EnvioPaquetes.envio(inicial, ServerPregunta, modoConexion);
						
						if (respuesta == null) {
							System.out.println("No hay respuesta del servidor consultado");
							break bucle;
						}

						if (!(respuesta.getAnswers().isEmpty())) {

							if (respuesta.getAnswers().get(0).getRRType().equals(RRType.CNAME)
									&& !RRType.valueOf(partes[0]).equals(RRType.CNAME)) {

								DomainName consulta = ((CNAMEResourceRecord) respuesta.getAnswers().get(0)).getNs();
								System.out.println(
										"A " + ServerPregunta.toString().substring(1, ServerPregunta.toString().length())
												+ " CNAME" + " " + consulta.toString());
								ServerPregunta = noDNS(consulta, ipOriginal, RRType.valueOf(partes[0]),modoConexion);
								if (ServerPregunta == null) {

									continue siguiente;
								}

							} else
								answer(respuesta, ServerPregunta);

							break;
						} else if (!respuesta.getNameServers().isEmpty()
								&& respuesta.getNameServers().get(0).getRRType().equals(RRType.SOA)) {
							System.out.println("La respuesta es de tipo SOA");
							break bucle;
						} else if ((!respuesta.getNameServers().isEmpty() && !respuesta.getAdditonalRecords().isEmpty())) {
							boolean a = false;

							for (int b = 0; b < respuesta.getNameServers().size(); b++) {
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
												continue bucle;
											}

										}
									}
								}
								if (a == true)
									break;
							}
							if (a == false) {
								System.out.println("no se ha encontrado un dns valido");
								break;
							}
						} else if (respuesta.getAdditonalRecords().isEmpty() && !respuesta.getNameServers().isEmpty()) {

							DomainName consulta = ((NSResourceRecord) respuesta.getNameServers().get(0)).getNS();
							System.out.println(
									"A " + ServerPregunta.toString().substring(1, ServerPregunta.toString().length()) + " "
											+ respuesta.getNameServers().get(0).getRRType() + " "
											+ respuesta.getNameServers().get(0).getTTL() + " " + consulta);
							System.out.println("No hay registro tipo A en seccion ADDITIONAL para " + consulta);
							RRType tipo = RRType.A;

							ServerPregunta = noDNS(consulta, ipOriginal, tipo,modoConexion);

							if (ServerPregunta == null) {
								System.out.println("El campo additonal está vacio");
								continue siguiente;
							}
							dnsConsultados.add(ServerPregunta.toString());
							o++;

							continue bucle;
							// break;

						} else {
							System.out.println("No hay respuesta");
							break;
						}
						o++;
					}

				} else {
					System.out.println("type no admitido " + partes[0]);
					continue;
				}

			}
		} catch (IOException e) {
			
			e.printStackTrace();
		}

	
}
	

	public static void answer(Message respuesta, Inet4Address ServerPregunta) {
		if ((respuesta.getAnswers().get(0)) instanceof AResourceRecord) {
			System.out.println("A " + ServerPregunta.toString().substring(1, ServerPregunta.toString().length()) + " "
					+ respuesta.getAnswers().get(0).getTTL() + " "
					+ ((AResourceRecord) (respuesta.getAnswers().get(0))).getAddress().toString().substring(1,
							((AResourceRecord) (respuesta.getAnswers().get(0))).getAddress().toString().length()));

			if (!cache.containsKey(respuesta.getQuestion().toString())) {
				cache.put(respuesta.getQuestion().toString(),
						new Dominio(((AResourceRecord) (respuesta.getAnswers().get(0))).getAddress(),
								respuesta.getAnswers().get(0).getTTL()));

			} else {
				cache.get(respuesta.getQuestion().toString())
						.setIp(((AResourceRecord) (respuesta.getAnswers().get(0))).getAddress());
			}

		}
		if ((respuesta.getAnswers().get(0)) instanceof TXTResourceRecord) {
			System.out.println("A " + ServerPregunta.toString().substring(1, ServerPregunta.toString().length()) + " "
					+ respuesta.getAnswers().get(0).getTTL() +" "+((TXTResourceRecord)respuesta.getAnswers().get(0)).gettxt());
		}
		if ((respuesta.getAnswers().get(0)) instanceof NSResourceRecord) {
			for (int i = 0; i < respuesta.getAnswers().size(); i++) {

				System.out.println("A " + ServerPregunta.toString().substring(1, ServerPregunta.toString().length())
						+ " " + respuesta.getAnswers().get(i).getTTL() + " "
						+ ((NSResourceRecord) (respuesta.getAnswers().get(i))).getNS().toString().substring(1,
								(((NSResourceRecord) (respuesta.getAnswers().get(i))).getNS().toString().length())));
			}

			if (!cache.containsKey(respuesta.getQuestion().toString())) {
				cache.put(respuesta.getQuestion().toString(),
						new Dominio(respuesta.getAnswers(), respuesta.getQuestionType()));

			} else {
				cache.get(respuesta.getQuestion().toString()).setServerNS(respuesta.getAnswers());
			}

		}
		
		

		if ((respuesta.getAnswers().get(0)) instanceof AAAAResourceRecord) {
			System.out.println("A " + ServerPregunta.toString().substring(1, ServerPregunta.toString().length()) + " "
					+ respuesta.getAnswers().get(0).getTTL() + " "
					+ ((AAAAResourceRecord) (respuesta.getAnswers().get(0))).getAddress().toString().substring(1,
							((AAAAResourceRecord) (respuesta.getAnswers().get(0))).getAddress().toString().length()));

			if (!cache.containsKey(respuesta.getQuestion().toString())) {
				cache.put(respuesta.getQuestion().toString(),
						new Dominio(((AAAAResourceRecord) (respuesta.getAnswers().get(0))).getAddress(),
								respuesta.getAnswers().get(0).getTTL()));

			} else {
				cache.get(respuesta.getQuestion().toString())
						.setIpv6(((AAAAResourceRecord) (respuesta.getAnswers().get(0))).getAddress());
			}

		}
		if ((respuesta.getAnswers().get(0)) instanceof MXResourceRecord) {
			for (int i = 0; i < respuesta.getAnswers().size(); i++) {
				System.out.println("A " + ServerPregunta.toString().substring(1, ServerPregunta.toString().length())
						+ " " + respuesta.getAnswers().get(i).getTTL() + " "
						+ ((MXResourceRecord) (respuesta.getAnswers().get(i))).getNS().toString());
			}
			if (!cache.containsKey(respuesta.getQuestion().toString())) {
				cache.put(respuesta.getQuestion().toString(),
						new Dominio(respuesta.getAnswers(), respuesta.getQuestionType()));

			} else {
				cache.get(respuesta.getQuestion().toString()).setServerMX(respuesta.getAnswers());
			}
		}

		if ((respuesta.getAnswers().get(0)) instanceof CNAMEResourceRecord) {

			System.out.println("A " + ServerPregunta.toString().substring(1, ServerPregunta.toString().length())
					+ " CNAME" + " " + ((CNAMEResourceRecord) respuesta.getAnswers().get(0)).getNs().toString());
			if (!cache.containsKey(respuesta.getQuestion().toString())) {
				cache.put(respuesta.getQuestion().toString(),
						new Dominio(((CNAMEResourceRecord) respuesta.getAnswers().get(0)).getNs(),
								respuesta.getAnswers().get(0).getTTL()));

			} else {
				cache.get(respuesta.getQuestion().toString())
						.setAlias(((CNAMEResourceRecord) respuesta.getAnswers().get(0)).getNs());
			}
		}

	}

	public static Inet4Address noDNS(DomainName consulta, Inet4Address ipOriginal, RRType tipo,String modoConexion) {
		//int contador = 0;
		Message respuesta = null;
		Message dns = new Message(consulta, tipo, false);
		Inet4Address ServerPregunta = ipOriginal;
		Inet4Address ip = null;
		
		ArrayList<String> dnsConsultados = new ArrayList<>();

		siguiente: for (int o = 0; o < 7; o++) {
			if (ServerPregunta == null) {
				return null;
			}
			System.out.println("Q " + modoConexion + " "
					+ ServerPregunta.toString().substring(1, ServerPregunta.toString().length()) + " " + tipo + " "
					+ consulta.toString());
		//	do {

			//	contador++;
				respuesta = EnvioPaquetes.envio(dns, ServerPregunta, modoConexion);
		//	} while (respuesta == null && contador < 3);
			if (respuesta == null) {
				System.out.println("No hay respuesta del servidor consultado");
				// System.exit(1);
				return null;
			}
			if (!respuesta.getNameServers().isEmpty()) {
				if (respuesta.getNameServers().get(0).getRRType().equals(RRType.SOA)) {
					System.out.println("La respuesta es de tipo SOA");
					return null;
				}

			}
			if (!(respuesta.getAnswers().isEmpty())) {
				if (respuesta.getAnswers().get(0).getRRType().equals(RRType.CNAME)) {
					DomainName consultas = ((CNAMEResourceRecord) respuesta.getAnswers().get(0)).getNs();
					System.out.println("A " + ServerPregunta.toString().substring(1, ServerPregunta.toString().length())
							+ " CNAME" + " " + consultas.toString());

					ServerPregunta = noDNS(consultas, ipOriginal, tipo,modoConexion);
					if (ServerPregunta == null) {

						continue siguiente;
					}

					// break;
				} else {
					answer(respuesta, ServerPregunta);
					if (tipo.equals(RRType.A)) {
						ip = ((AResourceRecord) (respuesta.getAnswers().get(0))).getAddress();

						return ip;
					}
					return null;
				}
			} else if (!respuesta.getAnswers().isEmpty() && !respuesta.getNameServers().isEmpty()
					&& respuesta.getNameServers().get(0).getRRType().equals(RRType.SOA)) {
				System.out.println("La respuesta es de tipo SOA");
				return null;
			}

			else if ((!respuesta.getNameServers().isEmpty() && !respuesta.getAdditonalRecords().isEmpty())) {
				boolean a = false;

				bucle: for (int b = 0; b < respuesta.getNameServers().size(); b++) {
					for (int i = 0; i < respuesta.getAdditonalRecords().size(); i++) {
						if (((respuesta.getAdditonalRecords().get(i))) instanceof AResourceRecord) {
							if (((NSResourceRecord) respuesta.getNameServers().get(b)).getNS().toString()
									.equals(((AResourceRecord) respuesta.getAdditonalRecords().get(i)).getDomain()
											.toString())) {
								if (!dnsConsultados
										.contains(((AResourceRecord) (respuesta.getAdditonalRecords().get(i)))
												.getAddress().toString())) {

									System.out.println("A "
											+ ServerPregunta.toString().substring(1, ServerPregunta.toString().length())
											+ " " + respuesta.getNameServers().get(b).getRRType() + " "
											+ respuesta.getNameServers().get(b).getTTL() + " "
											+ ((NSResourceRecord) (respuesta.getNameServers().get(b))).getNS());
									System.out.println("A "
											+ ServerPregunta.toString().substring(1, ServerPregunta.toString().length())

											+ " " + respuesta.getAdditonalRecords().get(i).getRRType() + " "

											+ respuesta.getNameServers().get(b).getTTL() + " "
											+ ((AResourceRecord) (respuesta.getAdditonalRecords().get(i))).getAddress()
													.toString().substring(1,
															((AResourceRecord) (respuesta.getAdditonalRecords().get(i)))
																	.getAddress().toString().length()));
									ServerPregunta = ((AResourceRecord) (respuesta.getAdditonalRecords().get(i)))
											.getAddress();

									dnsConsultados.add(ServerPregunta.toString());
									ip = ServerPregunta;
									a = true;
									break bucle;
								}

							}
						}
					}
					
				}
				if (a == false) {
					System.out.println("no se ha encontrado un dns valido");
					break;
				}
			}

			else if (respuesta.getAdditonalRecords().isEmpty() && !respuesta.getNameServers().isEmpty()) {
				
				
				System.out.println("No hay registro tipo A en seccion ADDITIONAL para " + ((NSResourceRecord) respuesta.getNameServers().get(0)).getNS());
				ServerPregunta = noDNS(((NSResourceRecord) respuesta.getNameServers().get(0)).getNS(), ipOriginal,
						tipo,modoConexion);
				
				continue;
			}
			else {
				System.out.println("No hay respuesta");
				return null;
			}

		}

		return null;

	}

}
