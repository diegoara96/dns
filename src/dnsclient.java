
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
			System.out.println("Direcion ip DNS in correcta");
			System.exit(1);
		}
		for (int i = 0; i < 4; i++) {

			byte b = (byte) Integer.parseInt(ipCachos[i]);
			ip[i] = (byte) (b & 0xFF);

		}
		Inet4Address ServerPregunta = (Inet4Address) InetAddress.getByAddress(ip);

		BufferedReader algo = new BufferedReader(new InputStreamReader(System.in));
		String entrada;

		while ((entrada = algo.readLine()) != null) {

			String partes[] = entrada.split("\\s+");
			if (partes.length != 2) {
				System.out.println("faltan parametros en la entrada");
				continue;
			}
			partes[0] = partes[0].toUpperCase().trim();
			if ((partes[0].equals("A") || partes[0].equals("NS"))) {
				
				Message inicial = new Message(partes[1], RRType.valueOf(partes[0]), false);
				int o = 0;
				ArrayList<String> dnsConsultados = new ArrayList<>();
				
				while (o < 10) {

					System.out.println("Q " + modoConexion + " "
							+ ServerPregunta.toString().substring(1, ServerPregunta.toString().length()) + " "
							+ inicial.getQuestionType() + " " + partes[1]);
					Message respuesta;
					do {
						respuesta = EnvioPaquetes.envioUDP(inicial, ServerPregunta);
					} while (respuesta == null);
					// System.out.println(respuesta.getQuestion());
					// System.out.println(respuesta.getAnswers().get(0).getRRType());
					if (!(respuesta.getAnswers().isEmpty())) {
						System.out.println(respuesta.getAnswers().size());
						if ((respuesta.getAnswers().get(0)) instanceof AResourceRecord) {
							System.out.println("A "
									+ ServerPregunta.toString().substring(1, ServerPregunta.toString().length()) + " "
									+ ((AResourceRecord) (respuesta.getAnswers().get(0))).getAddress().toString()
											.substring(1, ((AResourceRecord) (respuesta.getAnswers().get(0)))
													.getAddress().toString().length()));
						}
						if ((respuesta.getAnswers().get(0)) instanceof NSResourceRecord) {
							System.out.println(((NSResourceRecord) (respuesta.getAnswers().get(0))).getNS());
						}
						if (respuesta.getAnswers().get(0).getRRType().equals(RRType.CNAME)) {
							System.out.println("La respuesta es un CNAME");
						}
						break;
					} else if ((!respuesta.getNameServers().isEmpty() && !respuesta.getAdditonalRecords().isEmpty())) {
						boolean a = false;

						// for (int b = 0; b < respuesta.getNameServers().size(); b++) {
						// System.out.println(((NSResourceRecord)
						// (respuesta.getNameServers().get(b))).getNS());
						// }
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

											break;
										}

									}
								}
							}
							if (a = true)
								break;
						}
						if (a == false) {
							System.out.println("ni puta");
							System.exit(1);
						}
					} else {
						System.out.println("no hay respuesta");
						System.exit(1);
					}
					o++;
				}

				// System.out.println(((AResourceRecord)(respuesta.getAdditonalRecords().get(0))).getAddress().toString());

			} else {
				System.out.println("type no admitido");
				continue;
			}

		}

	}

}
