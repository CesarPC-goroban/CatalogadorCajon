# Debes tener un servidor Ollama instalado con minicpm-v (ollama run minicpm-v)

import ollama
import os
import json
from datetime import datetime

if __name__ == "__main__":
    print("⚡ MOTOR DE IA LOCAL MULTIPASADA ACTIVO ⚡")
    print("=================================================")

    ruta_de_mi_foto = "D:/MisFotos/cajon_desastre.jpg"

    # 🌟 Definimos 3 enfoques distintos para obligar a la IA a cambiar de vista
    enfoques = [
        "Identify ONE item located on the LEFT side of the drawer inside the white trays. Specify its color and shape.",
        "Identify ONE item located in the CENTER of the drawer inside the white trays. Specify its color and shape.",
        "Identify ONE item located on the RIGHT side of the drawer inside the white trays. Specify its color and shape."
    ]

    resultados_finales = []

    for i, enfoque in enumerate(enfoques, 1):
        print(f"\n🔄 Pasada {i}/3: Enfocando zona...")

        prompt_instrucciones = (
            f"{enfoque}\n"
            "You MUST write exactly ONE line in this format, nothing else:\n"
            "OBJETO: [objeto y su color en Español] | CATEGORIA: Cocina | CANTIDAD: 1"
        )

        # Reutilizamos tu función modificada para esta pasada libre
        try:
            respuesta = ollama.generate(
                model='minicpm-v',
                prompt=prompt_instrucciones,
                images=[ruta_de_mi_foto],
                options={"temperature": 0.4} # Subimos la temperatura para dar variedad
            )

            linea = respuesta['response'].strip()
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

    print(f"\n💾 ¡Historial combinado guardado con éxito en: {nombre_archivo}")
    print(json.dumps(resultados_finales, indent=4, ensure_ascii=False))

#if __name__ == "__main__":
#    print("⚡ MOTOR DE IA LOCAL OLLAMA ACTIVO (VERSION ROBUSTA) ⚡")
#    print("=================================================")

#    ruta_de_mi_foto = "./Images/cajon-de-cocina.webp"

#    resultados = analizar_foto_con_ollama(ruta_de_mi_foto)

#    print("\n✨ Resultados finales obtenidos de tu IA local:")
#    print(json.dumps(resultados, indent=4, ensure_ascii=False))