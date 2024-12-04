package com.coderscampus.assignment13.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coderscampus.assignment13.domain.User;
import com.coderscampus.assignment13.domain.Account;
import com.coderscampus.assignment13.domain.Address;
import com.coderscampus.assignment13.service.UserService;

@Controller
public class UserController {
	
	@Autowired
	private UserService userService;
	
	  public UserController(UserService userService) {
	        this.userService = userService;
	    }

	@GetMapping("/details")
    public ResponseEntity<Set<User>> getUsersWithDetails() {
        Set<User> users = userService.findAll();
        return ResponseEntity.ok(users);
    }
	
	@GetMapping("/register")
	public String getCreateUser (ModelMap model) {
		
		model.put("user", new User());
		
		return "register";
	}
	@GetMapping("/users")
	public String getAllUsers(ModelMap model) {
	    Set<User> users = userService.findAll();
	    model.put("users", users);
	    if (users.size() == 1) {
	        model.put("user", users.iterator().next());
	    }
	    return "users";
	}

	@GetMapping("/users/{userId}/details")
	public String getUserDetails(@PathVariable Long userId, Model model) {
	    // Fetch the user by ID
	    User user = userService.findById(userId);
	    
	    // Get the list of accounts associated with this user
	    List<Account> accounts = user.getAccounts();
	    
	    // Add attributes to the model
	    model.addAttribute("user", user);
	    model.addAttribute("accounts", accounts); // Pass accounts to the view
	    
	    return "userDetails";
	}

	@PostMapping("/users/{userId}/accounts/{accountId}/save")
	public String saveAccount(@PathVariable Long userId, 
	                          @PathVariable Long accountId, 
	                          @ModelAttribute Account account) {
	    // Fetch the user by ID
	    User user = userService.findById(userId); 

	    // Set the user for the account
	    account.setUser(user);  // Set the user for this account


	    // Save the account using the service
	    userService.saveAccountForUser(user, account);

	    // Log details for debugging
	    System.out.println("User ID: " + userId);
	    System.out.println("Account ID: " + accountId);
	    System.out.println("Account Name: " + account.getAccountName());

	    // Redirect to user details page
	    return "redirect:/users/" + userId;
	}

	@GetMapping("/users/{userId}/accounts")
	public String createAccountForm(@PathVariable("userId") Long userId, Model model, @ModelAttribute Account account) {
	    System.out.println("Create New Account Button Clicked");
	    account.setAccountId(0L);
	    model.addAttribute("userId", userId);
	    model.addAttribute("account", new Account()); // Add a new Account object to the model
	    System.out.println("account page reached/not reached");
	    return "account";
	}
	
	@GetMapping("/users/{userId}/accounts/{accountId}")
	public String showAccountDetails(@PathVariable Long userId, @PathVariable Long accountId, Model model) {
		System.out.println("Reached showAccountDetails method");
		// Fetch user and account
	    User user = userService.findById(userId);
	    Account account = userService.findByAccountId(accountId);

	    // Debug logging
	    System.out.println("User ID: " + userId);
	    System.out.println("Account ID: " + accountId);
	    System.out.println("Account fetched: " + (account != null ? account.getAccountId() : "null"));

	    // Add attributes to model
	    model.addAttribute("user", user);
	    model.addAttribute("userId", userId);
	    model.addAttribute("account", account);

	    return "account"; // Render the "account.html" view
	}
	
	@PostMapping("/register")
	public String postCreateUser (User user) {
		System.out.println(user);
		userService.saveUser(user);
		return "redirect:/register";
	}
	
	@RestController
	public class FaviconController {
	    @GetMapping("favicon.ico")
	    public void favicon() {
	        // Do nothing (or return an empty response)
	    }
	}
	
