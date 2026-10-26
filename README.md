# 📦 Catalogador de Cajón Inteligente (Multilenguaje & IA)

Un ecosistema híbrido desarrollado en **Java** y **Python** para la gestión estructurada, ordenación y catalogación automática de objetos cotidianos mediante Inteligencia Artificial local.

Este proyecto conecta una aplicación de escritorio nativa con un modelo de visión artificial multimodal para desglosar y persistir el contenido de cualquier espacio de forma relacional.

## 🚀 Características
- **Análisis Inteligente Multipasada:** Conexión mediante una **API REST (HTTP POST)** con un servidor **Flask** en Python que interactúa con **Ollama** de forma local. Utiliza el modelo de visión `minicpm-v` en 3 pasadas independientes (izquierda, centro, derecha) para un desglose exhaustivo de las imágenes.
- **Persistencia Relacional Avanzada:** Integración con **SQLite** (`cajon.db`) utilizando una arquitectura de tablas relacionales (`objetos`, `cajones` y la tabla intermedia de unión `cajones_objetos`) con integridad referencial (`ON DELETE CASCADE`).
- **Interfaz Gráfica Interactiva (GUI):** Entrada dinámica de datos mediante ventanas emergentes de `JOptionPane` para la gestión y creación automatizada de cajones independientes en la base de datos.
- **Explorador de Archivos Nativo:** Integración con `JFileChooser` para seleccionar las imágenes de los cajones interactivamente con el ratón, eliminando la necesidad de escribir rutas manualmente.
- **Caja Negra de Seguridad:** El servidor de Python almacena automáticamente copias de seguridad en archivos `.txt` con marcas de tiempo (`YYYY-MM-DD_HH-MM-SS`) con las respuestas JSON crudas generadas por la IA local.
- **Estructura limpia y robusta:** Parser JSON nativo optimizado mediante expresiones regulares inmune al orden de las claves y con un decodificador Unicode para asegurar el correcto formateo de tildes en Español dentro de la base de datos.

## 🛠️ Tecnologías utilizadas
- **Mundo Java:** Java 27 (OpenJDK), Maven, JDBC (Driver oficial de SQLite), Java Swing (`JFileChooser`, `JOptionPane`).
- **Mundo Python:** Python 3.x, Flask (Servidor API REST), Librería oficial de Ollama.
- **Inteligencia Artificial:** Ollama Local (Modelo multimodal `minicpm-v`).
- **Base de Datos:** SQLite 3.

## ⚙️ Arquitectura del Flujo
1. **Python (Servidor):** Se levanta el script `analizador_cajon.py` quedando a la escucha en el puerto `5000`.
2. **Java (Cliente):** Se ejecuta `Main.java`, el cual solicita el nombre del cajón y abre el explorador de archivos para elegir la imagen.
3. **Petición HTTP:** Java transmite la imagen como un flujo binario puro (`application/octet-stream`) hacia Python.
4. **Procesamiento Local:** Python procesa los bytes con Ollama, guarda un reporte local y devuelve un JSON estructurado.
5. **Persistencia:** Java recibe el JSON, lo limpia e indexa relacionalmente los nuevos objetos mapeados en la base de datos.
