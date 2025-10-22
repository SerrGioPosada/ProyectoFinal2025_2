package co.edu.uniquindio.poo.proyectofiinal2025_2.Model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for chart data points.
 * <p>
 * This DTO represents a single data point for charts,
 * with a label and a numeric value.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataDTO {

    /**
     * The label for this data point (e.g., date, category name)
     */
    private String label;

    /**
     * The numeric value for this data point
     */
    private double value;

    /**
     * Optional color for the data point (for custom styling)
     */
    private String color;

    /**
     * Optional additional description
     */
    private String description;

    /**
     * Constructor with label and value only
     * @param label The data point label
     * @param value The data point value
     */
    public ChartDataDTO(String label, double value) {
        this.label = label;
        this.value = value;
    }
}
