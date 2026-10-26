from flask import Flask, request, jsonify
import ollama
import json
from datetime import datetime

app = Flask(__name__)

@app.route('/upload-foto', methods=['POST'])
def analizar_cajon_endpoint():
    try:
        # 1. Validar que nos lleguen los bytes de la imagen desde Java
        if not request.data:
            return jsonify({"status": "error", "message": "No se recibieron datos de la imagen"}), 400

        image_bytes = request.data
        print("\n⚡ Petición recibida: Iniciando motor de IA local multipasada... ⚡")

        # 🌟 Tus 3 enfoques para obligar a la IA a cambiar de vista
        enfoques = [
            "Identify ONE item located on the LEFT side of the drawer inside the white trays. Specify its color and shape.",
            "Identify ONE item located in the CENTER of the drawer inside the white trays. Specify its color and shape.",
            "Identify ONE item located on the RIGHT side of the drawer inside the white trays. Specify its color and shape."
        ]

        resultados_finales = []

        # 2. Bucle de 3 pasadas usando los bytes en memoria
        for i, enfoque in enumerate(enfoques, 1):
            print(f"🔄 Pasada {i}/3: Enfocando zona...")

            prompt_instrucciones = (
                f"{enfoque}\n"
                "You MUST write exactly ONE line in this format, nothing else:\n"
                "OBJETO: [objeto y su color en Español] | CATEGORIA: Cocina | CANTIDAD: 1"
            )

            try:
                respuesta = ollama.generate(
                    model='minicpm-v',
                    prompt=prompt_instrucciones,
                    images=[image_bytes],  # <-- Pasamos directamente los bytes que envió Java
                    options={"temperature": 0.4}
                )

                linea = respuesta['response'].strip()
                print(f"   ↳ IA respondió: {linea}")

                if "OBJETO:" in linea and "CATEGORIA:" in linea:
                    partes = linea.split("|")
                    nombre = partes[0].replace("OBJETO:", "").strip()
                    categoria = partes[1].replace("CATEGORIA:", "").strip()
                    cantidad = int(partes[2].replace("CANTIDAD:", "").strip())

                    resultados_finales.append({
                        "nombre": nombre,
                        "categoria": categoria,
                        "cantidad": cantidad
                    })
            except Exception as e:
                print(f"⚠️ Error en pasada {i}: {e}")

        # 🗓️ Guardamos el JSON combinado final con tu marca de tiempo
        from datetime import datetime
        ahora = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
        nombre_archivo = f"./raw_response_{ahora}.txt"

        with open(nombre_archivo, "w", encoding="utf-8") as f:
            f.write(json.dumps(resultados_finales, indent=4, ensure_ascii=False))
        print(f"💾 Copia de seguridad guardada en: {nombre_archivo}")

        # 3. Retornar los resultados en formato JSON directo a Java
        # Java recibirá exactamente la lista estructurada lista para SQLite
        return jsonify(resultados_finales), 200

    except Exception as e:
        print(f"❌ Error crítico en el servidor: {e}")
        return jsonify({"status": "error", "message": str(e)}), 500

if __name__ == "__main__":
    print("🚀 Servidor Flask escuchando en http://localhost:5000/upload-foto")
    app.run(host='0.0.0.0', port=5000, debug=True)