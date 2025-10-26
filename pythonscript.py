import csv
import os

# --- File Names ---
master_stops_file = '(KEEP THIS)ORIGINAL_Masterfile_mumbai_bus_stops.csv'
routes_file = '(TEMPERARY)bus_routes_fully_corrected.csv'
output_file = 'Filtered_mumbai_bus_stops.csv'
# --- Column Indices (0-based) ---
# In routes file, which column contains the stop name?
route_stop_name_column_index = 2
# In master file, how many columns from the end are latitude and longitude?
master_lat_cols_from_end = 2
master_lon_cols_from_end = 1
# --- End Configuration ---

# Set to store unique stop names found in the routes file (case-insensitive)
valid_stop_names = set()

print(f"Reading stop names from: {routes_file}")
try:
    with open(routes_file, mode='r', encoding='utf-8', newline='') as infile:
        reader = csv.reader(infile)
        header = next(reader) # Skip the header row
        processed_count = 0
        for row in reader:
            if len(row) > route_stop_name_column_index:
                # Get the stop name, handling potential quotes automatically by csv module
                stop_name = row[route_stop_name_column_index].strip()
                if stop_name:
                    valid_stop_names.add(stop_name.upper()) # Store uppercase for matching
                    processed_count += 1
        print(f"-> Found {len(valid_stop_names)} unique stop names in {processed_count} route entries.")
except FileNotFoundError:
    print(f"!! ERROR: Routes file not found: {routes_file}")
    exit()
except Exception as e:
    print(f"!! ERROR reading routes file: {e}")
    exit()

if not valid_stop_names:
    print("!! WARNING: No stop names found in routes file. Output file will only contain header.")
    # You might want to exit() here if an empty file isn't useful

print(f"\nFiltering master stops file: {master_stops_file}")
stops_written = 0
master_header = []

try:
    with open(master_stops_file, mode='r', encoding='utf-8', newline='') as infile, \
         open(output_file, mode='w', encoding='utf-8', newline='') as outfile:

        reader = csv.reader(infile)
        writer = csv.writer(outfile)

        # Read and write the header from the master file
        master_header = next(reader)
        writer.writerow(master_header)
        stops_written += 1

        # Process each stop in the master file
        for row in reader:
            # Check if row has enough columns (at least name + lat + lon)
            if len(row) >= master_lat_cols_from_end + 1:
                 # Extract name: Assume name is in the first column, handled by csv module
                 master_name = row[0].strip()

                 # Alternative extraction if name might have unquoted commas:
                 # name_parts = row[:-master_lat_cols_from_end] # Get all parts before latitude
                 # master_name = ",".join(name_parts).strip()

                 # Check if this stop name exists in the set from the routes file (case-insensitive)
                 if master_name and master_name.upper() in valid_stop_names:
                    writer.writerow(row) # Write the original row to the output
                    stops_written += 1
            else:
                print(f"  - Skipping short row in master file: {row}")


except FileNotFoundError:
    print(f"!! ERROR: Master stops file not found: {master_stops_file}")
    exit()
except Exception as e:
    print(f"!! ERROR processing master file or writing output: {e}")
    exit()

print(f"\nFinished. Wrote {stops_written} rows (including header) to {output_file}")