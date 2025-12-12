import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Datos {
    public static final ConcurrentHashMap<String, Club> CLUBS = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, Jugador> JUGADORES = new ConcurrentHashMap<>();
    public static final AtomicInteger PUERTO_DATOS = new AtomicInteger(5001);
    public static final AtomicInteger SESIONES = new AtomicInteger(0);
}
