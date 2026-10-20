# 📦 Catalogador de Cajón

Un proyecto desarrollado en **Java** utilizando **Maven** para la gestión estructurada y ordenación de objetos cotidianos. Este desarrollo ha sido creado con el objetivo de reactivar, consolidar y actualizar mis conocimientos en Programación Orientada a Objetos (POO) y persistencia de datos.

## 🚀 Características
- **Persistencia con Base de Datos:** Integración con **SQLite** para almacenar los objetos de forma permanente en un archivo físico local (`cajon.db`).
- **Control de Duplicados Inteligente:** Lógica implementada en Java mediante consultas SQL con control de flujo para evitar la inserción repetida de datos en ejecuciones consecutivas.
- **Estructura POO limpia:** Separación de lógica en clases independientes (`Main` y `ObjetoCajon`).
- **Formateo personalizado:** Sobrescritura del método `@Override toString()` para una visualización limpia y organizada de los datos en consola.
- **Gestión moderna y documentada:** Configurado bajo el entorno profesional de **IntelliJ IDEA** utilizando el sistema de construcción **Maven**, incluyendo una guía de configuración para las VM Options en el código.

## 🛠️ Tecnologías utilizadas
- **Lenguaje:** Java 27 (OpenJDK)
- **Base de Datos:** SQLite (vía driver JDBC oficial)
- **Gestor de proyectos:** Maven
- **IDE:** IntelliJ IDEA