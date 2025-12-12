import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ClienteHandler implements Runnable {

    private final Socket socket;
    private boolean usuarioCorrecto = false;
    private boolean autenticado = false;

    public ClienteHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        Datos.SESIONES.incrementAndGet();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter pw = new PrintWriter(socket.getOutputStream(), true)) {

            String linea;

            while ((linea = br.readLine()) != null) {

                linea = linea.trim();
                if (linea.isEmpty()) continue;

                String[] partes = linea.split(" ", 3);
                String num = partes[0];
                String comando = partes.length > 1 ? partes[1].toUpperCase() : "";
                String info = partes.length == 3 ? partes[2] : "";

                switch (comando) {

                    case "USER":
                        if ("admin".equals(info)) {
                            usuarioCorrecto = true;
                            pw.println("OK " + num + " 200 Usuario correcto");
                        } else {
                            pw.println("FAILED " + num + " 401 Usuario incorrecto");
                        }
                        break;

                    case "PASS":
                        if (usuarioCorrecto && "admin".equals(info)) {
                            autenticado = true;
                            pw.println("OK " + num + " 200 Autenticación correcta");
                        } else {
                            pw.println("FAILED " + num + " 403 Clave incorrecta");
                        }
                        break;

                    case "EXIT":
                        pw.println("OK " + num + " 200 Conexión cerrada");
                        return;

                    case "SESIONES":
                        pw.println("OK " + num + " 200 " + Datos.SESIONES.get());
                        break;

                    case "ADDCLUB":
                        if (!autenticado) { pw.println("FAILED " + num + " 403 No autenticado"); break; }
                        addClub(num, pw);
                        break;

                    case "UPDATECLUB":
                        if (!autenticado) { pw.println("FAILED " + num + " 403 No autenticado"); break; }
                        updateClub(num, info, pw);
                        break;

                    case "GETCLUB":
                        if (!autenticado) { pw.println("FAILED " + num + " 403 No autenticado"); break; }
                        getClub(num, info, pw);
                        break;

                    case "REMOVECLUB":
                        if (!autenticado) { pw.println("FAILED " + num + " 403 No autenticado"); break; }
                        removeClub(num, info, pw);
                        break;

                    case "LISTCLUBES":
                        if (!autenticado) { pw.println("FAILED " + num + " 403 No autenticado"); break; }
                        listClubes(num, pw);
                        break;

                    case "COUNTCLUBES":
                        if (!autenticado) { pw.println("FAILED " + num + " 403 No autenticado"); break; }
                        pw.println("OK " + num + " 200 " + Datos.CLUBS.size());
                        break;

                    case "ADDJUGADOR":
                        if (!autenticado) { pw.println("FAILED " + num + " 403 No autenticado"); break; }
                        addJugador(num, pw);
                        break;

                    case "GETJUGADOR":
                        if (!autenticado) { pw.println("FAILED " + num + " 403 No autenticado"); break; }
                        getJugador(num, info, pw);
                        break;

                    case "REMOVEJUGADOR":
                        if (!autenticado) { pw.println("FAILED " + num + " 403 No autenticado"); break; }
                        removeJugador(num, info, pw);
                        break;

                    case "LISTJUGADORES":
                        if (!autenticado) { pw.println("FAILED " + num + " 403 No autenticado"); break; }
                        listJugadores(num, pw);
                        break;

                    case "ADDJUGADOR2CLUB":
                        if (!autenticado) { pw.println("FAILED " + num + " 403 No autenticado"); break; }
                        addJugador2Club(num, info, pw);
                        break;

                    case "REMOVEJUGFROMCLUB":
                        if (!autenticado) { pw.println("FAILED " + num + " 403 No autenticado"); break; }
                        removeJugFromClub(num, info, pw);
                        break;

                    case "LISTJUGFROMCLUB":
                        if (!autenticado) { pw.println("FAILED " + num + " 403 No autenticado"); break; }
                        listJugFromClub(num, info, pw);
                        break;

                    default:
                        pw.println("FAILED " + num + " 400 Comando no válido");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Datos.SESIONES.decrementAndGet();
            try { socket.close(); } catch (Exception ignored) {}
        }
    }

    private String ipLocal() throws Exception {
        return InetAddress.getLocalHost().getHostAddress();
    }

    private static class CanalDatos implements AutoCloseable {
        ServerSocket ss;
        Socket s;
        ObjectInputStream ois;
        ObjectOutputStream oos;

        public void close() {
            try { if (ois != null) ois.close(); } catch (Exception ignored) {}
            try { if (oos != null) oos.close(); } catch (Exception ignored) {}
            try { if (s != null) s.close(); } catch (Exception ignored) {}
            try { if (ss != null) ss.close(); } catch (Exception ignored) {}
        }
    }

    private CanalDatos abrirCanalDatos(String num, PrintWriter pw) throws Exception {
        int puerto = Datos.PUERTO_DATOS.getAndIncrement();
        String ip = ipLocal();
        pw.println("PREOK " + num + " 200 " + ip + " " + puerto);

        CanalDatos cd = new CanalDatos();
        cd.ss = new ServerSocket(puerto);
        cd.s = cd.ss.accept();
        return cd;
    }

    private void addClub(String num, PrintWriter pw) throws Exception {
        try (CanalDatos cd = abrirCanalDatos(num, pw)) {
            cd.ois = new ObjectInputStream(cd.s.getInputStream());
            Club c = (Club) cd.ois.readObject();

            if (Datos.CLUBS.putIfAbsent(c.getId(), c) != null) {
                pw.println("FAILED " + num + " 409 Club ya existe");
            } else {
                pw.println("OK " + num + " 200 Transferencia terminada");
            }
        }
    }

    private void updateClub(String num, String id, PrintWriter pw) throws Exception {
        Club actual = Datos.CLUBS.get(id);
        if (actual == null) { pw.println("FAILED " + num + " 404 Club no encontrado"); return; }

        try (CanalDatos cd = abrirCanalDatos(num, pw)) {
            cd.ois = new ObjectInputStream(cd.s.getInputStream());
            Club nuevo = (Club) cd.ois.readObject();
            nuevo.setId(id);

            for (Object o : actual.getJugadores()) {
                Jugador j = (Jugador) o;
                nuevo.addJugador(j.getId(), j);
            }

            Datos.CLUBS.put(id, nuevo);
            pw.println("OK " + num + " 200 Transferencia terminada");
        }
    }

    private void getClub(String num, String id, PrintWriter pw) throws Exception {
        Club c = Datos.CLUBS.get(id);
        if (c == null) { pw.println("FAILED " + num + " 404 Club no encontrado"); return; }

        try (CanalDatos cd = abrirCanalDatos(num, pw)) {
            cd.oos = new ObjectOutputStream(cd.s.getOutputStream());
            cd.oos.writeObject(c);
            cd.oos.flush();
            pw.println("OK " + num + " 200 Transferencia terminada");
        }
    }

    private void removeClub(String num, String id, PrintWriter pw) {
        Club c = Datos.CLUBS.remove(id);
        if (c == null) pw.println("FAILED " + num + " 404 Club no encontrado");
        else pw.println("OK " + num + " 200 Club eliminado");
    }

    private void listClubes(String num, PrintWriter pw) throws Exception {
        try (CanalDatos cd = abrirCanalDatos(num, pw)) {
            cd.oos = new ObjectOutputStream(cd.s.getOutputStream());
            cd.oos.writeObject(new ArrayList<>(Datos.CLUBS.values()));
            cd.oos.flush();
            pw.println("OK " + num + " 200 Transferencia terminada");
        }
    }

    private void addJugador(String num, PrintWriter pw) throws Exception {
        try (CanalDatos cd = abrirCanalDatos(num, pw)) {
            cd.ois = new ObjectInputStream(cd.s.getInputStream());
            Jugador j = (Jugador) cd.ois.readObject();

            if (Datos.JUGADORES.putIfAbsent(j.getId(), j) != null) {
                pw.println("FAILED " + num + " 409 Jugador ya existe");
            } else {
                pw.println("OK " + num + " 200 Transferencia terminada");
            }
        }
    }

    private void getJugador(String num, String id, PrintWriter pw) throws Exception {
        Jugador j = Datos.JUGADORES.get(id);
        if (j == null) { pw.println("FAILED " + num + " 404 Jugador no encontrado"); return; }

        try (CanalDatos cd = abrirCanalDatos(num, pw)) {
            cd.oos = new ObjectOutputStream(cd.s.getOutputStream());
            cd.oos.writeObject(j);
            cd.oos.flush();
            pw.println("OK " + num + " 200 Transferencia terminada");
        }
    }

    private void removeJugador(String num, String id, PrintWriter pw) throws Exception {
        Jugador j = Datos.JUGADORES.remove(id);

        try (CanalDatos cd = abrirCanalDatos(num, pw)) {
            cd.oos = new ObjectOutputStream(cd.s.getOutputStream());
            cd.oos.writeObject(j);
            cd.oos.flush();

            if (j == null) pw.println("FAILED " + num + " 404 Jugador no encontrado");
            else pw.println("OK " + num + " 200 Transferencia terminada");
        }
    }

    private void listJugadores(String num, PrintWriter pw) throws Exception {
        try (CanalDatos cd = abrirCanalDatos(num, pw)) {
            cd.oos = new ObjectOutputStream(cd.s.getOutputStream());
            cd.oos.writeObject(new ArrayList<>(Datos.JUGADORES.values()));
            cd.oos.flush();
            pw.println("OK " + num + " 200 Transferencia terminada");
        }
    }

    private void addJugador2Club(String num, String info, PrintWriter pw) {
        String[] p = info.split("\\s+");
        Club c = Datos.CLUBS.get(p[1]);
        Jugador j = Datos.JUGADORES.get(p[0]);

        if (c == null || j == null) {
            pw.println("FAILED " + num + " 404 Datos no encontrados");
        } else {
            c.addJugador(j.getId(), j);
            pw.println("OK " + num + " 200 Jugador añadido al club");
        }
    }

    private void removeJugFromClub(String num, String info, PrintWriter pw) {
        String[] p = info.split("\\s+");
        Club c = Datos.CLUBS.get(p[1]);

        if (c == null || !c.removeJugador(p[0])) {
            pw.println("FAILED " + num + " 404 Datos no encontrados");
        } else {
            pw.println("OK " + num + " 200 Jugador eliminado del club");
        }
    }

    private void listJugFromClub(String num, String idClub, PrintWriter pw) throws Exception {
        Club c = Datos.CLUBS.get(idClub);
        if (c == null) { pw.println("FAILED " + num + " 404 Club no encontrado"); return; }

        try (CanalDatos cd = abrirCanalDatos(num, pw)) {
            cd.oos = new ObjectOutputStream(cd.s.getOutputStream());
            cd.oos.writeObject(c.getJugadores());
            cd.oos.flush();
            pw.println("OK " + num + " 200 Transferencia terminada");
        }
    }
}
