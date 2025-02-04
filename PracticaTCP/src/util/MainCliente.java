package util;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class MainCliente extends JFrame {

    private JTextArea areaTexto;
    private JTextField campoMensaje;
    private JButton btnEnviar, btnSalir;
    private DataInputStream entrada;
    private DataOutputStream salida;
    private Socket socket;
    private String nombreUsuario;

    public MainCliente() {
        configurarInterfaz();
        conectarAlServidor();
    }

    private void configurarInterfaz() {
        setTitle("Chat Cliente");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        areaTexto = new JTextArea();
        areaTexto.setEditable(false);
        add(new JScrollPane(areaTexto), BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout());
        campoMensaje = new JTextField();
        btnEnviar = new JButton("Enviar");
        btnSalir = new JButton("Salir");

        panelInferior.add(campoMensaje, BorderLayout.CENTER);
        panelInferior.add(btnEnviar, BorderLayout.EAST);
        panelInferior.add(btnSalir, BorderLayout.WEST);
        add(panelInferior, BorderLayout.SOUTH);

        btnEnviar.addActionListener(e -> enviarMensaje());
        btnSalir.addActionListener(e -> salirDelChat());

        setVisible(true);
    }

    private void conectarAlServidor() {
        try {
            socket = new Socket("localhost", 6001);
            entrada = new DataInputStream(socket.getInputStream());
            salida = new DataOutputStream(socket.getOutputStream());

            while (true) {
                nombreUsuario = JOptionPane.showInputDialog(this, "Ingresa tu nombre:");
                if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) continue;
                salida.writeUTF(nombreUsuario);
                String respuesta = entrada.readUTF();
                if (respuesta.equals("ACEPTADO")) break;
                JOptionPane.showMessageDialog(this, "❌ Nombre en uso. Intenta otro.");
            }

            Thread hiloEscucha = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String mensaje = entrada.readUTF();
                            areaTexto.append(mensaje + "\n");
                        }
                    } catch (IOException e) {
                        System.out.println("Desconectado del servidor.");
                    }
                }
            });
            hiloEscucha.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enviarMensaje() {
        try {
            String mensaje = campoMensaje.getText();
            if (!mensaje.isEmpty()) {
                salida.writeUTF(mensaje);
                campoMensaje.setText("");
                SwingUtilities.invokeLater(() -> areaTexto.append(nombreUsuario + ": " +mensaje + "\n"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void salirDelChat() {
        try {
            salida.writeUTF("/salir");
            socket.close();
            dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new MainCliente();
    }
}