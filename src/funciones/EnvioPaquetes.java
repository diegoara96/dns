package funciones;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import es.uvigo.det.ro.simpledns.*;
public class EnvioPaquetes {

	
	
	
	public static Message envioUDP(Message consulta,Inet4Address direccion_ip)  {
		
					   int puerto = 53;
						
						
						DatagramSocket socketUDP;
						Message respuesta=null;
						try {
							socketUDP = new DatagramSocket();
						
						socketUDP.setSoTimeout(6000);
						//pasamos la cadena a bytes
						byte[] mensaje = consulta.toByteArray();
						//sacamos la ip del nombre del servidor
						InetAddress servidor = direccion_ip;

						//creamos el datagrama con los elemntos introducidos
						DatagramPacket peticionServidor = new DatagramPacket(mensaje, mensaje.length, servidor, puerto);

						//enviamos el datagrama con los datos introducidos
						socketUDP.send(peticionServidor);

						//creamos el datagrama para la respuesta del servidor
						byte[] almacen = new byte[1000];
						
						DatagramPacket respuestaServidor = new DatagramPacket(almacen, almacen.length);
						
						try {
							//recivimos el paquete, en caso de que el tiempo de espera exceda el time out sacamos excepcion y cerramos socket
							socketUDP.receive(respuestaServidor);

							
							 respuesta = new Message(respuestaServidor.getData());
					
						 
						} catch (SocketTimeoutException e) {
						//	System.out.println("Tiempo de espera excedido,compruebe los datos de conexión");
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
