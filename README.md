# GestorDeportivo

## Descripción

Sistema cliente-servidor para gestionar clubes y jugadores de deportes mediante sockets TCP con protocolo petición-respuesta.

## Arquitectura

- **Puerto 5000**: Canal de comandos (petición-respuesta por línea)
- **Puerto 5001+**: Canal de datos (ObjectInputStream/ObjectOutputStream)
- **Autenticación**: Usuario: `admin`, Contraseña: `admin`

## Cómo Compilar

```bash
cd src
javac src/*.java
```

## Cómo Ejecutar

### 1. Iniciar el Servidor

```bash
cd src
java -cp . Servidor
```

### 2. Iniciar el Cliente (en otra terminal)

```bash
cd src
java -cp . Cliente
```

## Comandos del Sistema

| Comando                  | Descripción          | Respuesta   |
| ------------------------ | -------------------- | ----------- |
| `<number> USER <nombre>` | Enviar usuario       | OK/FAILED   |
| `<number> PASS <pass>`   | Enviar contraseña    | OK/FAILED   |
| `<number> EXIT`          | Cerrar sesión        | OK          |
| `<number> SESIONES`      | Ver sesiones activas | OK <número> |

## Comandos de CLUBES

| Comando                    | Descripción                                     |
| -------------------------- | ----------------------------------------------- |
| `<number> ADDCLUB`         | Añadir club (envía objeto Club por canal datos) |
| `<number> UPDATECLUB <id>` | Actualizar club                                 |
| `<number> GETCLUB <id>`    | Obtener club                                    |
| `<number> REMOVECLUB <id>` | Eliminar club                                   |
| `<number> LISTCLUBES`      | Listar todos los clubes                         |
| `<number> COUNTCLUBES`     | Contar número de clubes                         |

## Comandos de JUGADORES

| Comando                                             | Descripción                 |
| --------------------------------------------------- | --------------------------- |
| `<number> ADDJUGADOR`                               | Añadir jugador              |
| `<number> GETJUGADOR <id>`                          | Obtener jugador             |
| `<number> REMOVEJUGADOR <id>`                       | Eliminar jugador            |
| `<number> LISTJUGADORES`                            | Listar jugadores            |
| `<number> ADDJUGADOR2CLUB <id_jugador> <id_club>`   | Añadir jugador a club       |
| `<number> REMOVEJUGFROMCLUB <id_jugador> <id_club>` | Quitar jugador de club      |
| `<number> LISTJUGFROMCLUB <id_club>`                | Listar jugadores de un club |

## Protocolo de Comunicación

### Estructura General

```
<number> <comando> [información_adicional]
```

Donde:

- **number**: Identificador único del envío (números o letras)
- **comando**: Acción a ejecutar
- **información_adicional**: Parámetros opcionales

### Respuestas

#### OK (Comando ejecutado correctamente)

```
OK <number> <código> [información]
```

#### PREOK (Espera datos por canal de datos)

```
PREOK <number> 200 <ip> <puerto>
```

#### FAILED (Comando fallido)

```
FAILED <number> <código> [información]
```

### Códigos de Respuesta

- **200**: Éxito
- **400**: Comando no válido
- **401**: Usuario incorrecto
- **403**: No autenticado
- **404**: Recurso no encontrado
- **409**: Conflicto (recurso duplicado)

## Ejemplo de Uso

### Login

```
1 USER admin
→ OK 1 200 Usuario correcto
2 PASS admin
→ OK 2 200 Autenticación correcta
```

### Añadir Club

```
10 ADDCLUB
→ PREOK 10 200 localhost 5001
[Cliente envía objeto Club por puerto 5001]
→ OK 10 200 Transferencia terminada
```

### Listar Clubes

```
14 LISTCLUBES
→ PREOK 14 200 localhost 5001
[Servidor envía ArrayList de Clubs por puerto 5001]
→ OK 14 200 Transferencia terminada
```

## Notas Importantes

- Todos los comandos requieren autenticación previa
- El usuario `admin` con contraseña `admin` tiene acceso a todas las funciones
- Los datos se transfieren por objetos serializados (ObjectStream)
- Las clases `Club` y `Jugador` implementan `Serializable`

## Estructura del Proyecto

```
GestorDeportivo/
├── src/
│   ├── Cliente.java           # Cliente interactivo
│   ├── Servidor.java          # Servidor principal
│   ├── ClienteHandler.java    # Manejador de conexiones
│   ├── Club.java              # Modelo de Club
│   ├── Jugador.java           # Modelo de Jugador
│   └── Datos.java             # Almacenamiento compartido
└── README.md
```

## Autores

Proyecto de gestión deportiva - Sistema cliente-servidor con sockets TCP
