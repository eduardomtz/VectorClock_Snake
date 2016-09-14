package snake;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import multicast.Multicast;
import multicast.MulticastListener;

public class Frame extends JFrame {
        
        Multicast multicast;
	private static final long serialVersionUID = 1L;
        
        final JOptionPane optionPane;
        final JDialog dialog;
        
        final JOptionPane optionPaneGanaste;
        final JDialog dialogGanaste;
        
        final JLabel txtVector;
                
        final Screen screen;
        
        private double score = 0.0;
        
        private DefaultListModel jugadores;
        
        private String EsteJugador = "";
        
        private boolean NadieHaGanado = true;
        
	public Frame() {
            jugadores = new DefaultListModel();
            txtVector = new JLabel("Vector Clock {}");
            screen = new Screen();

            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setTitle("Snake Game Multiplayer"); // :D
            setResizable(false);

            // DIALOGO VENTANA REINICIAR
            optionPane = new JOptionPane(
                        "Quieres volver a jugar?",
                        JOptionPane.QUESTION_MESSAGE,
                        JOptionPane.YES_NO_OPTION);

            dialog = new JDialog(this, 
                 "Click a button",
                 true);
            dialog.setLocationRelativeTo(this);
            dialog.setContentPane(optionPane);
            
            dialog.setFocusable(false);
            dialog.setVisible(false);
            dialog.pack();

            optionPane.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent e) {
                        String prop = e.getPropertyName();
                        if (dialog.isVisible() 
                         && (e.getSource() == optionPane)
                         && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                            //If you were going to check something
                            //before closing the window, you'd do
                            //it here.
                            dialog.setVisible(false);
                        }
                    }
                });
            
            // DIALOGO GANASTE
            optionPaneGanaste = new JOptionPane(
                        "GANASTE!!!",
                        JOptionPane.INFORMATION_MESSAGE,
                        JOptionPane.OK_OPTION);

            dialogGanaste = new JDialog(this, 
                 "Click a button",
                 true);
            dialogGanaste.setLocationRelativeTo(this);
            dialogGanaste.setContentPane(optionPaneGanaste);
            
            dialogGanaste.setFocusable(false);
            dialogGanaste.setVisible(false);
            dialogGanaste.pack();
            
            
            // CREAR ID UNICO PARA ESTE PROCESO
            if(EsteJugador.isEmpty())
            {
                Random r = new Random();
                Integer i = r.nextInt(1000);
                EsteJugador = i.toString();
            }
            
            multicast = new Multicast(EsteJugador);

            multicast.addListener(new MulticastListener(){
                @Override
                public void MensajeRecibido(String mensaje) {

                    if(mensaje.contains("Coordenadas")){
                        String cordx = mensaje.substring(mensaje.indexOf("(") + 1, mensaje.indexOf(","));
                        String cordy = mensaje.substring(mensaje.indexOf(",") + 1, mensaje.indexOf(")"));
                        
                        int x = Integer.parseInt(cordx);
                        int y = Integer.parseInt(cordy);
                        
                        screen.generarManzana(x, y);
                    }
                    else if(mensaje.contains("Inicia")){
                        String cordx = mensaje.substring(mensaje.indexOf("(") + 1, mensaje.indexOf(","));
                        String cordy = mensaje.substring(mensaje.indexOf(",") + 1, mensaje.indexOf(")"));
                        
                        int x = Integer.parseInt(cordx);
                        int y = Integer.parseInt(cordy);
                        
                        screen.generarManzana(x, y);
                        screen.setFocusable(true);
                        screen.start();
                        
                        String conectado = String.format("Jugador (" + EsteJugador + ") conectado");
                        multicast.enviarMensaje(conectado);

                    }
                    else if(mensaje.contains("Jugador")){
                        // conectado
                        String jugador = mensaje.substring(mensaje.indexOf("(") + 1, mensaje.indexOf(")"));
                        if(!jugadores.contains(jugador))
                            jugadores.addElement(jugador);
                    }
                    else if(mensaje.contains("Salio")){
                        String jugador = mensaje.substring(mensaje.indexOf("(") + 1, mensaje.indexOf(")"));
                        
                        if(!jugador.equals(EsteJugador)){
                            // si no es este jugador, borrar de la lista
                            System.out.println("Jugador salio " + jugador);
                            jugadores.removeElement(jugador);
                            
                            if(jugadores.size() == 1)
                            {
                                if(NadieHaGanado)
                                {
                                    String conectado = String.format("GANO (" + EsteJugador + ")");
                                    multicast.enviarMensaje(conectado);
                                    
                                    System.out.println("GANASTE");
                                    dialogGanaste.setVisible(true);                            
                                }
                            }
                        }
                    }
                    else if(mensaje.contains("GANO")){
                        String jugador = mensaje.substring(mensaje.indexOf("(") + 1, mensaje.indexOf(")"));
                        if(!jugador.equals(EsteJugador)){
                            NadieHaGanado = false;
                        }
                    }
                }
                
                @Override
                public void EnviarVector(String vector) {
                    // Vector Clock 
                    vector = "Vector Clock " + vector;
                    txtVector.setText(vector);
                }
            });
            init();
            multicast.recibirMensaje();
                
	}

	public void init() {
		setLayout(new GridBagLayout());
                
                JButton btIniciar = new JButton("Iniciar");
                
                JLabel txtPuntos = new JLabel("Puntos: ");
                JLabel txtConectados = new JLabel("Jugadores ID:");
                
                JLabel txtProceso = new JLabel("Process ID: " + EsteJugador);
                //JLabel txtVector = new JLabel("Vector Clock {}");
                
                JList list = new JList(jugadores);
                JScrollPane jug = new JScrollPane(list);
                jug.setPreferredSize(new Dimension(80, 80));
                
                list.setFocusable(false);
                jug.setFocusable(false);
                btIniciar.setFocusable(false);
                txtPuntos.setFocusable(false);
                txtConectados.setFocusable(false);
                txtVector.setFocusable(false);
                
                btIniciar.addActionListener(new ActionListener(){
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        
                        Random r = new Random();
                        int x = r.nextInt(screen.GAME_FIELD_ROLS);
                        int y = r.nextInt(screen.GAME_FIELD_COLS);

                        String mensaje = String.format("Inicia el juego (%d,%d)", x,y);

                        multicast.enviarMensaje(mensaje);
                        
                        btIniciar.setVisible(false);
                        
                    }
                    
                });
                
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 1;
                gbc.gridy = 2;
                gbc.weightx = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                this.add(btIniciar, gbc);
                
                
                gbc = new GridBagConstraints();
                gbc.gridx = 1;
                gbc.gridy = 1;
                gbc.weightx = 1;
                gbc.fill = GridBagConstraints.VERTICAL;
                this.add(txtPuntos, gbc);
                
                
                        
                gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 3;
                gbc.weightx = 1;
                gbc.fill = GridBagConstraints.VERTICAL;
                this.add(txtProceso, gbc);
                        
                gbc = new GridBagConstraints();
                gbc.gridx = 1;
                gbc.gridy = 3;
                gbc.weightx = 1;
                gbc.fill = GridBagConstraints.VERTICAL;
                this.add(txtVector, gbc);
                
                gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.weightx = 1;
                gbc.fill = GridBagConstraints.CENTER;
                this.add(txtConectados, gbc);
                
                gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 2;
                gbc.weighty = 1;
                gbc.fill = GridBagConstraints.VERTICAL;
                this.add(jug, gbc);
                
                gbc = new GridBagConstraints();
                gbc.gridx = 1;
                gbc.gridy = 2;
                gbc.weightx = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                this.add(screen, gbc);
                
                pack();

		setLocationRelativeTo(null);
		setVisible(true);
                
                screen.addListener(new SnakeListener(){
                    @Override
                    public void ComioManzana() {
                        // System.out.println("Comio manzana");
                        // multicast.enviarMensaje("Comio manzana");
                        
                        Random r = new Random();
                        int x = r.nextInt(screen.GAME_FIELD_ROLS);
                        int y = r.nextInt(screen.GAME_FIELD_COLS);
                        
                        String mensaje = String.format("Coordenadas (%d,%d)", x,y);
                        multicast.enviarMensaje(mensaje);
                        
                        score ++; // screen.snakeSpeed / screen.speedSstep;
                        
                        String strscore = String.format("Puntos: %2.0f", score);
                        txtPuntos.setText(strscore);
                    }

                    @Override
                    public void inicio() {
                        System.out.println("Inicio ");
                        score = 0.0;
                        String strscore = String.format("Puntos: %2.2f", score);
                        txtPuntos.setText(strscore);
                        
                        btIniciar.setVisible(false);
                        
                        dialog.setVisible(false);
                        
                        NadieHaGanado = true;
                    }

                    @Override
                    public void seDetuvo() {
                        System.out.println("se Detuvo ");
                        
                        String mensaje = String.format("Salio (" + EsteJugador + "), puntos [ %2.0f ]", score);
                        multicast.enviarMensaje(mensaje);
                        
                        dialog.setVisible(true);
                        
                        int value = ((Integer)optionPane.getValue()).intValue();
                        
                        if (value == JOptionPane.YES_OPTION) {
                            screen.start();
                            // multicast.recibirMensaje();
                        } else if (value == JOptionPane.NO_OPTION) {
                            screen.terminate();
                            dispose();
                        }
                        
                    }
                });
                
	}

	public static void main(String[] args) {
		new Frame();
	}
        
        
}