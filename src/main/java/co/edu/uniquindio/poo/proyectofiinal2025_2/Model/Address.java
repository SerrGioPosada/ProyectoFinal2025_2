package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

/**
 * Represents a physical address associated with a user or shipment.
 * <p>
 * Contains details such as street, city, state, country, and postal code.
 * </p>
 */
public class Address {

    private String street;
    private String city;
    private String state;
    private String country;
    private String zipCode;

    /**
     * Constructs a new Address with the provided data.
     *
     * @param street   street name and number
     * @param city     city of the address
     * @param state    state/province of the address
     * @param country  country of the address
     * @param zipCode  postal code
     */
    public Address(String street, String city, String state, String country, String zipCode) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.country = country;
        this.zipCode = zipCode;
    }

    // ======================
    // Getters
    // ======================

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public String getZipCode() {
        return zipCode;
    }

    // ======================
    // Setters
    // ======================

    public void setStreet(String street) {
        this.street = street;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    // ======================
    // ToString
    // ======================

    @Override
    public String toString() {
        return street + ", " + city + ", " + state + ", " + country + " (" + zipCode + ")";
    }
}
