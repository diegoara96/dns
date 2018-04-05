
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import es.uvigo.det.ro.simpledns.*;

public class dnsclient {

	public static void main(String[] args) throws IOException {
		if(args.length!=2) {
			System.out.println("numero de argumentos incorrecto");
		}
		args[0]=args[0].toLowerCase();
		System.out.println(args[0]);
		if(!(args[0].equals("-t")||args[0].equals("-u"))) {
			 System.out.println("modo de conexion distinto de -(t|u)");
		}
		 if (args[0].equals("-t")){
			System.out.println("No se halla implementado el uso de TCP. Se usará UDP");
			args[0]="-u";
		}
		
		BufferedReader algo = new BufferedReader(new InputStreamReader(System.in));
		String dominio=algo.readLine();
		
		do {
			
		System.out.println(dominio);
		
		
		 
		
		
		
		
		
		
		
		
		}while((dominio=algo.readLine())!= null);
		
		
	
		
	}
	

}
