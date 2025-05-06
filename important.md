# 🚗 EV3 Linienfolger – Projektübersicht

Ein autonom fahrender LEGO EV3-Roboter folgt einer schwarzen Linie mithilfe eines Farbsensors.  
Er erkennt, wenn er die Linie verloren hat, dreht sich automatisch zur Wiederfindung und kalibriert sich zu Beginn selbstständig.

---

## 🧠 Hauptfunktionen und Code-Struktur

| Abschnitt | Ort im Code | Zweck |
|----------|--------------|-------|
| **Initialisierung** | `initRobot()` | Startet Motoren und Sensoren, gibt Ton- und LED-Signale. |
| **Kalibrierung** | direkt nach `Button.waitForAnyPress();` | Benutzer stellt Roboter zuerst auf die Linie, dann auf den Hintergrund → automatische Schwellenwert-Berechnung. |
| **Autonomes Fahren** | `while (true) { ... }` | Der Roboter fährt selbstständig der Linie nach – durch Anpassen der Motorgeschwindigkeit je nach Helligkeitswert. |
| **Linienerkennung** | `if (reflected < lineThreshold)` | Wenn reflektiertes Licht unter dem Schwellenwert liegt, erkennt der Roboter die Linie. |
| **Drehung bei Linienverlust** | `if (offLineCounter > 2)` | Wenn die Linie mehrfach nicht erkannt wurde → Drehscan nach rechts, dann links, ggf. nochmal rechts. |
| **Suchbewegung (Scan)** | innerhalb des Dreh-Abschnitts | Der Roboter dreht sich, bis die Linie erneut erkannt wird. Dann richtet er sich neu aus und fährt weiter. |
| **Not-Stopp** | `if (Button.getButtons() != 0)` | Jederzeit durch Tastendruck abbrechbar. |

---

## 📌 Fachbegriffe erklärt

### 🔍 Kalibrierung  
Messung der Helligkeitswerte **über Linie** und **über Hintergrund**, um die Schwellenwerte automatisch zu setzen.  
Erhöht die **Zuverlässigkeit** bei wechselnden Lichtverhältnissen oder verschiedenen Untergründen.

### ♻️ Linienverfolgung (Line Following)  
Der Roboter passt die Geschwindigkeit der linken und rechten Motoren so an, dass er **auf der Linie bleibt**.

### 🔁 Drehung (Suche bei Linienverlust)  
Wenn die Linie nicht mehr erkannt wird, beginnt der Roboter eine **Suchbewegung**, erst rechtsdrehend, dann linksdrehend.

### ⚙️ PID-Regler (optional, nicht enthalten)  
Ein intelligenter Regelmechanismus, um noch **flüssiger und genauer** zu folgen – reagiert auf Stärke, Dauer und Veränderung des Abstands zur Linie.

---

## ✅ Technische Eckdaten

| Eigenschaft | Wert |
|-------------|------|
| **Sensor** | EV3-Farbsensor (RedMode) |
| **Motoren** | 2× EV3 Large Motor (Port B & C) |
| **Kalibrierung** | per Button-Eingabe durch Benutzer |
| **Maximale Geschwindigkeit** | konfigurierbar über `maxSpeed` |
| **Plattform** | LEGO EV3 mit Java (leJOS) |

---

