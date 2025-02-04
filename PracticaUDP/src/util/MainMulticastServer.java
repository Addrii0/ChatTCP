package util;

import hilos.HiloServer;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainMulticastServer {
    private static final int PUERTO = 23002;
    private static final String DIRECCION = "237.0.0.1";
    private static Set<String> nombresClientes = new HashSet<>();
    private static List<String> historialMensajes = new ArrayList<>();  // Almacena los mensajes
    private static Set<HiloServer> usuarios = new HashSet<>();
    private static MulticastSocket servidor;

    public static void main(String[] args) {
        try {
            servidor = new MulticastSocket(PUERTO);
            InetAddress direccion = InetAddress.getByName(DIRECCION);
            servidor.joinGroup(direccion);

            System.out.println("Servidor escuchando en el puerto " + PUERTO);

            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket paqueteRecibido = new DatagramPacket(buffer, buffer.length);
                servidor.receive(paqueteRecibido);

                String mensaje = new String(paqueteRecibido.getData(), 0, paqueteRecibido.getLength());
                System.out.println("Mensaje recibido: " + mensaje);

                // Guardar mensaje en historial
                historialMensajes.add(mensaje);

                // Retransmitir mensaje a todos los clientes
                DatagramPacket paqueteEnviar = new DatagramPacket(
                        mensaje.getBytes(),
                        mensaje.length(),
                        grupo,
                        PUERTO
                );
                socket.send(paqueteEnviar);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Controlar nombre único
    public static synchronized boolean registrarNombre(String nombre) {
        if (nombresClientes.contains(nombre)) {
            return false;  // Nombre ya en uso
        }
        nombresClientes.add(nombre);
        return true;
    }

    // Eliminar cliente
    public static synchronized void eliminarCliente(HiloServer cliente, String nombre) {
        usuarios.remove(cliente);
        nombresClientes.remove(nombre);
    }

    // Enviar mensaje a todos los clientes
    public static synchronized void broadcast(String mensaje, HiloServer servidor) {
        historialMensajes.add(mensaje);  // Almacenar mensaje en historial
        byte[] buffer = mensaje.getBytes();
        for (HiloServer cliente : usuarios) {
            if (cliente != servidor) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(DIRECCION), PUERTO);
                    multicastSocket.send(packet);  // Enviar mensaje a todos los clientes mediante multicast
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Mandar mensajes anteriores a un cliente nuevo
    public static synchronized void enviarMensajesHistorial(HiloServer cliente) {
        for (String mensaje : historialMensajes) {
            cliente.enviarMensaje(mensaje);
        }
    }
}