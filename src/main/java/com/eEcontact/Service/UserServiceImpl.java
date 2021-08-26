package com.eEcontact.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.eEcontact.Dao.UserDao;
import com.eEcontact.Entity.User;

public class UserServiceImpl implements UserDetailsService {


		@Autowired
		private UserDao userRepo;
		@Override
		public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
			// TODO Auto-generated method stub
			
			//fetching user from database
			
			User userByName = this.userRepo.getUserByName(username);
			if(userByName==null) {
				throw new UsernameNotFoundException("Could not found user");
			}
			
			CustomerUserDetails customuserDetail =new CustomerUserDetails(userByName);
			return customuserDetail;
		}

		
	
	
	
}
