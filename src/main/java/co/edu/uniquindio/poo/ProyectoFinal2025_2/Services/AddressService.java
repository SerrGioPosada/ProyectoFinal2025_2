package co.edu.uniquindio.poo.ProyectoFinal2025_2.Services;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Address;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.User;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.AddressRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.UserRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;

import java.util.UUID;

/**
 * Service layer for managing user addresses.
 * Handles validation and persistence of address operations.
 */
public class AddressService {

    private static AddressService instance;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    /**
     * Private constructor for Singleton pattern.
     */
    private AddressService() {
        this.addressRepository = AddressRepository.getInstance();
        this.userRepository = UserRepository.getInstance();
    }

    /**
     * Returns the singleton instance of AddressService.
     *
     * @return the unique instance of AddressService
     */
    public static synchronized AddressService getInstance() {
        if (instance == null) {
            instance = new AddressService();
        }
        return instance;
    }

    /**
     * Validates if an alias already exists for a user (case-insensitive).
     *
     * @param user the user whose addresses to check
     * @param alias the alias to validate
     * @param excludeAddressId optional ID of address to exclude from validation (for editing)
     * @return true if alias is duplicate, false otherwise
     */
    public boolean isDuplicateAlias(User user, String alias, String excludeAddressId) {
        if (user.getFrequentAddresses() == null || alias == null) {
            return false;
        }

        String normalizedAlias = alias.trim().toLowerCase();

        return user.getFrequentAddresses().stream()
            .filter(addr -> excludeAddressId == null || !addr.getId().equals(excludeAddressId))
            .anyMatch(addr -> addr.getAlias().trim().toLowerCase().equals(normalizedAlias));
    }

    /**
     * Adds a new address to the user's frequent addresses.
     *
     * @param user the user to add the address to
     * @param address the address to add
     * @throws IllegalArgumentException if alias is duplicate
     */
    public void addAddress(User user, Address address) {
        if (isDuplicateAlias(user, address.getAlias(), null)) {
            throw new IllegalArgumentException("Ya existe una dirección con la etiqueta '" + address.getAlias() + "'");
        }

        // Generate ID if not present
        if (address.getId() == null || address.getId().isEmpty()) {
            address.setId(UUID.randomUUID().toString());
        }

        // Add to user's list
        user.addFrequentAddress(address);

        // Save address to AddressRepository
        addressRepository.addAddress(address);

        // Update user in UserRepository
        userRepository.updateUser(user);

        Logger.info("Address '" + address.getAlias() + "' added for user: " + user.getEmail());
    }

    /**
     * Updates an existing address for a user.
     *
     * @param user the user who owns the address
     * @param oldAddress the address to update
     * @param updatedAddress the updated address data
     * @throws IllegalArgumentException if new alias is duplicate
     */
    public void updateAddress(User user, Address oldAddress, Address updatedAddress) {
        if (isDuplicateAlias(user, updatedAddress.getAlias(), oldAddress.getId())) {
            throw new IllegalArgumentException("Ya existe una dirección con la etiqueta '" + updatedAddress.getAlias() + "'");
        }

        // Keep the same ID
        updatedAddress.setId(oldAddress.getId());

        // Find and replace in user's list
        int index = user.getFrequentAddresses().indexOf(oldAddress);
        if (index >= 0) {
            user.getFrequentAddresses().set(index, updatedAddress);
        }

        // Update in AddressRepository
        addressRepository.addAddress(updatedAddress);

        // Update user in UserRepository
        userRepository.updateUser(user);

        Logger.info("Address '" + updatedAddress.getAlias() + "' updated for user: " + user.getEmail());
    }

    /**
     * Removes an address from the user's frequent addresses.
     *
     * @param user the user who owns the address
     * @param address the address to remove
     */
    public void deleteAddress(User user, Address address) {
        user.getFrequentAddresses().remove(address);

        // Update user in UserRepository
        userRepository.updateUser(user);

        // Note: We don't delete from AddressRepository as it might be used in shipments
        Logger.info("Address '" + address.getAlias() + "' removed for user: " + user.getEmail());
    }
}
