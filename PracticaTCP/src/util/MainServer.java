package util;

import hilos.HiloCliente;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class MainServer  {
    private static Set<String> nombresClientes = new HashSet<>();
    private static Set<HiloCliente> clientes = new HashSet<>();
    private static int puerto = 6001;


    public static void main(String[] args) {
        Thread hilo;
        try (ServerSocket servidor = new ServerSocket(puerto)) {
            System.out.println("Servidor de chat escuchando en el puerto " + puerto);

            while (true) {
                Socket cliente = servidor.accept();
                hilo = new Thread(new HiloCliente(cliente));
                hilo.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metodo sincronizado para verificar y registrar un nuevo nombre
    public static synchronized boolean registrarNombre(String nombre) {
        if (nombresClientes.contains(nombre)) {
            return false; // Nombre ya en uso
        }else {
            nombresClientes.add(nombre);
            return true;
        }
    }

    // Metodo para eliminar un cliente cuando se desconecta
    public static synchronized void eliminarCliente(HiloCliente cliente, String nombre) {
        clientes.remove(cliente);
        nombresClientes.remove(nombre);
    }

    // Metodo para enviar mensajes a todos los clientes
    public static synchronized void broadcast(String mensaje, HiloCliente remitente) {
        for (HiloCliente cliente : clientes) {
            if (cliente != remitente) {
                cliente.enviarMensaje(mensaje);
            }
        }
    }
    public static synchronized void agregarCliente(HiloCliente cliente) {
        clientes.add(cliente);
    }
}

