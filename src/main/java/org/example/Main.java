package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    /*
        Main ➜ Edit Configurations...
        "+" Add New Configuration
            ↳ Application
        Name: "Main"
        Main Class: "org.example.Main"
        Modify options - Alt + M
            ↳ Java
              ↳ Add VM options
                ↳ "--enable-native-access=ALL-UNNAMED"
        Apply ➜ OK ➜ ➤

        Convertimos el analizador_cajon.py en una API REST:
            primero hay que lanzar "python  analizador_cajon.py"
            luego la aplicación Java
            tiene interfaz gráfica para elegir el cajón
            y también elegir el archivo de imagen del cajón con un FileChooser
     */

    private static final String URL_BD = "jdbc:sqlite:./cajon.db";
    // Endpoint de tu servidor Flask que está corriendo en segundo plano
    private static final String URL_SERVIDOR_PYTHON = "http://localhost:5000/upload-foto";
    // Es test?
    private static final boolean IS_TEST = false;

    static void main(String[] ignoredArgs) {
        System.out.println("⚡ ¡Catalogador del Cajón con SQLite y Ollama Activo! ⚡");
        System.out.println("=================================================");

        if (IS_TEST) {
            // 1. Crear la base de datos y las tablas automáticamente si no existen
            boolean existe = crearTablaSiNoExiste();

            if (existe) {
                // 2. Insertar objetos de prueba en la Base de Datos
                insertarObjeto(1, "Cable HDMI de 2 metros", "Cables", 3);
                insertarObjeto(1, "Pila AA medio gastada", "Baterias", 2);
                insertarObjeto(1, "Destornillador de estrella", "Herramientas", 1);
            }
            if (IS_TEST) {
                // 3. Leer los objetos directamente desde la Base de Datos y mostrarlos
                //mostrarCajon("CajonTest");
                mostrarCajon(1);
            }
        } else {
            // 1. Crear la base de datos y las tablas automáticamente si no existen
            crearTablaSiNoExiste();

            // 🌟 AQUÍ ELIGES EL CAJÓN QUE TÚ QUIERAS POR SU NOMBRE
            // String nombreCajonTrabajo = "CajonDeCocina";
            // 🌟 VENTANA VISUAL 1: Elegir el cajón por teclado de forma gráfica
            String nombreCajonTrabajo = javax.swing.JOptionPane.showInputDialog(
                    null,
                    "Escribe el nombre del cajón que quieres catalogar:",
                    "📦 Selector de Cajón",
                    javax.swing.JOptionPane.QUESTION_MESSAGE
            );

            // Si el usuario cancela o lo deja vacío, salimos limpiamente
            if (nombreCajonTrabajo == null || nombreCajonTrabajo.trim().isEmpty()) {
                System.out.println("🛑 Operación cancelada por el usuario.");
                System.exit(0);
            }

            // 2. Obtenemos el ID real de la base de datos de forma dinámica
            int idCajonActual = obtenerOCrearCajonId(nombreCajonTrabajo);

            // 3. Ruta de la foto real que quieres que analice Ollama en multipasada
            // Cambia esta ruta por la imagen que quieras procesar en este momento
            // String rutaFotoProcesar = "D:/MisFotos/cajon-de-cocina.webp";
            // 🌟 VENTANA VISUAL 3: Elegir la ruta de la foto a procesar
            // String rutaFotoProcesar = javax.swing.JOptionPane.showInputDialog(
            //        null,
            //        "Introduce la ruta absoluta de la imagen (.webp, .jpg, .png):",
            //        "📸 Captura de Imagen",
            //        javax.swing.JOptionPane.QUESTION_MESSAGE,
            //        null,
            //        null,
            //        "D:/MisFotos/cajon-de-cocina.webp" // Ruta por defecto para que no tengas que escribirla entera
            //).toString();

            // 🌟 VENTANA VISUAL 3: Explorador de archivos nativo (JFileChooser)
            javax.swing.JFileChooser selectorArchivos = new javax.swing.JFileChooser();
            selectorArchivos.setDialogTitle("📸 Selecciona la foto del cajón para la IA");

            // Le indicamos que empiece directamente en tu carpeta favorita para ahorrar clics
            selectorArchivos.setCurrentDirectory(new java.io.File("D:/MisFotos"));

            // Filtro para que solo te deje elegir imágenes y no te equivoques de archivo
            javax.swing.filechooser.FileNameExtensionFilter filtroImagenes =
                    new javax.swing.filechooser.FileNameExtensionFilter("Imágenes (*.webp, *.jpg, *.png)", "webp", "jpg", "jpeg", "png");
            selectorArchivos.setFileFilter(filtroImagenes);

            // Abrimos la ventana del explorador de Windows
            int resultadoSeleccion = selectorArchivos.showOpenDialog(null);

            // Si el usuario cierra el explorador o da a cancelar, salimos sin romper nada
            if (resultadoSeleccion != javax.swing.JFileChooser.APPROVE_OPTION) {
                System.out.println("🛑 No se seleccionó ninguna imagen. Operación cancelada.");
                System.exit(0);
            }

            // Recuperamos la ruta del archivo que has seleccionado con el ratón
            String rutaFotoProcesar = selectorArchivos.getSelectedFile().getAbsolutePath();

            System.out.println("\n📸 Iniciando análisis de la imagen: " + rutaFotoProcesar);

            // 4. Enviamos la foto a Python y recibimos el JSON estructurado
            String jsonRespuesta = enviarFotoAPython(rutaFotoProcesar);
            //System.out.println(jsonRespuesta);
            //System.exit(0);

            if (jsonRespuesta != null && !jsonRespuesta.trim().equals("[]")) {
                System.out.println("🤖 IA procesó con éxito. Guardando resultados en SQLite...");

                // 4b. Parseamos el JSON manualmente e insertamos los objetos en el Cajon (ID: 1)
                parsearEInsertarJson(jsonRespuesta, idCajonActual);
            } else {
                System.out.println("⚠️ No se pudieron obtener objetos de la IA. Saltando inserción automática.");
            }

            // 5. Leer los objetos directamente desde la Base de Datos y mostrarlos
            mostrarCajon(idCajonActual);
        }
    }

    /**
     * Crea el Cajón
     */
    private static int obtenerOCrearCajonId(String nombreCajon) {
        String sqlBuscar = "SELECT id FROM cajones WHERE nombre = ?";
        String sqlInsertar = "INSERT INTO cajones(nombre) VALUES(?)";

        try (Connection conn = DriverManager.getConnection(URL_BD)) {
            // 1. Intentamos buscar si ya existe el cajón con ese nombre
            try (PreparedStatement pstmtBuscar = conn.prepareStatement(sqlBuscar)) {
                pstmtBuscar.setString(1, nombreCajon);
                try (ResultSet rs = pstmtBuscar.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("id"); // ¡Encontrado! Devolvemos su ID
                    }
                }
            }

            // 2. Si no existe, lo creamos dinámicamente
            try (PreparedStatement pstmtInsertar = conn.prepareStatement(sqlInsertar, Statement.RETURN_GENERATED_KEYS)) {
                pstmtInsertar.setString(1, nombreCajon);
                pstmtInsertar.executeUpdate();

                try (ResultSet generatedKeys = pstmtInsertar.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int nuevoId = generatedKeys.getInt(1);
                        System.out.println("📦 Se ha creado un nuevo cajón en la BD: \"" + nombreCajon + "\" (ID: " + nuevoId + ")");
                        return nuevoId;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error al gestionar el ID del cajón: " + e.getMessage());
        }
        return 1; // Por seguridad, si algo falla devuelve el ID 1 por defecto
    }

    /**
     * Envía la imagen como bytes al servidor Flask en Python
     */
    private static String enviarFotoAPython(String rutaFoto) {
        try {
            Path path = Path.of(rutaFoto);
            if (!Files.exists(path)) {
                System.err.println("❌ Error: El archivo de imagen no existe en la ruta: " + rutaFoto);
                return null;
            }

            byte[] bytesImagen = Files.readAllBytes(path);
            HttpClient cliente = HttpClient.newHttpClient();

            HttpRequest peticion = HttpRequest.newBuilder()
                    .uri(URI.create(URL_SERVIDOR_PYTHON))
                    .header("Content-Type", "application/octet-stream")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(bytesImagen))
                    .build();

            System.out.println("🔄 Comunicando con el servidor Flask (esperando 3 pasadas de Ollama)...");
            HttpResponse<String> respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());

            if (respuesta.statusCode() == 200) {
                return respuesta.body();
            } else {
                System.err.println("❌ Python respondió con error HTTP: " + respuesta.statusCode());
                System.err.println("Detalle: " + respuesta.body());
                return null;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Fallo de conexión con Python: " + e.getMessage());
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * Parsea el JSON crudo del servidor de Python sin dependencias externas
     * e inserta cada objeto directamente en la base de datos SQLite
     */
    private static void parsearEInsertarJson(String json, int idCajon) {
        int contadorInsertados = 0;

        try {
            String jsonLimpio = json.trim();

            // 1. Buscamos cada bloque de objeto individual { ... }
            Pattern pattern = Pattern.compile("\\{[^\\}]+\\}");
            Matcher matcher = pattern.matcher(jsonLimpio);

            while (matcher.find()) {
                String bloqueObjeto = matcher.group();

                // 2. Extraemos cada clave por separado (así da igual el orden en el que vengan)
                String nombreRaw = extraerRegex(bloqueObjeto, "\"nombre\"\\s*:\\s*\"([^\"]+)\"");
                String categoria = extraerRegex(bloqueObjeto, "\"categoria\"\\s*:\\s*\"([^\"]+)\"");
                String cantidadStr = extraerRegex(bloqueObjeto, "\"cantidad\"\\s*:\\s*(\\d+)");

                if (nombreRaw != null && categoria != null && cantidadStr != null) {
                    // 3. Limpiamos los caracteres Unicode como \u00e1 para que en SQLite se guarde con tildes reales
                    String nombre = unescapeUnicode(nombreRaw);
                    int cantidad = Integer.parseInt(cantidadStr);

                    // 4. Inserción directa en la base de datos relacional
                    insertarObjeto(idCajon, nombre, categoria, cantidad);
                    contadorInsertados++;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error crítico en el analizador de Java: " + e.getMessage());
        }

        System.out.println("✅ Se procesaron e insertaron " + contadorInsertados + " objetos nuevos en el cajón.");
    }

    /**
     * Método auxiliar interno para aislar propiedades de texto en el JSON
     */
    private static String extraerRegex(String texto, String expresion) {
        Pattern p = Pattern.compile(expresion, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(texto);
        if (m.find()) {
            return m.group(1).trim();
        }
        return null;
    }

    /**
     * Convierte secuencias Unicode estilo \u00e1 en caracteres legibles en Español
     */
    private static String unescapeUnicode(String texto) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < texto.length()) {
            char ch = texto.charAt(i);
            if (ch == '\\' && i + 1 < texto.length() && texto.charAt(i + 1) == 'u') {
                String hex = texto.substring(i + 2, i + 6);
                sb.append((char) Integer.parseInt(hex, 16));
                i += 6;
            } else {
                sb.append(ch);
                i++;
            }
        }
        return sb.toString();
    }

    private static void parsearEInsertarJson2(String json, int idCajon) {
        // Expresión regular para capturar los bloques de objetos { ... } en el JSON array
        Pattern pattern = Pattern.compile("\\{\\s*\"nombre\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"categoria\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"cantidad\"\\s*:\\s*(\\d+)\\s*\\}");
        Matcher matcher = pattern.matcher(json);

        int contadorInsertados = 0;

        while (matcher.find()) {
            String nombre = matcher.group(1);
            String categoria = matcher.group(2);
            int cantidad = Integer.parseInt(matcher.group(3));

            // Llamamos a tu método existente para meterlo en la base de datos relacional
            insertarObjeto(idCajon, nombre, categoria, cantidad);
            contadorInsertados++;
        }
        System.out.println("✅ Se procesaron e insertaron " + contadorInsertados + " objetos nuevos en el cajón.");
    }

    private static boolean crearTablaSiNoExiste() {
        String sql = "CREATE TABLE IF NOT EXISTS objetos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT NOT NULL," +
                "categoria TEXT NOT NULL," +
                "cantidad INTEGER NOT NULL" +
                ");";
        String sql2 = "CREATE TABLE IF NOT EXISTS cajones (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT NOT NULL" +
                ");";
        String sql2_3 = "SELECT COUNT(*) FROM cajones;";
        String sql2_5 = "INSERT INTO cajones(nombre) VALUES(?)";
        String sql3 = "CREATE TABLE IF NOT EXISTS cajones_objetos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_cajon INTEGER NOT NULL," +
                "id_objeto INTEGER NOT NULL," +
                "CONSTRAINT fk_cajon FOREIGN KEY (id_cajon) REFERENCES cajones(id) ON DELETE CASCADE,"+
                "CONSTRAINT fk_objeto FOREIGN KEY (id_objeto) REFERENCES objetos(id) ON DELETE CASCADE"+
                ");";

        String sql4 = "SELECT COUNT(*) FROM objetos;";

        try (Connection conn = DriverManager.getConnection(URL_BD);
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            stmt.execute(sql2);
            stmt.execute(sql3);

            try (ResultSet rsC = stmt.executeQuery(sql2_3)) {
                if (rsC.next()) {
                    int totalCajones = rsC.getInt(1);
                    if (totalCajones == 0) {
                        try (PreparedStatement pstmt = conn.prepareStatement(sql2_5)) {
                            pstmt.setString(1, "CajonTest");
                            pstmt.executeUpdate();
                        }
                    }
                }
            }

            boolean Empty = false;
            try (ResultSet rs = stmt.executeQuery(sql4)) {
                if (rs.next()) {
                    int total = rs.getInt(1);
                    Empty = (total == 0);
                }
            }
            System.out.println("✅ Base de datos lista (Tablas relacionales verificadas).");
            return Empty;
        } catch (Exception e) {
            System.out.println("❌ Error al crear la tabla: " + e.getMessage());
            return true;
        }
    }

    private static void insertarObjeto(int idCajon, String nombre, String categoria, int cantidad) {
        String sql = "INSERT INTO objetos(nombre, categoria, cantidad) VALUES(?,?,?)";
        String sqlIntermedia = "INSERT INTO cajones_objetos(id_cajon, id_objeto) VALUES(?,?)";

        int idObjetoGenerado = -1;

        try (Connection conn = DriverManager.getConnection(URL_BD);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { // Habilitado RETURN_GENERATED_KEYS para recuperar el ID
            pstmt.setString(1, nombre);
            pstmt.setString(2, categoria);
            pstmt.setInt(3, cantidad);
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    idObjetoGenerado = generatedKeys.getInt(1);
                }
            }
            if (idObjetoGenerado != -1) {
                try (PreparedStatement pstmtIntermedia = conn.prepareStatement(sqlIntermedia)) {
                    pstmtIntermedia.setInt(1, idCajon);
                    pstmtIntermedia.setInt(2, idObjetoGenerado);
                    pstmtIntermedia.executeUpdate();
                    System.out.println("   ↳ Objeto \"" + nombre + "\" indexado en el cajón correctamente.");
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error al insertar objeto: " + e.getMessage());
        }
    }

    private static void mostrarCajon(int idCajon) {
        String sql = "SELECT cajones.nombre AS nombrec, objetos.nombre AS nombreo, categoria, cantidad " +
                "FROM cajones_objetos " +
                "INNER JOIN cajones ON cajones_objetos.id_cajon = cajones.id " +
                "INNER JOIN objetos ON cajones_objetos.id_objeto = objetos.id " +
                "WHERE cajones.id = '" + idCajon + "'";
        System.out.println("\n📦 CONTENIDO REAL DE TU CAJÓN (DESDE SQLITE):");

        try (Connection conn = DriverManager.getConnection(URL_BD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ObjetoCajon obj = new ObjetoCajon(
                        rs.getString("nombreo"),
                        rs.getString("categoria"),
                        rs.getInt(  "cantidad")
                );
                System.out.println(rs.getString("nombrec") + " " + obj);
            }
        } catch (Exception e) {
            System.out.println("❌ Error al leer la base de datos: " + e.getMessage());
        }
    }
}