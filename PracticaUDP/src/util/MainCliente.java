package util;

import hilos.HiloCliente;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

import java.io.IOException;

public class MainCliente extends JFrame{
    private static final int PUERTO = 10002;
    private static final String DIRECCION = "237.0.0.1";  // Dirección multicast

    private JTextArea areaTexto;
    private JTextField campoMensaje;
    private JButton bEnviar, bSalir;
    private DataInputStream entrada;
    private DataOutputStream salida;
    private MulticastSocket usuario;
    private String nombreUsuario;
    private boolean disponible = false;

    public MainCliente() {
        configurarInterfaz();
        conectarAlServidor();
    }

    private void configurarInterfaz() {
        setTitle("Chat");
        setSize(400, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        areaTexto = new JTextArea();
        areaTexto.setEditable(false);
        add(new JScrollPane(areaTexto), BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout());
        campoMensaje = new JTextField();
        bEnviar = new JButton("Enviar");
        bSalir = new JButton("Salir");

        panelInferior.add(campoMensaje, BorderLayout.CENTER);
        panelInferior.add(bEnviar, BorderLayout.EAST);
        panelInferior.add(bSalir, BorderLayout.WEST);
        add(panelInferior, BorderLayout.SOUTH);

        bEnviar.addActionListener(e -> enviarMensaje());
        bSalir.addActionListener(e -> salirDelChat());

        setVisible(true);
    }

    private void conectarAlServidor() {
        try {
            usuario = new MulticastSocket(PUERTO);
            InetAddress direccion = InetAddress.getByName(DIRECCION);
            usuario.joinGroup(direccion);

            while (!disponible) {
                nombreUsuario = JOptionPane.showInputDialog(this, "Ingresa tu nombre:");
                salida.writeUTF(nombreUsuario);
                String respuesta = entrada.readUTF();
                if (!respuesta.contains("ya está en uso")){
                    disponible = true;
                }else {
                    JOptionPane.showMessageDialog(this, " Nombre en uso. Intenta otro.");
                }
            }
            // Iniciar hilo de recepción de mensajes
            HiloCliente hilo = new HiloCliente(entrada, areaTexto);
            Thread thread = new Thread(hilo);
            thread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enviarMensaje() {
        try {
            String mensaje = campoMensaje.getText();
            if (!mensaje.isEmpty()) {
                salida.writeUTF(mensaje);
                areaTexto.append("Tú: " + mensaje + "\n");
                campoMensaje.setText("");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void salirDelChat() {
        try {
            salida.writeUTF("/salir");
            usuario.close();
            dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new MainCliente();
    }
}