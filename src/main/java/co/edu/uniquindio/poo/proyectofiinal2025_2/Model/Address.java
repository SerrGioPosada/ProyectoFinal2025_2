package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import java.util.Objects;

/**
 * Represents a physical address, which can be labeled with an alias.
 * <p>
 * Contains details such as street, city, state, country, and postal code.
 * Implements equals() and hashCode() for value-based comparison, which is essential
 * for validation logic (e.g., ensuring origin and destination are not the same).
 * </p>
 */
public class Address {

    private String alias; // e.g., "Home", "Office"
    private String street;
    private String city;
    private String state;
    private String country;
    private String zipCode;

    /**
     * Constructs a new Address with the provided data.
     *
     * @param alias    A user-friendly name for the address.
     * @param street   The street name and number.
     * @param city     The city of the address.
     * @param state    The state or province of the address.
     * @param country  The country of the address.
     * @param zipCode  The postal code.
     */
    public Address(String alias, String street, String city, String state, String country, String zipCode) {
        this.alias = alias;
        this.street = street;
        this.city = city;
        this.state = state;
        this.country = country;
        this.zipCode = zipCode;
    }

    // ======================
    // Getters and Setters
    // ======================

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    // ======================
    // Overridden Methods
    // ======================

    @Override
    public String toString() {
        return "'" + alias + "': " + street + ", " + city + ", " + state;
    }

    /**
     * Compares this address to another object for equality.
     * Two addresses are considered equal if their street, city, state, country, and zip code are the same.
     * The alias is ignored in the comparison.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(street, address.street) &&
               Objects.equals(city, address.city) &&
               Objects.equals(state, address.state) &&
               Objects.equals(country, address.country) &&
               Objects.equals(zipCode, address.zipCode);
    }

    /**
     * Generates a hash code for the address based on its core fields.
     * The alias is excluded from the hash code calculation.
     */
    @Override
    public int hashCode() {
        return Objects.hash(street, city, state, country, zipCode);
    }
}
