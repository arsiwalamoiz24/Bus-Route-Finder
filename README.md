# 🚌 Mumbai Bus Route Navigator

**A Java Swing desktop application for finding optimal BEST bus routes across Mumbai, built for the 3rd Semester Object-Oriented Programming lab.**

## 📸 Screenshots

| Homepage (Input) | Results Screen (Map & Details) |
| :---: | :---: |
| <img width="450" alt="Homepage" src="https://github.com/user-attachments/assets/17c3dfb1-ac92-44a1-9dd7-547fdd8d0e13"> | <img width="450" alt="Results" src="https://github.com/user-attachments/assets/3f4d936e-fa5e-4792-b619-1f1ddb530d6b"> |

## 📖 About This Project

This project is a desktop application designed to help users navigate Mumbai's complex BEST bus network. It allows a user to select a starting bus stop and a destination from dropdown lists. The application then calculates an optimal route, which can include transfers between different bus lines.

The final route is presented in two ways:
* **Visually:** Drawn as a red line on a static map of the relevant Mumbai area.
* **Textually:** As a step-by-step list of stops, bus numbers, and clearly marked transfer points.

## ✨ Features

* **Optimal Route Finding:** Uses a modified A\* search algorithm to find an efficient path.
* **Transfer Handling:** The A\* algorithm applies a penalty for each bus transfer, prioritizing routes with fewer changes.
* **Visual Map Display:** Loads and displays static map images (`.png` files) and draws the calculated route, start points, end points, and transfer stops.
* **Text Directions:** Shows a detailed, scrollable list of directions, broken down by "Legs" for each bus.
* **Data-Driven:** All stop, route, and map data is loaded on startup from local CSV files.
* **Custom UI:** A custom-built homepage (using `null` layout for specific design) for selecting stops.

## 🛠️ Technologies Used

* **Core:** Java
* **GUI:** Java Swing & AWT
* **Core Algorithm:** A\* Search (implemented manually)
* **Data Structures:** `List`, `Map` (using `HashMap`), `PriorityQueue`
* **File Handling:** `BufferedReader`, `FileReader`, and String manipulation for CSV parsing.
* **Code Organization:** Java Packages (`busfinder`, `busfinder.data`, `busfinder.gui`, `busfinder.helpful`).

## 🚀 How to Run

1.  **Clone the repository:**
    In your Bash terminal, run the command:
    ```bash
    git clone [https://github.com/arsiwalamoiz24/Mumbai-Bus-Route-Navigator.git](https://github.com/arsiwalamoiz24/Mumbai-Bus-Route-Navigator.git)
    ```

2.  **Navigate to the project directory:**
    ```bash
    cd Mumbai-Bus-Route-Navigator
    ```

3.  **Ensure Data Files are Present:**
    Make sure the following files are in the **root** of the `Mumbai-Bus-Route-Navigator` directory (alongside the `busfinder` folder):
    * `Masterfile_mumbai_bus_stops.csv`
    * `(TEMPERARY)bus_routes_fully_corrected.csv`
    * `map_regions.csv`
    * All map images (e.g., `map1.png`, `map2.png`, etc.)
    * All icon images (e.g., `bus icon.png`, `background.png`, `animated bus.gif`)

---

## ⚠️ Known Issue: Stops with Commas

> **For some reason, stops with commas in their names are not processed correctly by the pathfinding algorithm.**
>
> While these stops load and display in the dropdowns, selecting one as a start or end point will result in a "No route found" error. Please **do not use stops with commas in their names** for now until a solution for this is found.
>
> If you are able to find a solution, please let me know or open an issue!

---

4.  **Compile the Code:**
    From the root directory (`Mumbai-Bus-Route-Finder`), run the `javac` command. This command compiles all `.java` files within their packages.
    ```bash
    javac busfinder/*.java busfinder/data/*.java busfinder/gui/*.java busfinder/helpful/*.java
    ```

5.  **Run the Application:**
    Run the `home` class (your main app) using its fully qualified name:
    ```bash
    java busfinder.home
    ```

## 📄 A Note on Data Files

The `Masterfile_mumbai_bus_stops.csv` file is a filtered version of the file named `(KEEP THIS)ORIGINAL_Masterfile_mumbai_bus_stops.csv`. It only includes stops that are actively mapped to at least one bus route in the `(TEMPERARY)bus_routes_fully_corrected.csv` file. The original file is included for reference.

## 👨‍💻 Author

* **Moiz Arsiwala** - [arsiwalamoiz24](https://github.com/arsiwalamoiz24)  - Email: moiz.arsiwala24@gmail.com
