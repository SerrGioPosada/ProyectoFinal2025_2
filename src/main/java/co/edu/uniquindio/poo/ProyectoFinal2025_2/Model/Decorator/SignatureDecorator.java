package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Decorator;

import java.util.ArrayList;
import java.util.List;

/**
 * A concrete decorator that adds signature requirement cost to a shipment.
 * <p>Signature required ensures proof of delivery with recipient signature.</p>
 */
public class SignatureDecorator implements CostCalculator {

    private final CostCalculator wrappedCalculator;
    private final double signatureFee;

    /**
     * Constructs a signature requirement decorator.
     * @param wrappedCalculator The cost calculator to wrap
     * @param signatureFee The fixed fee for signature requirement
     */
    public SignatureDecorator(CostCalculator wrappedCalculator, double signatureFee) {
        if (wrappedCalculator == null) {
            throw new IllegalArgumentException("Wrapped calculator cannot be null");
        }
        if (signatureFee < 0) {
            throw new IllegalArgumentException("Signature fee must be non-negative");
        }

        this.wrappedCalculator = wrappedCalculator;
        this.signatureFee = signatureFee;
    }

    /**
     * Constructs a signature requirement decorator with default fee of $8,000.
     * @param wrappedCalculator The cost calculator to wrap
     */
    public SignatureDecorator(CostCalculator wrappedCalculator) {
        this(wrappedCalculator, 8000.0);
    }

    @Override
    public double calculateCost() {
        return wrappedCalculator.calculateCost() + signatureFee;
    }

    @Override
    public String getDescription() {
        return wrappedCalculator.getDescription() + " + Firma Requerida";
    }

    @Override
    public List<CostBreakdownItem> getBreakdown() {
        List<CostBreakdownItem> breakdown = new ArrayList<>(wrappedCalculator.getBreakdown());
        breakdown.add(new CostBreakdownItem("Firma Requerida", signatureFee));
        return breakdown;
    }
}
