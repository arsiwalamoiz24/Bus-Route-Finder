# Mumbai Bus Route Navigator

A Java Swing desktop application for finding optimal BEST bus routes across Mumbai. The app uses a modified A\* search algorithm to compute multi-leg routes between any two bus stops in the city, including transfer handling, and visualises the result on a zoned static map of Mumbai.

---

<div align="center">

| Home Screen | Route Results |
|:-----------:|:-------------:|
| <img width="440" alt="Home screen showing stop selector and search button" src="https://github.com/user-attachments/assets/17c3dfb1-ac92-44a1-9dd7-547fdd8d0e13"> | <img width="440" alt="Results screen showing route drawn on map with step-by-step directions" src="https://github.com/user-attachments/assets/3f4d936e-fa5e-4792-b619-1f1ddb530d6b"> |

</div>

---

## About

This project was built as part of a 3rd Semester Object-Oriented Programming lab, and subsequently extended into a fully functional transit tool for Mumbai. It addresses the practical problem of navigating the BEST bus network, which operates over 700 routes and thousands of stops across the city.

The core challenge was building an efficient graph-based routing engine from real transit data, then surfacing the result in a usable interface that non-technical users can interact with.

## Features

- **Autocomplete Stop Search** — Both source and destination fields support live filtering as you type, with a scrollable dropdown that narrows results against the full stop database.
- **Multi-leg Route Finding** — The A\* algorithm traverses a graph of all bus stops and routes to find the optimal path, including routes that require one or more transfers between bus lines.
- **Transfer Penalty** — Each bus change adds a weighted cost to the search, so direct routes are preferred over routes with multiple transfers even when the latter are technically shorter.
- **Zoned Map Visualisation** — The app loads the most relevant static map image (from a set of pre-defined geographic zones) and draws the full route, start/end markers, and transfer points directly on it.
- **Step-by-Step Directions** — A scrollable text panel breaks the journey into legs, listing each stop, the bus number to board, and where to transfer.
- **Background Route Calculation** — Route computation runs on a background thread, keeping the UI responsive throughout.
- **Data-Driven Architecture** — All stop, route, and map data is loaded from CSV files at startup, making it straightforward to update routes or extend to other cities.

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 11+ |
| GUI Framework | Java Swing / AWT |
| Core Algorithm | A\* Search with transfer penalty heuristic |
| Data Structures | `HashMap`, `PriorityQueue`, `ArrayList` |
| File I/O | `BufferedReader`, CSV parsing |
| Transit Data | Mumbai BEST bus network (live transit API) |
| Build | `javac` (no external build tool required) |

## Project Structure

```
BUS_ROUTE_FINDER/
├── Main.java                    Entry point
├── busfinder/
│   ├── MainApp.java             Main application window
│   ├── home.java                Home screen (stop selector)
│   ├── data/
│   │   ├── database.java        CSV loader, graph builder
│   │   ├── BusStop.java         Stop data model
│   │   ├── mapregion.java       Map zone data model
│   │   └── RouteEntry.java      Internal route-entry model
│   ├── gui/
│   │   ├── mappanel.java        Map rendering and route drawing
│   │   ├── routedetailspanel.java  Step-by-step directions panel
│   │   └── SearchableComboBox.java Autocomplete dropdown component
│   └── helpful/
│       └── CoordinateConverter.java  Lat/lon to pixel coordinate mapping
├── assets/                      Map images, icons, background graphics
└── CSV_FILES/
    ├── bus_stops.csv            5,777 BEST bus stops with coordinates
    ├── bus_routes.csv           749 routes with ordered stop sequences
    └── map_regions.csv          Geographic bounds for each map image
```

## How to Run

### Prerequisites

- Java Development Kit (JDK) 11 or higher
- All files cloned together (the `assets/` and `CSV_FILES/` directories must be present alongside the source files)

### Steps

**1. Clone the repository**

```bash
git clone https://github.com/arsiwalamoiz24/Mumbai-Bus-Route-Navigator.git
cd Mumbai-Bus-Route-Navigator
```

**2. Compile**

From the `BUS_ROUTE_FINDER` directory:

```bash
javac Main.java
```

**3. Run**

```bash
java Main
```

> On Windows you can also use the included `compile.bat` and `run.bat` scripts.

## Data

| File | Description |
|---|---|
| `bus_stops.csv` | 5,777 BEST bus stops with disambiguated names and coordinates |
| `bus_routes.csv` | 749 BEST routes with stops ordered geographically from terminal to terminal |
| `map_regions.csv` | Bounding-box definitions that map latitude/longitude ranges to specific PNG image files |

Route stop sequences are ordered by projecting each stop's coordinates onto the vector from the route's start terminal to its end terminal. This removes the dependency on live schedule data for static routing.

## Algorithm

The app builds a directed graph where each node is a bus stop and each edge represents two consecutive stops on the same route. Route finding uses A\* with:

- **Edge cost** — geographic distance between consecutive stops (Haversine formula)
- **Transfer cost** — a fixed penalty added whenever the algorithm switches bus numbers
- **Heuristic** — straight-line Haversine distance from the current stop to the destination

The result is the path with the lowest total weighted cost, which naturally favours fewer transfers and shorter distances.

## Author

**Moiz Arsiwala**
[github.com/arsiwalamoiz24](https://github.com/arsiwalamoiz24) — arsiwala.moiz24@gmail.com

---

If this project was useful or interesting to you, consider giving it a star and forking it to build on top of it.
