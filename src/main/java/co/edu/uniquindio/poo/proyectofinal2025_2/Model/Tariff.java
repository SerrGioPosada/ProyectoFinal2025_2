package co.edu.uniquindio.poo.proyectofinal2025_2.Model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>Defines the pricing rules and tariffs for calculating shipping costs.</p>
 * <p>This class holds all the variables required to calculate the final cost of a shipment,
 * including base costs, per-kilometer rates, per-kilogram rates, and surcharges for
 * different priority levels.</p>
 */
@Getter
@Setter
@ToString
public class Tariff {

    private String id;
    private String description;
    private double baseCost;
    private double costPerKilometer;
    private double costPerKilogram;
    private double prioritySurcharge;

    /**
     * Default constructor.
     */
    public Tariff() {
    }

    /**
     * Private constructor for the builder pattern.
     * @param builder The builder instance to construct from.
     */
    private Tariff(Builder builder) {
        this.id = builder.id;
        this.description = builder.description;
        this.baseCost = builder.baseCost;
        this.costPerKilometer = builder.costPerKilometer;
        this.costPerKilogram = builder.costPerKilogram;
        this.prioritySurcharge = builder.prioritySurcharge;
    }

    // ======================================
    //               BUILDER
    // ======================================

    /**
     * Static builder class for creating Tariff instances.
     */
    public static class Builder {
        private String id;
        private String description;
        private double baseCost;
        private double costPerKilometer;
        private double costPerKilogram;
        private double prioritySurcharge;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withBaseCost(double baseCost) {
            this.baseCost = baseCost;
            return this;
        }

        public Builder withCostPerKilometer(double costPerKilometer) {
            this.costPerKilometer = costPerKilometer;
            return this;
        }

        public Builder withCostPerKilogram(double costPerKilogram) {
            this.costPerKilogram = costPerKilogram;
            return this;
        }

        public Builder withPrioritySurcharge(double prioritySurcharge) {
            this.prioritySurcharge = prioritySurcharge;
            return this;
        }

        /**
         * Creates a new Tariff instance from the builder's properties.
         * @return A new Tariff instance.
         */
        public Tariff build() {
            return new Tariff(this);
        }
    }
}
