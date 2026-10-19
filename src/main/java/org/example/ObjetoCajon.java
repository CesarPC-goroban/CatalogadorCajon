package org.example;

public class ObjetoCajon {
    // 1. Atributos (Las características de lo que guardamos)
    private String nombre;
    private String categoria; // Ejemplo: "Electrónica", "Herramientas", "Cables"
    private int cantidad;

    // 2. Constructor (Para crear el objeto en memoria)
    public ObjetoCajon(String nombre, String categoria, int cantidad) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.cantidad = cantidad;
    }

    // 3. Getters y Setters (Para leer y modificar los datos de forma segura)
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    // 4. Un método especial para imprimir el objeto bonito en la consola
    @Override
    public String toString() {
        return "📦 [" + categoria.toUpperCase() + "] " + nombre + " (Cantidad: " + cantidad + ")";
    }
}
