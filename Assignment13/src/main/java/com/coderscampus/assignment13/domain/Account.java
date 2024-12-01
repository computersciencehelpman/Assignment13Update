package com.coderscampus.assignment13.domain;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;

    @Column(length = 100)
    private String accountName;

    @Column(nullable = false) // Ensure it's not null
    private Integer accountsOrder = 0; // Default value
    
    @ManyToMany(mappedBy = "accounts", cascade = CascadeType.ALL)
    private List<User> users = new ArrayList<>();

    // Getter, setter, and helper methods for users

    public void addUser(User user) {
        if (this.users == null) {
            this.users = new ArrayList<>();
        }
        this.users.add(user);
        user.getAccounts().add(this); // Ensure bidirectional consistency
    }

    public void removeUser(User user) {
        if (this.users != null) {
            this.users.remove(user);
            user.getAccounts().remove(this); // Ensure bidirectional consistency
        }
    }

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public Integer getAccountsOrder() {
		return accountsOrder;
	}

	public void setAccountsOrder(Integer accountsOrder) {
		this.accountsOrder = accountsOrder;
	}

	@Override
	public String toString() {
		return "Account [accountId=" + accountId + ", accountName=" + accountName + ", users=" + users + "]";
	}
    
}
