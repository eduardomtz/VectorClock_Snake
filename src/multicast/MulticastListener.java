/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multicast;

/**
 *
 * @author EMARTINENE
 */
public interface MulticastListener {
    // void listo();
    void MensajeRecibido(String mensaje);
    
    void EnviarVector(String vector);
}
