package hilos;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.IOException;

public class HiloCliente implements Runnable{
    private DataInputStream entrada;
    private JTextArea areaTexto;

    public HiloCliente(DataInputStream entrada, JTextArea areaTexto){
        this.entrada = entrada;
        this.areaTexto = areaTexto;
    }

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
}

