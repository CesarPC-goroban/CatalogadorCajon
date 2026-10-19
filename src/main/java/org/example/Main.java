package org.example;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        // Mensaje de bienvenida
        System.out.println("⚡ ¡Bienvenido al Catalogador del Cajón, César! ⚡");
        System.out.println("=================================================");

        // Creamos una lista dinámica en memoria para guardar las cosas
        ArrayList<ObjetoCajon> miCajon = new ArrayList<>();

        // Añadimos 3 objetos de prueba para ver que funciona
        miCajon.add(new ObjetoCajon("Cable HDMI de 2 metros", "Cables", 3));
        miCajon.add(new ObjetoCajon("Pila AA medio gastada", "Baterias", 2));
        miCajon.add(new ObjetoCajon("Destornillador de estrella", "Herramientas", 1));

        // Mostramos el contenido del cajón por consola
        System.out.println("En tu cajón tienes actualmente estos objetos:");
        for (ObjetoCajon objeto : miCajon) {
            //objeto.toString();
            System.out.println(objeto);
            //System.out.println("📦 " + objeto);
        }
    }
}