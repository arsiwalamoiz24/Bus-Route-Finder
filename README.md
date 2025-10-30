🚌 Mumbai Bus Route Navigator
A Java Swing desktop application for finding optimal BEST bus routes across Mumbai, built for our 3rd Semester Object-Oriented Programming lab.

📖 About This Project
This project is a desktop application designed to help users navigate Mumbai's complex BEST bus network. It allows a user to select a starting bus stop and a destination from dropdown lists. The application then calculates an optimal route, which can include transfers between different bus lines.The final route is presented in two ways:Visually: Drawn as a red line on a static map of the relevant Mumbai area.Textually: As a step-by-step list of stops, bus numbers, and clearly marked transfer points.

✨ Features
Optimal Route Finding: Uses a modified A* search algorithm to find an efficient path.

Transfer Handling: The A* algorithm applies a penalty for each bus transfer, prioritizing routes with fewer changes.

Visual Map Display: Loads and displays static map images (.png files) and draws the calculated route, start points, end points, and transfer stops.

Text Directions: Shows a detailed, scrollable list of directions, broken down by "Legs" for each bus.

Data-Driven: All stop, route, and map data is loaded on startup from local CSV files.

Custom UI: A custom-built homepage (using null layout for specific design) for selecting stops.

🛠️ Technologies Used
Core: Java

GUI: Java Swing & AWT

Core Algorithm: A* Search (implemented manually)

Data Structures: List, Map (using HashMap), PriorityQueue

File Handling: BufferedReader, FileReader, and String manipulation for CSV parsing.

Code Organization: Java Packages (busfinder, busfinder.data, busfinder.gui, busfinder.util).

How to Run
Clone the repository:

Bash

git clone https://github.com/arsiwalamoiz24/Mumbai-Bus-Route-Navigator.git

Navigate to the project directory:

Bash

cd Mumbai-Bus-Route-Navigator

Ensure Data Files are Present: Make sure the following files are in the root of the Mumbai-Bus-Route-Navigator directory (alongside the busfinder folder):

Masterfile_mumbai_bus_stops.csv

(TEMPERARY)bus_routes_fully_corrected.csv

map_regions.csv

All map images (e.g., map1.png, map2.png, etc.)

All icon images (e.g., bus icon.png, background.png, animated bus.gif)

Compile the Code: From the root directory (Mumbai-Bus-Route-Finder), run the javac command, pointing to all your .java files within their packages.

Bash

javac javac busfinder/home.java busfinder/data/*.java busfinder/gui/*.java busfinder/helpful/*.java
Run the Application: Run the main class (your home.java) using its fully qualified name (package + class name).

Bash

java busfinder.home