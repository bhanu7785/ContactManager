package com.eEcontact.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.eEcontact.Dao.UserDao;
import com.eEcontact.Entity.User;
import com.eEcontact.Helper.Message;


@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder; 
	
	@Autowired
	private UserDao userRepository;	
	
	@RequestMapping("/")
	public String home(Model m) {

		m.addAttribute("title", "Home-Smart Contact-Manager");
		return "home";
	}

	@RequestMapping("/about")
	public String about(Model m) {

		m.addAttribute("title", "About-Smart Contact-Manager");
		return "about";
	}

	@RequestMapping("/signup")
	public String signup(Model m) {

		m.addAttribute("title", "SignUp-Smart Contact-Manager");
		m.addAttribute("user", new User());
		return "signup";
	}

//	This handle for Registering user
	@PostMapping("/do_register")
	public String getData(@Valid @ModelAttribute User user, BindingResult results,
			@RequestParam(value = "aggrement", defaultValue = "false") boolean aggrement, Model model,
			HttpSession session) {

		try {

			if (results.hasErrors()) {
				model.addAttribute("user", user);
				return "signup";
			}
			if (!aggrement) {
				System.out.println("You have not aggred the terms and conditions");
				throw new Exception("You have not aggred the terms and conditions");
			}
			System.out.println("Aggrement" + aggrement);
			System.out.println("User" + user);

			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("profile-default.png");

			user.setPassword(this.passwordEncoder.encode(user.getPassword()));
			User result = this.userRepository.save(user);
			model.addAttribute("user", new User());

			session.setAttribute("message", new Message("Successfully Registered!! ", "alert-success"));

			return "signup";

		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something Went Wrong!! ", "alert-danger"));

			return "signup";
		}

	}
	
	
	//handler for custom login
	
	@GetMapping("/signin")
	public String customLogin(Model model) {
		
		
		model.addAttribute("title", "Login Page");
		return "login";
		
	}




}
