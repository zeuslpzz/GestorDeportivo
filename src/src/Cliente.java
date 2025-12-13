import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class Cliente {

    private static int contadorEnvios = 1;

    private static String siguienteId() {
        return String.valueOf(contadorEnvios++);
    }

    private static String enviar(PrintWriter pw, BufferedReader br, String msg) throws Exception {
        pw.println(msg);
        return br.readLine();
    }

    private static String[] preok(String r) {
        return r.split("\\s+");
    }

    private static void enviarObjeto(String ip, int puerto, Object obj) throws Exception {
        Socket s = new Socket(ip, puerto);
        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
        oos.writeObject(obj);
        oos.flush();
        oos.close();
        s.close();
    }

    private static Object recibirObjeto(String ip, int puerto) throws Exception {
        Socket s = new Socket(ip, puerto);
        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
        Object obj = ois.readObject();
        ois.close();
        s.close();
        return obj;
    }

    private static void mostrarJugador(Jugador j) {
        if (j == null) {
            System.out.println("(sin datos)");
            return;
        }
        System.out.println("Id: " + j.getId());
        System.out.println("Nombre: " + j.getNombre());
        System.out.println("Apellidos: " + j.getApellidos());
        System.out.println("Goles: " + j.getGoles());
    }

    private static void mostrarClub(Club c) {
        if (c == null) {
            System.out.println("(sin datos)");
            return;
        }
        System.out.println("Id: " + c.getId());
        System.out.println("Nombre: " + c.getNombre());
        System.out.println("Total jugadores: " + c.totalJugadores());
    }

    private static void mostrarListaClubes(ArrayList lista) {
        if (lista == null || lista.isEmpty()) {
            System.out.println("(no hay clubes)");
            return;
        }
        for (int i = 0; i < lista.size(); i++) {
            Club c = (Club) lista.get(i);
            System.out.println("- " + c.getId() + " | " + c.getNombre() + " | jugadores: " + c.totalJugadores());
        }
    }

    private static void mostrarListaJugadores(ArrayList lista) {
        if (lista == null || lista.isEmpty()) {
            System.out.println("(no hay jugadores)");
            return;
        }
        for (int i = 0; i < lista.size(); i++) {
            Jugador j = (Jugador) lista.get(i);
            System.out.println("- " + j.getId() + " | " + j.getNombre() + " " + j.getApellidos() + " | goles: " + j.getGoles());
        }
    }

    private static void ayuda() {
        System.out.println("Comandos:");
        System.out.println("  USER <nombre>");
        System.out.println("  PASS <pass>");
        System.out.println("  EXIT");
        System.out.println("  SESIONES");
        System.out.println();
        System.out.println("Clubes:");
        System.out.println("  ADDCLUB");
        System.out.println("  UPDATECLUB <id>");
        System.out.println("  GETCLUB <id>");
        System.out.println("  REMOVECLUB <id>");
        System.out.println("  LISTCLUBES");
        System.out.println("  COUNTCLUBES");
        System.out.println();
        System.out.println("Jugadores:");
        System.out.println("  ADDJUGADOR");
        System.out.println("  GETJUGADOR <id>");
        System.out.println("  REMOVEJUGADOR <id>");
        System.out.println("  LISTJUGADORES");
        System.out.println();
        System.out.println("Relacion:");
        System.out.println("  ADDJUGADOR2CLUB <idJugador> <idClub>");
        System.out.println("  REMOVEJUGFROMCLUB <idJugador> <idClub>");
        System.out.println("  LISTJUGFROMCLUB <idClub>");
        System.out.println();
        System.out.println("Ejemplos:");
        System.out.println("  USER admin");
        System.out.println("  PASS admin");
        System.out.println("  ADDCLUB");
        System.out.println("  GETJUGADOR 1");
        System.out.println("  LISTCLUBES");
    }

    public static void main(String[] args) {

        try {
            Socket cmd = new Socket("localhost", 5000);
            PrintWriter pw = new PrintWriter(cmd.getOutputStream(), true);
            BufferedReader br = new BufferedReader(new InputStreamReader(cmd.getInputStream()));
            Scanner sc = new Scanner(System.in);

            System.out.println("Cliente conectado. Escribe AYUDA para ver los comandos.");

            while (true) {
                System.out.print("> ");
                String linea = sc.nextLine().trim();
                if (linea.isEmpty()) continue;

                String upper = linea.toUpperCase(Locale.ROOT);

                if (upper.equals("AYUDA") || upper.equals("HELP")) {
                    ayuda();
                    continue;
                }

                String[] partes = linea.split("\\s+");
                String comando = partes[0].toUpperCase(Locale.ROOT);

                String idMsg = siguienteId();
                String envio;

                if (partes.length == 1) {
                    envio = idMsg + " " + comando;
                } else {
                    int pos = linea.indexOf(' ');
                    String resto = linea.substring(pos + 1);
                    envio = idMsg + " " + comando + " " + resto;
                }

                String resp1 = enviar(pw, br, envio);
                System.out.println(resp1);

                if (resp1 != null && resp1.startsWith("PREOK")) {
                    String[] p = preok(resp1);
                    String ip = p[3];
                    int puertoDatos = Integer.parseInt(p[4]);

                    if (comando.equals("ADDCLUB")) {
                        System.out.print("Id del club: ");
                        String id = sc.nextLine().trim();
                        System.out.print("Nombre del club: ");
                        String nombre = sc.nextLine().trim();
                        enviarObjeto(ip, puertoDatos, new Club(id, nombre));
                    } else if (comando.equals("UPDATECLUB")) {
                        if (partes.length < 2) {
                            System.out.println("(faltó el id)");
                        } else {
                            System.out.print("Nuevo nombre del club: ");
                            String nombre = sc.nextLine().trim();
                            String idClub = partes[1];
                            enviarObjeto(ip, puertoDatos, new Club(idClub, nombre));
                        }
                    } else if (comando.equals("ADDJUGADOR")) {
                        System.out.print("Id del jugador: ");
                        String id = sc.nextLine().trim();
                        System.out.print("Nombre: ");
                        String nombre = sc.nextLine().trim();
                        System.out.print("Apellidos: ");
                        String apellidos = sc.nextLine().trim();
                        System.out.print("Goles: ");
                        int goles = Integer.parseInt(sc.nextLine().trim());
                        enviarObjeto(ip, puertoDatos, new Jugador(id, nombre, apellidos, goles));
                    } else if (comando.equals("GETCLUB")) {
                        Object obj = recibirObjeto(ip, puertoDatos);
                        mostrarClub((Club) obj);
                    } else if (comando.equals("GETJUGADOR")) {
                        Object obj = recibirObjeto(ip, puertoDatos);
                        mostrarJugador((Jugador) obj);
                    } else if (comando.equals("LISTCLUBES")) {
                        ArrayList lista = (ArrayList) recibirObjeto(ip, puertoDatos);
                        mostrarListaClubes(lista);
                    } else if (comando.equals("LISTJUGADORES")) {
                        ArrayList lista = (ArrayList) recibirObjeto(ip, puertoDatos);
                        mostrarListaJugadores(lista);
                    } else if (comando.equals("LISTJUGFROMCLUB")) {
                        ArrayList lista = (ArrayList) recibirObjeto(ip, puertoDatos);
                        mostrarListaJugadores(lista);
                    } else if (comando.equals("REMOVEJUGADOR")) {
                        Object obj = recibirObjeto(ip, puertoDatos);
                        System.out.println("Jugador eliminado:");
                        mostrarJugador((Jugador) obj);
                    }

                    String resp2 = br.readLine();
                    System.out.println(resp2);
                }

                if (comando.equals("EXIT")) break;
            }

            sc.close();
            br.close();
            pw.close();
            cmd.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
