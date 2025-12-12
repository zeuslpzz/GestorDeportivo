import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Cliente {

    private static String enviar(PrintWriter pw, BufferedReader br, String msg) throws Exception {
        pw.println(msg);
        return br.readLine();
    }

    private static String[] preok(String r) {
        return r.split("\\s+");
    }

    private static void enviarObjeto(String ip, int puerto, Object obj) throws Exception {
        try (Socket s = new Socket(ip, puerto);
             ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream())) {
            oos.writeObject(obj);
            oos.flush();
        }
    }

    private static Object recibirObjeto(String ip, int puerto) throws Exception {
        try (Socket s = new Socket(ip, puerto);
             ObjectInputStream ois = new ObjectInputStream(s.getInputStream())) {
            return ois.readObject();
        }
    }

    public static void main(String[] args) {
        try (Socket cmd = new Socket("localhost", 5000);
             PrintWriter pw = new PrintWriter(cmd.getOutputStream(), true);
             BufferedReader br = new BufferedReader(new InputStreamReader(cmd.getInputStream()));
             Scanner sc = new Scanner(System.in)) {

            System.out.println(enviar(pw, br, "1 USER admin"));
            System.out.println(enviar(pw, br, "2 PASS admin"));

            while (true) {
                System.out.println();
                System.out.println("1. Añadir club");
                System.out.println("2. Modificar club");
                System.out.println("3. Obtener club");
                System.out.println("4. Eliminar club");
                System.out.println("5. Listar clubes");
                System.out.println("6. Contar clubes");
                System.out.println("7. Añadir jugador");
                System.out.println("8. Obtener jugador");
                System.out.println("9. Eliminar jugador");
                System.out.println("10. Listar jugadores");
                System.out.println("11. Añadir jugador a club");
                System.out.println("12. Quitar jugador de club");
                System.out.println("13. Listar jugadores de un club");
                System.out.println("14. Ver sesiones activas");
                System.out.println("0. Salir");
                System.out.print("> ");

                String op = sc.nextLine().trim();

                if (op.equals("0")) {
                    System.out.println(enviar(pw, br, "999 EXIT"));
                    break;
                }

                switch (op) {
                    case "1" -> {
                        System.out.print("Id del club: ");
                        String id = sc.nextLine().trim();
                        System.out.print("Nombre del club: ");
                        String nombre = sc.nextLine().trim();

                        String r = enviar(pw, br, "10 ADDCLUB");
                        System.out.println(r);
                        if (r.startsWith("PREOK")) {
                            String[] p = preok(r);
                            enviarObjeto(p[3], Integer.parseInt(p[4]), new Club(id, nombre));
                            System.out.println(br.readLine());
                        }
                    }
                    case "2" -> {
                        System.out.print("Id del club: ");
                        String id = sc.nextLine().trim();
                        System.out.print("Nuevo nombre del club: ");
                        String nombre = sc.nextLine().trim();

                        String r = enviar(pw, br, "11 UPDATECLUB " + id);
                        System.out.println(r);
                        if (r.startsWith("PREOK")) {
                            String[] p = preok(r);
                            enviarObjeto(p[3], Integer.parseInt(p[4]), new Club(id, nombre));
                            System.out.println(br.readLine());
                        }
                    }
                    case "3" -> {
                        System.out.print("Id del club: ");
                        String id = sc.nextLine().trim();

                        String r = enviar(pw, br, "12 GETCLUB " + id);
                        System.out.println(r);
                        if (r.startsWith("PREOK")) {
                            String[] p = preok(r);
                            Object obj = recibirObjeto(p[3], Integer.parseInt(p[4]));
                            System.out.println(obj);
                            System.out.println(br.readLine());
                        }
                    }
                    case "4" -> {
                        System.out.print("Id del club: ");
                        String id = sc.nextLine().trim();
                        System.out.println(enviar(pw, br, "13 REMOVECLUB " + id));
                    }
                    case "5" -> {
                        String r = enviar(pw, br, "14 LISTCLUBES");
                        System.out.println(r);
                        if (r.startsWith("PREOK")) {
                            String[] p = preok(r);
                            ArrayList lista = (ArrayList) recibirObjeto(p[3], Integer.parseInt(p[4]));
                            for (Object c : lista) System.out.println(c);
                            System.out.println(br.readLine());
                        }
                    }
                    case "6" -> System.out.println(enviar(pw, br, "15 COUNTCLUBES"));

                    case "7" -> {
                        System.out.print("Id del jugador: ");
                        String id = sc.nextLine().trim();
                        System.out.print("Nombre: ");
                        String nombre = sc.nextLine().trim();
                        System.out.print("Apellidos: ");
                        String apellidos = sc.nextLine().trim();
                        System.out.print("Goles: ");
                        int goles = Integer.parseInt(sc.nextLine().trim());

                        String r = enviar(pw, br, "20 ADDJUGADOR");
                        System.out.println(r);
                        if (r.startsWith("PREOK")) {
                            String[] p = preok(r);
                            enviarObjeto(p[3], Integer.parseInt(p[4]), new Jugador(id, nombre, apellidos, goles));
                            System.out.println(br.readLine());
                        }
                    }
                    case "8" -> {
                        System.out.print("Id del jugador: ");
                        String id = sc.nextLine().trim();

                        String r = enviar(pw, br, "21 GETJUGADOR " + id);
                        System.out.println(r);
                        if (r.startsWith("PREOK")) {
                            String[] p = preok(r);
                            Object obj = recibirObjeto(p[3], Integer.parseInt(p[4]));
                            System.out.println(obj);
                            System.out.println(br.readLine());
                        }
                    }
                    case "9" -> {
                        System.out.print("Id del jugador: ");
                        String id = sc.nextLine().trim();

                        String r = enviar(pw, br, "22 REMOVEJUGADOR " + id);
                        System.out.println(r);
                        if (r.startsWith("PREOK")) {
                            String[] p = preok(r);
                            Object obj = recibirObjeto(p[3], Integer.parseInt(p[4]));
                            System.out.println(obj);
                            System.out.println(br.readLine());
                        }
                    }
                    case "10" -> {
                        String r = enviar(pw, br, "23 LISTJUGADORES");
                        System.out.println(r);
                        if (r.startsWith("PREOK")) {
                            String[] p = preok(r);
                            ArrayList lista = (ArrayList) recibirObjeto(p[3], Integer.parseInt(p[4]));
                            for (Object j : lista) System.out.println(j);
                            System.out.println(br.readLine());
                        }
                    }
                    case "11" -> {
                        System.out.print("Id del jugador: ");
                        String idj = sc.nextLine().trim();
                        System.out.print("Id del club: ");
                        String idc = sc.nextLine().trim();
                        System.out.println(enviar(pw, br, "30 ADDJUGADOR2CLUB " + idj + " " + idc));
                    }
                    case "12" -> {
                        System.out.print("Id del jugador: ");
                        String idj = sc.nextLine().trim();
                        System.out.print("Id del club: ");
                        String idc = sc.nextLine().trim();
                        System.out.println(enviar(pw, br, "31 REMOVEJUGFROMCLUB " + idj + " " + idc));
                    }
                    case "13" -> {
                        System.out.print("Id del club: ");
                        String idc = sc.nextLine().trim();

                        String r = enviar(pw, br, "32 LISTJUGFROMCLUB " + idc);
                        System.out.println(r);
                        if (r.startsWith("PREOK")) {
                            String[] p = preok(r);
                            ArrayList lista = (ArrayList) recibirObjeto(p[3], Integer.parseInt(p[4]));
                            for (Object j : lista) System.out.println(j);
                            System.out.println(br.readLine());
                        }
                    }
                    case "14" -> System.out.println(enviar(pw, br, "40 SESIONES"));

                    default -> System.out.println("Opción no válida");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
