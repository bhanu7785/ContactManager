package com.eEcontact.Dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.RequestParam;

import com.eEcontact.Entity.Contact;
import com.eEcontact.Entity.User;

@EnableJpaRepositories
public interface ContactDao  extends JpaRepository<Contact, Integer>{
	
	//pagination...
	//payble have two thing  current page -page 2.contact per page
	@Query("from Contact as c where c.user.id=:userid")
	public Page<Contact> findContactByUser(@RequestParam("userid") int userid,Pageable pageable);
	
	//search functionality
	
	public List<Contact> findByNameContainingAndUser(String name,User user);
	
	

}
