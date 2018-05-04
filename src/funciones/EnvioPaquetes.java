package funciones;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import es.uvigo.det.ro.simpledns.*;

public class EnvioPaquetes {

	/**
	 * funcion que administra el tipo de conexion, en caso de respuests truncada
	 * cambia de udp a tcp
	 * 
	 * @param consulta
	 * @param direccion_ip
	 * @param modoConexion
	 * @return
	 */
	public static Message envio(Message consulta, Inet4Address direccion_ip, String modoConexion) {
		if (modoConexion.equals("UDP")) {
			try {
				int contador = 0;
				Message respuesta;
				do {
					contador++;
					respuesta = envioUDP(consulta, direccion_ip);
					// solo reintentamos udp, se suelen perder a veces algun paquete
				} while (contador < 3 && respuesta == null);
				return respuesta;
			} catch (ExceptionTruncada e) {
				System.out.println("Se ha recibido un paquete truncado se usará TCP en esta consulta");
				System.out.println("Q " + "TCP" + " "
						+ direccion_ip.toString().substring(1) + " "
						+ consulta.getQuestionType() + " " + consulta.getQuestion());
				return envioTCP(consulta, direccion_ip);
			} catch (Exception e) {

				e.printStackTrace();
				return null;
			}
		} else if (modoConexion.equals("TCP")) {
			return envioTCP(consulta, direccion_ip);
		} else {
			return null;
		}
	}

	/**
	 * envia un mensaje a un servidor dns usando UDP
	 * 
	 * @param consulta
	 * @param direccion_ip
	 * @return Message
	 * @throws Exception
	 */
	public static Message envioUDP(Message consulta, Inet4Address direccion_ip) throws Exception {

		int puerto = 53;

		DatagramSocket socketUDP;
		Message respuesta = null;
		try {
			socketUDP = new DatagramSocket();

			socketUDP.setSoTimeout(4000);
			// pasamos la cadena a bytes
			byte[] mensaje = consulta.toByteArray();
			// sacamos la ip del nombre del servidor
			InetAddress servidor = direccion_ip;

			// creamos el datagrama con los elemntos introducidos
			DatagramPacket peticionServidor = new DatagramPacket(mensaje, mensaje.length, servidor, puerto);

			// enviamos el datagrama con los datos introducidos
			socketUDP.send(peticionServidor);

			// creamos el datagrama para la respuesta del servidor
			byte[] almacen = new byte[1000];

			DatagramPacket respuestaServidor = new DatagramPacket(almacen, almacen.length);

			try {
				// recivimos el paquete, en caso de que el tiempo de espera exceda el time out
				// sacamos excepcion y cerramos socket
				socketUDP.receive(respuestaServidor);

				respuesta = new Message(respuestaServidor.getData());

			} catch (SocketTimeoutException e) {

			}
			// Cerramos el socket
			socketUDP.close();

		} catch (SocketException e1) {

			e1.printStackTrace();
		} catch (IOException e1) {

			e1.printStackTrace();
		}

		return respuesta;
	}

	/**
	 * envia un mensaje a un servidor dns usando TCP
	 * 
	 * @param consulta
	 * @param direccion_ip
	 * @return Message
	 */
	public static Message envioTCP(Message consulta, Inet4Address direccion_ip) {
		int puerto = 53;
		Message respuesta = null;
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			// creamos el socket TCP vacio

			Socket socketCliente = new Socket();
			socketCliente.setSoTimeout(1000);
			// conectamos el socket al servidor, asi si no conecta sale antes que con la
			// excepcion del connect exception
			socketCliente.connect(new InetSocketAddress(direccion_ip, puerto), 3000);

			InputStream entrada = socketCliente.getInputStream();

			DataInputStream entradaBytes = new DataInputStream(entrada);
			DataOutputStream salidabytes = new DataOutputStream(socketCliente.getOutputStream());
			// creamos objeto para la salida de los datos hacia el servidor

			// en el caso de tener en la invocacion una palabra a buscar
			byte mensaje[] = consulta.toByteArray();
			byte longitud[] = Utils.int16toByteArray(mensaje.length);
			os.write(longitud);
			os.write(mensaje);
			salidabytes.write(os.toByteArray());
			ByteArrayOutputStream al = new ByteArrayOutputStream();
			int longTotal = entradaBytes.readUnsignedShort();

			for (int i = 0; i < longTotal; i++) {
				al.write(entradaBytes.readByte());

			}

			respuesta = new Message(al.toByteArray());

			socketCliente.close();

		} catch (SocketTimeoutException e) {

			return null;

		} catch (ConnectException e) {

			System.out.println("El servidor rechaza TCP");

		} catch (IOException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}
		return respuesta;
	}

}
