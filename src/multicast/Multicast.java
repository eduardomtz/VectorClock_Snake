/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import vectorclock.VectorClock;

/**
 *
 * @author EMARTINENE
 */
public class Multicast {
    
    private ArrayList<MulticastListener> listeners = new ArrayList<MulticastListener>();
    private ArrayDeque<String> colaMensajes;
    private VectorClock vectorClock;
    MulticastSocket s = null;
        
    // 224.0.0.0 a 239.255.255.255
    // primera esta reservada
    
    private String group = "225.4.5.7";
    private int port = 5000;
    private String processID;
    
    // Which ttl
    int ttl = 1;
    
    public Multicast(String id){
        vectorClock = new VectorClock();
        colaMensajes = new ArrayDeque<String>();
        processID = id;
    }
    
    // OBSERVABLE PATTERN UI
    public void addListener(MulticastListener toAdd) {
        listeners.add(toAdd);
    }
    
    public void recibirMensaje(){
        
        VectorClock vClockRecibido = new VectorClock();
   	try {
                s = new MulticastSocket(port);
	   	s.joinGroup(InetAddress.getByName(group)); 

	    	byte[] buffer = new byte[1024];
                                
 	   	while(true){
                    System.out.println("Waiting for messages");
                    DatagramPacket messageIn = 
			new DatagramPacket(buffer, buffer.length);
 		    s.receive(messageIn);
                    
                    
                    String mensaje = new String(messageIn.getData());
                    String address = messageIn.getAddress().getHostAddress();
                    
                    if(!mensaje.isEmpty()){
                        // AL RECIBIR EL MENSAJE, COLOCARLO EN UNA COLA
                            colaMensajes.push(mensaje);

                        // OBTENER VECTOR CLOCK DEL MENSAJE
                        String pIDEnvio = mensaje.substring(mensaje.indexOf("¡") + 1, mensaje.indexOf("!"));
                        String vectorEnvio = mensaje.substring(mensaje.indexOf("{") + 1, mensaje.indexOf("}"));

                        // otros elementos menores que
                        boolean otrosMenores = false;
                        String[] valores = vectorEnvio.split(",");
                        
                        
                        for(String v:valores){
                            String[] llavevalor = v.split("=");
                            llavevalor[0] = llavevalor[0].trim();
                            llavevalor[1] = llavevalor[1].trim();
                            vClockRecibido.set(llavevalor[0], Integer.parseInt(llavevalor[1]));
                        }
                        
                        
                        
                        boolean esMismoProceso = false;
                        boolean sinElementos = false;
                        if(!pIDEnvio.equals(processID))
                        {
                            Integer[] mio = vectorClock.getOrderedValues(pIDEnvio);
                            Integer[] otros = vClockRecibido.getOrderedValues(pIDEnvio);
                            
                            if(mio != null && otros != null && (  mio.length > 0 && otros.length > 0) )
                            {
                                for(int i=0; i<mio.length; i++){
                                    if(i < otros.length)
                                    {
                                        if(mio[i] != null && otros[i] != null)
                                        {
                                            if(mio[i] <= otros[i])
                                            {
                                                otrosMenores = true;
                                            }
                                        }
                                        else
                                        {
                                            sinElementos = true;
                                        }
                                    }
                                }
                            }
                            else
                            {
                                sinElementos = true;
                            }
                        }
                        else
                            esMismoProceso = true;

                        // ESPERAR HASTA QUE
                        // 1) EL ELEMENTO DEL PROCESO j QUE ENVIO EL MENSAJE SEA IGUAL AL DEL VECTOR + 1
                        // Vi[j] = Vj[j]+1
                        // 2) LOS DEMAS ELEMENTOS SEAN <= QUE LOS ELEMENTOS DE ESTE VECTOR
                        if(vectorClock.get(pIDEnvio) + 1  == vClockRecibido.get(pIDEnvio)
                                && otrosMenores || esMismoProceso || sinElementos)
                        {
                            // SACAR DE LA COLA
                            mensaje = colaMensajes.poll();

                            // MENSAJE RECIBIDO
                            System.out.println("Message CO: " + mensaje );
                            System.out.println("From: " + address);
                            if(listeners.size()>0){
                                for(MulticastListener l : listeners){
                                    l.MensajeRecibido(mensaje);
                                }
                            }
                            
                            // AL CUMPLIRSE LAS CONDICIONES Vi[j]++;
                            if(!esMismoProceso)
                            {
                                vectorClock.incrementClock(pIDEnvio);
                            }
                            
                            if(listeners.size()>0){
                                for(MulticastListener l : listeners){
                                    l.EnviarVector(vectorClock.toString());
                                }
                            }
                        }
                    }
                    
                    if(mensaje.equals("_EXIT_"))
                        break;
                }
	    	s.leaveGroup(InetAddress.getByName(group));
         }
         catch (SocketException e){
             System.out.println("Socket: " + e.getMessage());
	 }
         catch (IOException e){
             System.out.println("IO: " + e.getMessage());
         }
	 finally {
            s.close();
        }
    }
     
    public void enviarMensaje(String mensaje){
        try {
            MulticastSocket enviar = new MulticastSocket();
            
            // INCREMENTAR RELOJ
            String pID = String.format("¡" + processID + "!");
            
            vectorClock.incrementClock(processID);
            
            mensaje = mensaje.concat(vectorClock.toString());
                    
            // AGREGAR VECTOR CLOCK EN EL MENSAJE
            pID = pID.concat(mensaje);
            
            // Fill the buffer with some data
            byte[] buf = pID.getBytes();
            // Create a DatagramPacket 
            DatagramPacket pack = new DatagramPacket(buf, buf.length,
                                                     InetAddress.getByName(group), port);
            // enviar.getTimeToLive(10); // un tiempo para el mensaje
            
            enviar.send(pack);
            enviar.close();
        
        } catch (IOException ex) {
            Logger.getLogger(Multicast.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
