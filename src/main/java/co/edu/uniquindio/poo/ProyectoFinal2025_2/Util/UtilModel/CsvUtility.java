package co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * A utility class with static methods to handle the creation of CSV files.
 */
public class CsvUtility {

    /**
     * Writes data to a CSV file.
     *
     * @param filePath The path where the CSV file will be saved
     * @param headers  The column headers
     * @param rows     The data rows (each inner list represents a row)
     * @return The created File object
     * @throws IOException If writing fails
     */
    public static File writeCSV(String filePath, List<String> headers, List<List<String>> rows) throws IOException {
        File file = new File(filePath);
        file.getParentFile().mkdirs(); // Create parent directories if needed

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Write headers
            writer.println(String.join(",", headers));

            // Write rows
            for (List<String> row : rows) {
                writer.println(String.join(",", row.stream()
                        .map(CsvUtility::escapeCSV)
                        .toList()));
            }
        }

        return file;
    }

    /**
     * Escapes a CSV field by wrapping it in quotes if it contains special characters.
     */
    private static String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
