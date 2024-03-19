import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private DataInputStream input = null;
    private DataOutputStream output = null;
    Scanner keyboard = new Scanner(System.in);
    final String COMANDO_TERMINACION = "Exit()";

    public void levantarConexion(String ip, int puerto) {
        try {
            socket = new Socket(ip, puerto);
            mostrarTexto("Connecting to :" + socket.getInetAddress().getHostName());

        } catch (Exception e) {
            mostrarTexto("Excepción when creating connection: " + e.getMessage()); //Manda una excepcion en caso de que ocurra un error al crear la conexion
            System.exit(0);
        }
    }

    public static void mostrarTexto(String s) {
        System.out.println(s);
    }

    public void abrirFlujos() {         //establece los flujos de entrada y salida y se comunica a un socket en un servidor
        try {
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            output.flush();
        } catch (IOException e) {
            mostrarTexto("Flow opening error");
        }
    }

    public void enviar(String s) {        // recibe el string y lo reescribe en utf8
        try {
            output.writeUTF(s);
            output.flush();
        } catch (IOException e) {
            mostrarTexto("IOException on send");
        }
    }

    public void cerrarConexion() {
        try {
            input.close();                  //Se asegura de que cierre las conexiones y finaliza con conexión ended
            input.close();
            socket.close();
            mostrarTexto("Conexión ended");
        } catch (IOException e) {
            mostrarTexto("IOException on cerrarConexion()");
        }finally{
            System.exit(0);
        }
    }

    public void ejecutarConexion(String ip, int puerto) {       //inicia con un hilo y recibe todos los datos, finalmente cierra la conexion
        Thread hilo = new Thread(() -> {
            try {
                levantarConexion(ip, puerto);
                abrirFlujos();
                recibirDatos();
            } finally {
                cerrarConexion();
            }
        });
        hilo.start();
    }

    public void recibirDatos() {
        String st = "";
        try {
            do {
                st = input.readUTF();
                mostrarTexto("\n[Server] => " + st);
                System.out.print("\n[You] => ");
            } while (!st.equals(COMANDO_TERMINACION));
        } catch (IOException e) {}
    }

    public void escribirDatos() {
        String entrada = "";
        while (true) {
            System.out.print("[You] => ");
            entrada = keyboard.nextLine();
            if(entrada.length() > 0)
                enviar(entrada);
        }
    }

    public static void main(String[] argumentos) {
        Client cliente = new Client();
        Scanner escaner = new Scanner(System.in);
        mostrarTexto("Enter the IP: [localhost by default] ");
        String ip = escaner.nextLine();
        if (ip.length() <= 0) ip = "localhost";

        mostrarTexto("Port: [5050 Default] ");
        String puerto = escaner.nextLine();
        if (puerto.length() <= 0) puerto = "5050";
        cliente.ejecutarConexion(ip, Integer.parseInt(puerto));
        cliente.escribirDatos();
    }
}