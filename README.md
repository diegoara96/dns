# Dns

Proyecto realizado en el marco de la asignatura de Redes de Ordenadores de Ingenieria de Telecomunicaciones. 
Se trata de una aplicación que actua como servidor dns cuya entrada será el tipo de conexion -t (TCP) o -u (UDP) y un servidor dns raiz


### Iniciar

```bash
java -t 198.41.0.4
```

```java
TipoConsulta Servidor
```
Ejemplo
```java
A ww.google.es
```

## Tipos de consultas DNS admitidas: 

<ul>
  <li><b>Dirección de host IPv4:</b> A
  <li><b>Dirección de host IPv6:</b> AAAA
  <li><b>Servidor de nombre de autorización:</b> NS
  <li><b>Dirección de servidor de correo:</b> MX
  <li><b>Nombre canónico para un alias:</b> CNAME
  <li><b>Cadenas de caracteres de texto:</b> TXT
  <li><b>Puntero de nombre de dominio:</b> PTR
  </ul>


## Ejemplo de salida con entrada A www.google.es

Q TCP 198.41.0.4 A www.google.es<br>
A 198.41.0.4 NS 172800 g.nic.es.<br>
A 198.41.0.4 A 172800 204.61.217.1<br>
Q TCP 204.61.217.1 A www.google.es<br>
A 204.61.217.1 NS 86400 ns1.google.com.<br>
No hay registro tipo A en seccion ADDITIONAL para ns1.google.com.<br>
Q UDP 198.41.0.4 A ns1.google.com.<br>
A 198.41.0.4 NS 172800 e.gtld-servers.net.<br>
A 198.41.0.4 A 172800 192.12.94.30<br>
Q UDP 192.12.94.30 A ns1.google.com.<br>
A 192.12.94.30 NS 172800 ns2.google.com.<br>
A 192.12.94.30 A 172800 216.239.34.10<br>
Q UDP 216.239.34.10 A ns1.google.com.<br>
A 198.41.0.4 345600 216.239.32.10<br>
Q TCP 216.239.32.10 A www.google.es<br>
A 216.239.32.10 300 172.217.17.3<br>



## Ejemplo de salida con entrada PTR 1.0.0.1

Q CACHE CACHE PTR 1.0.0.1.in-addr.arpa.<br>
La cache no tiene la respuesta<br>
Q TCP 198.41.0.4 PTR 1.0.0.1.in-addr.arpa.<br>
A 198.41.0.4 NS 172800 e.in-addr-servers.arpa.<br>
A 198.41.0.4 A 172800 203.119.86.101<br>
Q TCP 203.119.86.101 PTR 1.0.0.1.in-addr.arpa.<br>
A 203.119.86.101 NS 86400 tinnie.arin.net.<br>
No hay registro tipo A en seccion ADDITIONAL para tinnie.arin.net.<br>
Q TCP 198.41.0.4 A tinnie.arin.net.<br>
A 198.41.0.4 NS 172800 a.gtld-servers.net.<br>
A 198.41.0.4 A 172800 192.5.6.30<br>
Q TCP 192.5.6.30 A tinnie.arin.net.<br>
A 192.5.6.30 NS 172800 ns1.arin.net.<br>
A 192.5.6.30 A 172800 199.212.0.108<br>
Q TCP 199.212.0.108 A tinnie.arin.net.<br>
A 199.212.0.108 43200 199.212.0.53<br>
Q TCP 199.212.0.53 PTR 1.0.0.1.in-addr.arpa.<br>
A 199.212.0.53 NS 86400 ns3.cloudflare.com.<br>
No hay registro tipo A en seccion ADDITIONAL para ns3.cloudflare.com.<br>
Q TCP 198.41.0.4 A ns3.cloudflare.com.<br>
A 198.41.0.4 NS 172800 a.gtld-servers.net.<br>
A 198.41.0.4 A 172800 192.5.6.30<br>
Q TCP 192.5.6.30 A ns3.cloudflare.com.<br>
A 192.5.6.30 NS 172800 ns3.cloudflare.com.<br>
A 192.5.6.30 A 172800 162.159.0.33<br>
Q TCP 162.159.0.33 A ns3.cloudflare.com.<br>
A 162.159.0.33 900 162.159.7.226<br>
A 162.159.0.33 900 162.159.0.33<br>
Q TCP 162.159.7.226 PTR 1.0.0.1.in-addr.arpa.<br>
A /162.159.7.226 1800 ne.one.one.one.<br>



## Authors

* **Diego Araujo ** - *Diseñador y Desarrollador* - [diegoara96](https://github.com/diegoara96)

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details
