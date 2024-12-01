package com.coderscampus.assignment13.repository;


import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.coderscampus.assignment13.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByUsername(String username);
    List<User> findByNameAndUsername(String name, String username);
 

    // Custom query example for accounts and address
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.accounts LEFT JOIN FETCH u.address")
    Set<User> findAllUsersWithAccountsAndAddress();
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.accounts WHERE u.userId = :userId")
    Optional<User> findByIdWithAccounts(@Param("userId") Long userId);


    
}