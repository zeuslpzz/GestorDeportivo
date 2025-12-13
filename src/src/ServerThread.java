import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerThread implements Runnable {

    private final Socket socket;
    private boolean usuarioCorrecto = false;
    private boolean autenticado = false;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        Datos.SESIONES.incrementAndGet();

        BufferedReader br = null;
        PrintWriter pw = null;

        try {
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw = new PrintWriter(socket.getOutputStream(), true);

            String linea;

            while ((linea = br.readLine()) != null) {

                linea = linea.trim();
                if (linea.length() == 0) continue;

                String[] partes = linea.split(" ", 3);
                String num = partes[0];
                String comando = "";
                String info = "";

                if (partes.length > 1) comando = partes[1].toUpperCase();
                if (partes.length == 3) info = partes[2];

                if (comando.equals("USER")) {
                    if ("admin".equals(info)) {
                        usuarioCorrecto = true;
                        pw.println("OK " + num + " 200 Usuario correcto");
                    } else {
                        usuarioCorrecto = false;
                        autenticado = false;
                        pw.println("FAILED " + num + " 401 Usuario incorrecto");
                    }
                } else if (comando.equals("PASS")) {
                    if (usuarioCorrecto && "admin".equals(info)) {
                        autenticado = true;
                        pw.println("OK " + num + " 200 Autenticación correcta");
                    } else {
                        autenticado = false;
                        pw.println("FAILED " + num + " 403 Clave incorrecta");
                    }
                } else if (comando.equals("EXIT")) {
                    pw.println("OK " + num + " 200 Conexión cerrada");
                    return;
                } else if (comando.equals("SESIONES")) {
                    pw.println("OK " + num + " 200 " + Datos.SESIONES.get());
                } else if (comando.equals("ADDCLUB")) {
                    if (!autenticado) { pw.println("FAILED " + num + " 403 No autenticado"); continue; }
                    addClub(num, pw);
                } else if (comando.equals("UPDATECLUB")) {
                    if (!autenticado) { pw.println("FAILED " + num + " 403 No autenticado"); continue; }
                    updateClub(num, info, pw);
                } else if (comando.equals("GETCLUB")) {
                    if (!autenticado) { pw.println("FAILED " + num + " 403 No autenticado"); continue; }
                    getClub(num, info, pw);
                } else if (comando.equals("REMOVECLUB")) {
                    if (!autenticado) { pw.println("FAILED " + num + " 403 No autenticado"); continue; }
                    removeClub(num, info, pw);
                } else if (comando.equals("LISTCLUBES")) {
                    if (!autenticado) { pw.println("FAILED " + num + " 403 No autenticado"); continue; }
                    listClubes(num, pw);
                } else if (comando.equals("COUNTCLUBES")) {
                    if (!autenticado) { pw.println("FAILED " + num + " 403 No autenticado"); continue; }
                    pw.println("OK " + num + " 200 " + Datos.CLUBS.size());
                } else if (comando.equals("ADDJUGADOR")) {
                    if (!autenticado) { pw.println("FAILED " + num + " 403 No autenticado"); continue; }
                    addJugador(num, pw);
                } else if (comando.equals("GETJUGADOR")) {
                    if (!autenticado) { pw.println("FAILED " + num + " 403 No autenticado"); continue; }
                    getJugador(num, info, pw);
                } else if (comando.equals("REMOVEJUGADOR")) {
                    if (!autenticado) { pw.println("FAILED " + num + " 403 No autenticado"); continue; }
                    removeJugador(num, info, pw);
                } else if (comando.equals("LISTJUGADORES")) {
                    if (!autenticado) { pw.println("FAILED " + num + " 403 No autenticado"); continue; }
                    listJugadores(num, pw);
                } else if (comando.equals("ADDJUGADOR2CLUB")) {
                    if (!autenticado) { pw.println("FAILED " + num + " 403 No autenticado"); continue; }
                    addJugador2Club(num, info, pw);
                } else if (comando.equals("REMOVEJUGFROMCLUB")) {
                    if (!autenticado) { pw.println("FAILED " + num + " 403 No autenticado"); continue; }
                    removeJugFromClub(num, info, pw);
                } else if (comando.equals("LISTJUGFROMCLUB")) {
                    if (!autenticado) { pw.println("FAILED " + num + " 403 No autenticado"); continue; }
                    listJugFromClub(num, info, pw);
                } else {
                    pw.println("FAILED " + num + " 400 Comando no válido");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Datos.SESIONES.decrementAndGet();

            try { if (br != null) br.close(); } catch (Exception ignored) {}
            try { if (pw != null) pw.close(); } catch (Exception ignored) {}
            try { socket.close(); } catch (Exception ignored) {}
        }
    }

    private String ipLocal() throws Exception {
        return InetAddress.getLocalHost().getHostAddress();
    }

    private void addClub(String num, PrintWriter pw) throws Exception {
        int puerto = Datos.PUERTO_DATOS.getAndIncrement();
        pw.println("PREOK " + num + " 200 " + ipLocal() + " " + puerto);

        ServerSocket ss = new ServerSocket(puerto);
        Socket s = ss.accept();
        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());

        Club c = (Club) ois.readObject();

        ois.close();
        s.close();
        ss.close();

        if (c == null || c.getId() == null || c.getId().trim().length() == 0) {
            pw.println("FAILED " + num + " 422 Datos incorrectos");
            return;
        }

        if (Datos.CLUBS.putIfAbsent(c.getId(), c) != null) {
            pw.println("FAILED " + num + " 409 Club ya existe");
        } else {
            pw.println("OK " + num + " 200 Transferencia terminada");
        }
    }

    private void updateClub(String num, String id, PrintWriter pw) throws Exception {
        if (id == null) id = "";
        id = id.trim();

        Club actual = Datos.CLUBS.get(id);
        if (actual == null) {
            pw.println("FAILED " + num + " 404 Club no encontrado");
            return;
        }

        int puerto = Datos.PUERTO_DATOS.getAndIncrement();
        pw.println("PREOK " + num + " 200 " + ipLocal() + " " + puerto);

        ServerSocket ss = new ServerSocket(puerto);
        Socket s = ss.accept();
        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());

        Club nuevo = (Club) ois.readObject();

        ois.close();
        s.close();
        ss.close();

        if (nuevo == null) {
            pw.println("FAILED " + num + " 422 Datos incorrectos");
            return;
        }

        nuevo.setId(id);

        ArrayList lista = actual.getJugadores();
        for (int i = 0; i < lista.size(); i++) {
            Jugador j = (Jugador) lista.get(i);
            nuevo.addJugador(j.getId(), j);
        }

        Datos.CLUBS.put(id, nuevo);
        pw.println("OK " + num + " 200 Transferencia terminada");
    }

    private void getClub(String num, String id, PrintWriter pw) throws Exception {
        if (id == null) id = "";
        id = id.trim();

        Club c = Datos.CLUBS.get(id);
        if (c == null) {
            pw.println("FAILED " + num + " 404 Club no encontrado");
            return;
        }

        int puerto = Datos.PUERTO_DATOS.getAndIncrement();
        pw.println("PREOK " + num + " 200 " + ipLocal() + " " + puerto);

        ServerSocket ss = new ServerSocket(puerto);
        Socket s = ss.accept();
        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());

        oos.writeObject(c);
        oos.flush();

        oos.close();
        s.close();
        ss.close();

        pw.println("OK " + num + " 200 Transferencia terminada");
    }

    private void removeClub(String num, String id, PrintWriter pw) {
        if (id == null) id = "";
        id = id.trim();

        Club c = Datos.CLUBS.remove(id);
        if (c == null) pw.println("FAILED " + num + " 404 Club no encontrado");
        else pw.println("OK " + num + " 200 Club eliminado");
    }

    private void listClubes(String num, PrintWriter pw) throws Exception {
        int puerto = Datos.PUERTO_DATOS.getAndIncrement();
        pw.println("PREOK " + num + " 200 " + ipLocal() + " " + puerto);

        ServerSocket ss = new ServerSocket(puerto);
        Socket s = ss.accept();
        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());

        oos.writeObject(new ArrayList<>(Datos.CLUBS.values()));
        oos.flush();

        oos.close();
        s.close();
        ss.close();

        pw.println("OK " + num + " 200 Transferencia terminada");
    }

    private void addJugador(String num, PrintWriter pw) throws Exception {
        int puerto = Datos.PUERTO_DATOS.getAndIncrement();
        pw.println("PREOK " + num + " 200 " + ipLocal() + " " + puerto);

        ServerSocket ss = new ServerSocket(puerto);
        Socket s = ss.accept();
        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());

        Jugador j = (Jugador) ois.readObject();

        ois.close();
        s.close();
        ss.close();

        if (j == null || j.getId() == null || j.getId().trim().length() == 0) {
            pw.println("FAILED " + num + " 422 Datos incorrectos");
            return;
        }

        if (Datos.JUGADORES.putIfAbsent(j.getId(), j) != null) {
            pw.println("FAILED " + num + " 409 Jugador ya existe");
        } else {
            pw.println("OK " + num + " 200 Transferencia terminada");
        }
    }

    private void getJugador(String num, String id, PrintWriter pw) throws Exception {
        if (id == null) id = "";
        id = id.trim();

        Jugador j = Datos.JUGADORES.get(id);
        if (j == null) {
            pw.println("FAILED " + num + " 404 Jugador no encontrado");
            return;
        }

        int puerto = Datos.PUERTO_DATOS.getAndIncrement();
        pw.println("PREOK " + num + " 200 " + ipLocal() + " " + puerto);

        ServerSocket ss = new ServerSocket(puerto);
        Socket s = ss.accept();
        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());

        oos.writeObject(j);
        oos.flush();

        oos.close();
        s.close();
        ss.close();

        pw.println("OK " + num + " 200 Transferencia terminada");
    }

    private void removeJugador(String num, String id, PrintWriter pw) throws Exception {
        if (id == null) id = "";
        id = id.trim();

        Jugador borrado = Datos.JUGADORES.remove(id);

        int puerto = Datos.PUERTO_DATOS.getAndIncrement();
        pw.println("PREOK " + num + " 200 " + ipLocal() + " " + puerto);

        ServerSocket ss = new ServerSocket(puerto);
        Socket s = ss.accept();
        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());

        oos.writeObject(borrado);
        oos.flush();

        oos.close();
        s.close();
        ss.close();

        if (borrado == null) pw.println("FAILED " + num + " 404 Jugador no encontrado");
        else pw.println("OK " + num + " 200 Transferencia terminada");
    }

    private void listJugadores(String num, PrintWriter pw) throws Exception {
        int puerto = Datos.PUERTO_DATOS.getAndIncrement();
        pw.println("PREOK " + num + " 200 " + ipLocal() + " " + puerto);

        ServerSocket ss = new ServerSocket(puerto);
        Socket s = ss.accept();
        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());

        oos.writeObject(new ArrayList<>(Datos.JUGADORES.values()));
        oos.flush();

        oos.close();
        s.close();
        ss.close();

        pw.println("OK " + num + " 200 Transferencia terminada");
    }

    private void addJugador2Club(String num, String info, PrintWriter pw) {
        if (info == null) info = "";
        String[] p = info.trim().split("\\s+");
        if (p.length < 2) {
            pw.println("FAILED " + num + " 400 Datos incorrectos");
            return;
        }

        Jugador j = Datos.JUGADORES.get(p[0]);
        Club c = Datos.CLUBS.get(p[1]);

        if (j == null) {
            pw.println("FAILED " + num + " 404 Jugador no encontrado");
            return;
        }
        if (c == null) {
            pw.println("FAILED " + num + " 404 Club no encontrado");
            return;
        }

        c.addJugador(j.getId(), j);
        pw.println("OK " + num + " 200 Jugador añadido al club");
    }

    private void removeJugFromClub(String num, String info, PrintWriter pw) {
        if (info == null) info = "";
        String[] p = info.trim().split("\\s+");
        if (p.length < 2) {
            pw.println("FAILED " + num + " 400 Datos incorrectos");
            return;
        }

        Club c = Datos.CLUBS.get(p[1]);
        if (c == null) {
            pw.println("FAILED " + num + " 404 Club no encontrado");
            return;
        }

        boolean ok = c.removeJugador(p[0]);
        if (ok) pw.println("OK " + num + " 200 Jugador eliminado del club");
        else pw.println("FAILED " + num + " 404 Jugador no encontrado");
    }

    private void listJugFromClub(String num, String idClub, PrintWriter pw) throws Exception {
        if (idClub == null) idClub = "";
        idClub = idClub.trim();

        Club c = Datos.CLUBS.get(idClub);
        if (c == null) {
            pw.println("FAILED " + num + " 404 Club no encontrado");
            return;
        }

        int puerto = Datos.PUERTO_DATOS.getAndIncrement();
        pw.println("PREOK " + num + " 200 " + ipLocal() + " " + puerto);

        ServerSocket ss = new ServerSocket(puerto);
        Socket s = ss.accept();
        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());

        oos.writeObject(c.getJugadores());
        oos.flush();

        oos.close();
        s.close();
        ss.close();

        pw.println("OK " + num + " 200 Transferencia terminada");
    }
}
