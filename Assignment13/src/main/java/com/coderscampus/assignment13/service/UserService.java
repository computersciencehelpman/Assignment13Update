package com.coderscampus.assignment13.service;


import java.util.List;
import java.util.Optional;
import java.util.Set;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coderscampus.assignment13.ResourceNotFoundException;
import com.coderscampus.assignment13.domain.Account;
import com.coderscampus.assignment13.domain.Address;
import com.coderscampus.assignment13.domain.User;
import com.coderscampus.assignment13.repository.AccountRepository;
import com.coderscampus.assignment13.repository.AddressRepository;
import com.coderscampus.assignment13.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final AccountRepository accountRepo;
    private final AddressRepository addressRepo;

    @Autowired
    public UserService(UserRepository userRepo, AccountRepository accountRepo, AddressRepository addressRepo) {
        this.userRepo = userRepo;
        this.accountRepo = accountRepo;
        this.addressRepo = addressRepo;
    }

    // Fetch user by ID
    public User findById(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow();
    }

    // Fetch all users
    public Set<User> findAll() {
        return userRepo.findAllUsersWithAccountsAndAddress(); // Assuming this returns a Set<User>
    }

    // Fetch user by username
    public List<User> findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    @Transactional
    public void saveUser(User user) {
        userRepo.save(user);
    }

    
    // Save or update a user
//    @Transactional
//    public User saveUser(User user) {
//        if (user.getUserId() != null) {
//            User existingUser = findById(user.getUserId()); // Use findById to ensure user exists
//            existingUser.setUsername(user.getUsername());
//            existingUser.setName(user.getName());
//
//            if (user.getAddress() != null) {
//                Address address = user.getAddress();
//                address.setUser(existingUser); // Ensure the address links back to the user
//                addressRepo.save(address);
//                existingUser.setAddress(address);
//            }
//            return userRepo.save(existingUser);
//        } else {
//            // For new users
//            if (user.getAddress() != null) {
//                Address address = user.getAddress();
//                address.setUser(user); // Link the address to the new user
//                addressRepo.save(address);
//                user.setAddress(address);
//            }
//            return userRepo.save(user);
//        }
//    }

    // Delete a user
    @Transactional
    public void deleteUser(Long userId) {
        User user = findById(userId); // Ensure user exists
        userRepo.delete(user);
    }

    // Update an address
    @Transactional
    public void updateAddress(Address updatedAddress) {
        if (updatedAddress.getUser() == null || updatedAddress.getUser().getUserId() == null) {
            throw new IllegalArgumentException("Address must be linked to a valid user.");
        }

        Long userId = updatedAddress.getUser().getUserId();
        User user = findById(userId);

        Address existingAddress = user.getAddress();
        if (existingAddress == null) {
            updatedAddress.setUser(user);
            user.setAddress(updatedAddress);
            addressRepo.save(updatedAddress);
        } else {
            existingAddress.setAddressLine1(updatedAddress.getAddressLine1());
            existingAddress.setAddressLine2(updatedAddress.getAddressLine2());
            existingAddress.setCity(updatedAddress.getCity());
            existingAddress.setRegion(updatedAddress.getRegion());
            existingAddress.setZipCode(updatedAddress.getZipCode());
            existingAddress.setCountry(updatedAddress.getCountry());
            addressRepo.save(existingAddress);
        }
    }

    // Create a new account for a user
    @Transactional
    public Account createAccountForUser(Long userId, String accountName) {
        User user = findById(userId); // Fetch the user
        Account account = new Account();
        account.setAccountName(accountName);
        account.setUser(user); // Correctly associate the account with the user

        user.getAccounts().add(account); // Update the user's account list
        return accountRepo.save(account); // Save and return the account
    }

    @Transactional
    public void saveAccountForUser(User user, Account account) {
        // Ensure bidirectional consistency
        account.setUser(user); // Set the user in the account
        user.getAccounts().add(account); // Add the account to the user's account list

        accountRepo.save(account); // Save the account
        userRepo.save(user);       // Save the user
    }

    @Transactional
    public void updateUserAccounts(Long userId) {
        User user = userRepo.findById(userId).orElseThrow();
        List<Account> accounts = user.getAccounts(); // Safe within the transaction
        accounts.add(new Account());
    }

    // Save or update an account
    @Transactional
    public void saveOrUpdateAccount(Long userId, Account account) {
        User user = findById(userId); // Ensure user exists
        Optional<Account> existingAccountOpt = user.getAccounts().stream()
                .filter(a -> a.getAccountId().equals(account.getAccountId()))
                .findFirst();
        
        if (existingAccountOpt.isPresent()) {
            Account existingAccount = existingAccountOpt.get();
            existingAccount.setAccountName(account.getAccountName());
        } else {
            account.getUser();
            user.getAccounts().add(account);
        }
        
        accountRepo.save(account);
    }


    // Find account by ID
    public Account findByAccountId(Long accountId) {
        return accountRepo.findById(accountId).orElseThrow(() -> 
            new ResourceNotFoundException("Account not found for ID: " + accountId));
    }

    // Find all users with accounts and address (if custom query exists)
    public Set<User> findAllUsersWithAccountsAndAddress() {
        return userRepo.findAllUsersWithAccountsAndAddress();
    }
}