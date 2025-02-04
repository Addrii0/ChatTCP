package hilos;

import util.MainServer;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class HiloCliente implements Runnable{
    private Socket cliente;
    private DataInputStream entrada;
    private DataOutputStream salida;
    private String nombreCliente;
    private boolean disponible = false;

    public HiloCliente(Socket cliente){
        this.cliente = cliente;
    }

    @Override
    public void run() {
        try {
            entrada = new DataInputStream(cliente.getInputStream());
            salida = new DataOutputStream(cliente.getOutputStream());

            //Verificar nombre único
            while (!disponible) {
                nombreCliente = entrada.readUTF();
                if (MainServer.registrarNombre(nombreCliente)) {
                    salida.writeUTF("ACEPTADO");
                    MainServer.agregarCliente(this); // Agregar al usuario a la lista 
                    disponible = true;
                } else {
                    salida.writeUTF("NOMBRE EN USO");
                }
            }
            MainServer.broadcast( "🔵 " + nombreCliente + " se ha unido al chat", this);
            while (disponible){
                String mensaje = entrada.readUTF();
                if(mensaje.equalsIgnoreCase("/salir")){

                }
                MainServer.broadcast("💬 " + nombreCliente + ": " + mensaje, this);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MainServer.eliminarCliente(this, nombreCliente);
        MainServer.broadcast("🔴 " + nombreCliente + " ha salido del chat", this);
        try {
            cliente.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
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
