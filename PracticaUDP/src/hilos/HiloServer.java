package hilos;

import util.MainMulticastServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class HiloServer implements Runnable{

    private Socket cliente;
    private DataInputStream entrada;
    private DataOutputStream salida;
    private String nombreCliente;
    private boolean disponible = false;

    public HiloServer(Socket cliente) {
        this.cliente = cliente;
    }

    @Override
    public void run() {
        try {
            entrada = new DataInputStream(cliente.getInputStream());
            salida = new DataOutputStream(cliente.getOutputStream());

            // Solicitar nombre único
            while (!disponible) {
                salida.writeUTF("Ingrese su nombre:");
                nombreCliente = entrada.readUTF();

                if (MainMulticastServer.registrarNombre(nombreCliente)) {
                    salida.writeUTF("Bienvenido al chat, " + nombreCliente);
                    disponible = true;
                } else {
                    salida.writeUTF("El nombre ya está en uso, elige otro.");
                }
            }

            // Añadir cliente al conjunto
            MainMulticastServer.broadcast("🟢 " + nombreCliente + " se ha unido al chat", this);
            MainMulticastServer.enviarMensajesHistorial(this);
            MainMulticastServer.broadcast("💬 " + nombreCliente + " ha ingresado al chat", this);

            while (disponible) {
                String mensaje = entrada.readUTF();
                if (mensaje.equalsIgnoreCase("/salir")) {
                    disponible = false;
                } else {
                    MainMulticastServer.broadcast("💬 " + nombreCliente + ": " + mensaje, this);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Eliminar cliente
        MainMulticastServer.eliminarCliente(this, nombreCliente);
        MainMulticastServer.broadcast("🔴 " + nombreCliente + " ha salido del chat", this);

        try {
            cliente.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enviarMensaje(String mensaje) {
        try {
            salida.writeUTF(mensaje);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

