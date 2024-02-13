package com.smart.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;
import com.smart.entities.User;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

	//fetch the contact of user using contactRepository
	
	//Pagination method....
	
	@Query("from Contact as c where c.user.id=:userId")
	public Page<Contact> findContactsByUser(@Param("userId")int userId, Pageable pageable);  //pageable contains current page and number of contacts per page
	
	
	//search
	public List<Contact>findByNameContainingAndUser(String name,User user);
}
