package org.example;

import java.sql.*;
//import java.util.ArrayList;

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
     */
    // La ruta donde se guardará físicamente tu base de datos en local
    private static final String URL_BD = "jdbc:sqlite:./cajon.db";

    static void main(String[] ignoredArgs) {
        // Mensaje de bienvenida
        System.out.println("⚡ ¡Catalogador del Cajón con SQLite activo! ⚡");
        System.out.println("=================================================");

        // 1. Crear la base de datos y la tabla automáticamente si no existen
        boolean existe = crearTablaSiNoExiste();

        if (!existe) {
            // 2. Insertar objetos de prueba en la Base de Datos
            insertarObjeto("Cable HDMI de 2 metros", "Cables", 3);
            insertarObjeto("Pila AA medio gastada", "Baterias", 2);
            insertarObjeto("Destornillador de estrella", "Herramientas", 1);
        }

        // 3. Leer los objetos directamente desde la Base de Datos y mostrarlos
        mostrarCajon();
    }

    private static boolean crearTablaSiNoExiste() {
        String sql = "CREATE TABLE IF NOT EXISTS objetos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT NOT NULL," +
                "categoria TEXT NOT NULL," +
                "cantidad INTEGER NOT NULL" +
                ");";

        String sql2 = "SELECT COUNT(*) FROM objetos;";

        try (Connection conn = DriverManager.getConnection(URL_BD);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            ResultSet rs = stmt.executeQuery(sql2);
            boolean Empty=false;
            while (rs.next()) {
                Empty = true;
            }
            System.out.println("✅ Base de datos lista (Tabla 'objetos' verificada).");
            return Empty;
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            System.out.println("❌ Error al crear la tabla: " + e.getMessage());
            return true;
        }
    }

    private static void insertarObjeto(String nombre, String categoria, int cantidad) {
        String sql = "INSERT INTO objetos(nombre, categoria, cantidad) VALUES(?,?,?)";

        try (Connection conn = DriverManager.getConnection(URL_BD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setString(2, categoria);
            pstmt.setInt(3, cantidad);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("❌ Error al insertar objeto: " + e.getMessage());
        }
    }

    private static void mostrarCajon() {
        String sql = "SELECT nombre, categoria, cantidad FROM objetos";
        System.out.println("\n📦 CONTENIDO REAL DE TU CAJÓN (DESDE SQLITE):");

        try (Connection conn = DriverManager.getConnection(URL_BD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Instanciamos tu clase ObjetoCajon con los datos que devuelve la BD
                ObjetoCajon obj = new ObjetoCajon(
                        rs.getString("nombre"),
                        rs.getString("categoria"),
                        rs.getInt("cantidad")
                );
                System.out.println(obj); // Llama a tu método toString() personalizado
            }
        } catch (Exception e) {
            System.out.println("❌ Error al leer la base de datos: " + e.getMessage());
        }
    }
}