import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            while (true) {
                Socket cliente = serverSocket.accept();
                new Thread(new ServerThread(cliente)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