	@PostMapping("/users/{userId}/update")
	public String updateUser(@PathVariable Long userId, @ModelAttribute User user) {
	    // Find the existing user by ID
	    User existingUser = userService.findById(userId);

	    // Update basic user details
	    existingUser.setUsername(user.getUsername());
	    existingUser.setName(user.getName());
	    existingUser.setPassword(user.getPassword());

	    // Update address details, if provided
	    Address address = user.getAddress();
	    if (address != null) {
	        address.setUser(existingUser); // Associate address with the user
	        userService.updateAddress(address); // Persist address changes
	    }

	    // Save updated user
	    userService.saveUser(existingUser);
	    System.out.println("Updating user " + userId);

	    return "redirect:/users";
	}




	@GetMapping("/users/{userId}")
	public String getOneUser(ModelMap model, @PathVariable Long userId) {
	    User user = userService.findById(userId);

	    // Check if the user has an address; create one if it doesn't exist
	    if (user.getAddress() == null) {
	        Address newAddress = new Address();
	        newAddress.setUser(user); // Associate the address with the user
	        user.setAddress(newAddress); // Set the new address for the user
	    }

	    // Check if the user has accounts; initialize if empty
	    if (user.getAccounts() == null || user.getAccounts().isEmpty()) {
	        user.setAccounts(new ArrayList<>());
	    }

	    // Add attributes to the model
	    model.addAttribute("user", user);
	    model.addAttribute("accounts", user.getAccounts());
	    model.addAttribute("address", user.getAddress()); // Use singular "address" since it's not a collection

	    System.out.println("Confirm");
	    return "userDetails";
	}

	@PostMapping("/users/{userId}")
	public String postOneUser (@PathVariable Long userId, @ModelAttribute User user) {
		user.setUserId(userId); 
		userService.saveUser(user);
		return "redirect:/users/";
	}
	
	@PostMapping("/users/{userId}/delete")
	public String deleteOneUser (@PathVariable Long userId) {
		userService.deleteUser(userId);
		return "redirect:/users";
	}
	
	@PostMapping("/users/{userId}/accounts")
	public String createOrUpdateAccount(@PathVariable Long userId, @ModelAttribute Account account) {
	    if (account.getAccountId() == null) {
	        // Handle new account creation
	        userService.createAccountForUser(userId, account.getAccountName());
	    } else {
	        // Handle account update
	        userService.saveOrUpdateAccount(userId, account);
	    }
	    return "redirect:/users/" + userId;
	}


	
	@PostMapping("/users/save")
	public String saveUser(@ModelAttribute User user) {
	    userService.saveUser(user);
	    System.out.println("User saved successfully for user ID: " + user.getUserId());
	    return "redirect:/users";
	}

	@GetMapping("/users/{userId}/accounts/{accountId}/info")
	public String getOneAccount(ModelMap model, @PathVariable Long userId, @PathVariable Long accountId) {
	    Account account = userService.findByAccountId(accountId);
	    if (account == null) {
	        return "redirect:/users/" + userId + "/accounts/new"; 
	    }
	    model.addAttribute("account", account);
	    model.addAttribute("userId", userId);
	    return "account";
	}

	@PostMapping("/users/{userId}/accounts/{accountId}")
	public String postOneAccount(@PathVariable Long userId, @PathVariable(required = false) Long accountId, @ModelAttribute Account account) {
	    if (accountId == null || account.getAccountId() == null) {
	       
	        Account newAccount = userService.createAccountForUser(userId, account.getAccountName());
	        newAccount.setAccountName(account.getAccountName());
	        userService.saveOrUpdateAccount(userId, newAccount);
	        System.out.println("New account created successfully for user ID: " + userId);
	        return "redirect:/users/" + userId + "/accounts/" + newAccount.getAccountId();
	    } else {
	        
	        Account existingAccount = userService.findByAccountId(accountId);
	        if (existingAccount != null) {
	            existingAccount.setAccountName(account.getAccountName());
	            userService.saveOrUpdateAccount(userId,existingAccount);
	            System.out.println("Account updated successfully for account ID: " + accountId);
	        }
	        return "redirect:/users/" + userId + "/accounts/" + accountId;
	    }
	}
}