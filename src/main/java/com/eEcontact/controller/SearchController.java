package com.eEcontact.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.eEcontact.Dao.ContactDao;
import com.eEcontact.Dao.UserDao;
import com.eEcontact.Entity.Contact;
import com.eEcontact.Entity.User;

@RestController
public class SearchController {
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private ContactDao conatctDao;
	
	//search handler
	@GetMapping("/search/{query}")
	public ResponseEntity<?> search(@PathVariable("query") String query, Principal principal){
		
	    User user = this.userDao.getUserByName(principal.getName());
		
		List<Contact> contact = this.conatctDao.findByNameContainingAndUser(query, user);
		
		return ResponseEntity.ok(contact);
		
		
	}

}
