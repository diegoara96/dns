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

	// administra tcp/udp posible implementacion de respuestas truncadas cuando sepa
	// como detectarlas y un ejemplo de ellas
	
	//de no ir cambiar la llamada al bloque try de udp
	public static Message envio(Message consulta, Inet4Address direccion_ip, String modoConexion) {
		if (modoConexion.equals("UDP")) {
			try {
				return envioUDP(consulta, direccion_ip);
			}catch(ExceptionTruncada e) {
				return envioTCP(consulta, direccion_ip);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		} else if (modoConexion.equals("TCP")) {
			return envioTCP(consulta, direccion_ip);
		} else {
			return null;
		}
	}

	// envia udp
	public static Message envioUDP(Message consulta, Inet4Address direccion_ip)throws Exception {

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
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return respuesta;
	}

	// envia tcp
	public static Message envioTCP(Message consulta, Inet4Address direccion_ip) {
		int puerto = 53;
		Message respuesta = null;
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			// creamos el socket TCP

			Socket socketCliente = new Socket();
			socketCliente.setSoTimeout(1000);
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

		}catch (SocketTimeoutException e) {
			
			return null;

		} catch (ConnectException e) {
			
			System.out.println("El servidor rechaza TCP");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return respuesta;
	}

}
