package com.coderscampus.assignment13.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.coderscampus.assignment13.domain.Account;


@Repository
public interface AccountRepository extends JpaRepository<Account, Long>{

	// select * from users where accountName = :accountName
		List<Account> findByAccountName(String accountName);
	
}