package com.coderscampus.assignment13.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.EntityManager;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coderscampus.assignment13.domain.Account;
import com.coderscampus.assignment13.domain.Address;
import com.coderscampus.assignment13.domain.User;
import com.coderscampus.assignment13.repository.AccountRepository;
import com.coderscampus.assignment13.repository.AddressRepository;
import com.coderscampus.assignment13.repository.UserRepository;


@Service
public class UserService {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private AccountRepository accountRepo;
	@Autowired
	private AddressRepository addressRepo;
	
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private List<Account> accounts = new ArrayList<>();

	public List<User> findByUsername(String username) {
		return userRepo.findByUsername(username);
	}
	
	public List<Account> findByAccountName(String accountName){
		return accountRepo.findByAccountName(accountName);
	}
	
	public List<User> findByNameAndUsername(String name, String username) {
		return userRepo.findByNameAndUsername(name, username);
	}
	
	public List<User> findByCreatedDateBetween(LocalDate date1, LocalDate date2) {
		return userRepo.findByCreatedDateBetween(date1, date2);
	}
	
	public User findExactlyOneUserByUsername(String username) {
		List<User> users = userRepo.findExactlyOneUserByUsername(username);
		if (users.size() > 0)
			return users.get(0);
		else
			return new User();
	}
	
	public Set<User> findAll () {
		return userRepo.findAllUsersWithAccountsAndAddresses();
	}
	public Set<User> findAllUsersWithDetails() {
        return userRepo.findAllUsersWithAccountsAndAddresses();
    }
	
	@Transactional
	public void updateAddress(Address updatedAddress) {
	    Long userId = updatedAddress.getUser() != null ? updatedAddress.getUser().getUserId() : null;

	    if (userId != null) {
	        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

	        
	        if (user.getAddresses() == null) {
	            user.setAddresses(new ArrayList<>());
	        }

	      
	        Optional<Address> existingAddressOpt = user.getAddresses().stream()
	                .filter(address -> updatedAddress.getId() != null
	                        && updatedAddress.getId().equals(address.getId()))
	                .findFirst();

	        if (existingAddressOpt.isPresent()) {
	            Address existingAddress = existingAddressOpt.get();
	            existingAddress.setAddressLine1(updatedAddress.getAddressLine1());
	            existingAddress.setAddressLine2(updatedAddress.getAddressLine2());
	            existingAddress.setCity(updatedAddress.getCity());
	            existingAddress.setRegion(updatedAddress.getRegion());
	            existingAddress.setZipCode(updatedAddress.getZipCode());
	            existingAddress.setCountry(updatedAddress.getCountry());
	            addressRepo.save(existingAddress);
	        } else {
	            updatedAddress.setUser(user);
	            user.getAddresses().add(updatedAddress);
	            addressRepo.save(updatedAddress);
	        }
	    }
	}

	public User findById(Long userId) {
        
        Optional<User> userOpt = userRepo.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            
            if (user.getAddresses() == null || user.getAddresses().isEmpty()) {
                Address newAddress = new Address();
                newAddress.setUser(user); // Associate new address with the existing user
                user.getAddresses().add(newAddress);
                addressRepo.save(newAddress); // Save the new address
            }
            return user;
        } else {
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }


	public Account findByAccountId(Long accountId) {
	    return accountRepo.findById(accountId).orElse(new Account());
	}

	public Account createAccountForUser(Long userId, String accountName) {
	    User user = userRepo.findById(userId)
	            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

	    Account newAccount = new Account();
	    newAccount.setAccountName(accountName); 

	    List<User> users = new ArrayList<>();
	    users.add(user);
	    newAccount.setUsers(users);

	    return accountRepo.save(newAccount);
	}

	public User saveUser(User user) {
	    if (user.getUserId() != null) {
	        User existingUser = userRepo.findById(user.getUserId())
	                .orElseThrow(() -> new RuntimeException("User not found"));

	        existingUser.setUsername(user.getUsername());
	        existingUser.setName(user.getName());

	        if (user.getAddresses() != null && !user.getAddresses().isEmpty()) {
	            for (Address address : user.getAddresses()) {
	                address.setUser(existingUser);
	                addressRepo.save(address);
	            }
	        }

	        return userRepo.save(existingUser);
	    } else {
	        if (user.getAddresses() != null && !user.getAddresses().isEmpty()) {
	            for (Address address : user.getAddresses()) {
	                address.setUser(user);
	                addressRepo.save(address);
	            }
	        }

	        Account checking = new Account();
	        checking.setAccountName("Checking Account");
	        checking.getUsers().add(user);

	        Account savings = new Account();
	        savings.setAccountName("Savings Account");
	        savings.getUsers().add(user);

	        user.getAccounts().add(checking);
	        user.getAccounts().add(savings);

	        accountRepo.save(checking);
	        accountRepo.save(savings);

	        return userRepo.save(user);
	    }
	}


	public void delete(Long userId) {
		userRepo.deleteById(userId);
	}
	
	public void saveOrUpdateAccount(Long userId, Account account) {
	    User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

	    List<User> users = new ArrayList<>(account.getUsers());
	    users.add(user);
	    account.setUsers(users);

	    accountRepo.save(account);
	}

	public void saveAccount(Long userId, Account account) {
	    User user = userRepo.findById(userId).orElseThrow(() -> 
	        new RuntimeException("User not found with ID: " + userId)
	    );
	    
	    Optional<Account> existingAccountOpt = user.getAccounts().stream()
	            .filter(a -> a.getAccountId() != null && a.getAccountId().equals(account.getAccountId()))
	            .findFirst();
	    
	    if (existingAccountOpt.isPresent()) {
	        Account existingAccount = existingAccountOpt.get();
	        existingAccount.setAccountName(account.getAccountName());
	    } else {
	       
	        account.getUsers().add(user);
	        user.getAccounts().add(account); 
	    }

	    accountRepo.save(account);
	    userRepo.save(user);
	}

	public Account saveAccount(Account account) {
		return accountRepo.save(account);
	}
	
	public Account updateAccount(Long userId, Long accountId, Account account) {
		return accountRepo.save(account);
	}
}