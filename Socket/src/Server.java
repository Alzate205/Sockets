import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    private Socket socket;
    private ServerSocket serverSocket;
    private DataInputStream bufferDeEntrada = null;  // proporciona métodos para leer datos primitivos y se inicializa como nulo ya que se inicializará  una instancia  para su uso.
    private DataOutputStream bufferDeSalida = null;
    Scanner escaner = new Scanner(System.in);
    final String COMANDO_TERMINACION = "salir()";

    public void levantarConexion(int puerto) {
        try {
            serverSocket = new ServerSocket(puerto);
            mostrarTexto("Waiting for incoming connection on port " + String.valueOf(puerto) + "...");
            socket = serverSocket.accept();
            mostrarTexto("Connection established with: " + socket.getInetAddress().getHostName() + "\n\n\n");
        } catch (Exception e) {
            mostrarTexto("Connection error(): " + e.getMessage());      //Lanza una excepcion cuando no puede conectar con el servidor
            System.exit(0);
        }
    }
    public void flujos() {                //Aquí se crea un nuevo objeto DataInputStream y se asigna a la variable bufferDeEntrada
        try {
            bufferDeEntrada = new DataInputStream(socket.getInputStream());
            bufferDeSalida = new DataOutputStream(socket.getOutputStream());
            bufferDeSalida.flush();                        //Cuando se llama a este método (El punnto flush), garantiza que cualquier dato que esté almacenado en el búfer del flujo de salida se envíe de inmediato al destino deseado
        } catch (IOException e) {
            mostrarTexto("Flow opening error");   // Si pasa esta excepción, se llama el metodo para mostrar un mensaje de error
        }
    }

    public void recibirDatos() {  // recibe datos hasta que le mandan un comando_terminacion
        String st = "";  //st=string??
        try {
            do {
                st = bufferDeEntrada.readUTF(); //recibe la cadena en utf8
                mostrarTexto("\n[Client] => " + st);
                System.out.print("\n[You] => ");
            } while (!st.equals(COMANDO_TERMINACION));
        } catch (IOException e) {
            cerrarConexion();
        }
    }


    public void enviar(String s) {
        try {
            bufferDeSalida.writeUTF(s);  //toma la cadena de texto s y la escribe en utf8
            bufferDeSalida.flush();  //envia los datos apenas tenga escrito en utf8
        } catch (IOException e) {
            mostrarTexto("Failed to send(): " + e.getMessage());  // excepcion por si pasa algun error
        }
    }

    public static void mostrarTexto(String s) {
        System.out.print(s);
    }

    public void escribirDatos() {
        while (true) {
            System.out.print("[You] => ");      //solicita al usuario que ingrese texto para luego enviarlo al buffer de salida
            enviar(escaner.nextLine());
        }
    }

    public void cerrarConexion() {     //Se asegura de que cierre todos los elementos y finaliza el programa mostrando un mensaje
        try {
            bufferDeEntrada.close();
            bufferDeSalida.close();
            socket.close();
        } catch (IOException e) {
            mostrarTexto("Excepción closeConnection(): " + e.getMessage());
        } finally {
            mostrarTexto("Conversatión ended....");
            System.exit(0);

        }
    }

    public void ejecutarConexion(int puerto) {   //este método inicia una conexión en un hilo , despues de que complete de recibir los datos se asegura que la conexión se cierre correctamente
        Thread hilo = new Thread(() -> {
            while (true) {
                try {
                    levantarConexion(puerto);
                    flujos();
                    recibirDatos();
                } finally {
                    cerrarConexion();
                }
            }
        });
        hilo.start();
    }

    public static void main(String[] args) throws IOException {        // inicializa y ejecuta el servidor en especifico por el usuario o usara el predeterminado que en este caso sera el 5050
        Server s = new Server();
        Scanner sc = new Scanner(System.in);

        mostrarTexto("Enter the port [5050 default]: ");
        String puerto = sc.nextLine();
        if (puerto.length() <= 0) puerto = "5050";
        s.ejecutarConexion(Integer.parseInt(puerto));
        s.escribirDatos();
    }
}