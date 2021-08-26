package com.eEcontact.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.servlet.http.HttpSession;
import javax.xml.crypto.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.eEcontact.Dao.ContactDao;
import com.eEcontact.Dao.UserDao;
import com.eEcontact.Entity.Contact;
import com.eEcontact.Entity.User;
import com.eEcontact.Helper.Message;
import com.eEcontact.Service.EmailService;


@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserDao userDao;
	@Autowired
	private ContactDao contactDao;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private EmailService emailService;
	
// method to adding common things to data base
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		// get the user using username
		
		String name = principal.getName();  
		User userByName = userDao.getUserByName(name);

		model.addAttribute("user", userByName);

		System.out.println("User" + userByName);

	}
    
	
	//dashboard home
	@RequestMapping("/index")
	public String getData(Model model,Principal principal) {

		return "normal/user_dashboard";
	}
  //adding contact
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {

		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/addcontact";
	}
	//creating handler for form contact(processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file ,Principal principal,HttpSession session) {
		try {
		String name = principal.getName();
		User userByName = this.userDao.getUserByName(name);
		
		contact.setUser(userByName);
		userByName.getContacts().add(contact);
		
		//processing and uploading file
		if(file.isEmpty()) {
			contact.setImage("contact.png");
			System.out.println("Please upload file");
		}else {
			//file the file to folder and update the name to contact
			contact.setImage(file.getOriginalFilename());
			
			File file2 = new ClassPathResource("static/image").getFile();
			
			Path path = Paths.get(file2.getAbsoluteFile()+File.separator+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			
		}
		
		this.userDao.save(userByName);
		System.out.println("Data"+contact);
		
		System.out.println("Added to database");
		session.setAttribute("message",new Message("Your contact is added", "success"));
		
		}
		catch(Exception e) {
			System.out.println(e);
			session.setAttribute("message",new Message("Something Went Wrong !! Try Again","danger"));
		}
		return "normal/addcontact";
	}
	
	
	//show contact handler
	//per page 5
	//current page
	
	@GetMapping("/show-contact/{page}")
	public String showContact(@PathVariable("page") Integer page,Model model,Principal principal) {	
		model.addAttribute("title", "Show User Contacts");
		//contact List we have to send
		
		String name = principal.getName();
		User userByName = this.userDao.getUserByName(name);
		
		Pageable pagable= PageRequest.of(page, 5);
		
		Page<Contact> contacts = this.contactDao.findContactByUser(userByName.getId(),pagable);
		
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentpage", page);
		
		model.addAttribute("totalPages", contacts.getTotalPages());
		
		
		return "normal/show-contact";
	}
	
	@RequestMapping("/{cid}/contact")
	public String showContactDetails(@PathVariable("cid") Integer cid,Model model,Principal principal) {
		System.out.println(cid);
		
		Optional<Contact> contactopt = this.contactDao.findById(cid);
		
		Contact contact = contactopt.get();
		String name = principal.getName();
		User user = this.userDao.getUserByName(name);
		
		if(user.getId()==contact.getUser().getId()) {
		model.addAttribute("contact", contact);
		}
		
		return "normal/contact_detail";
	}
	
	
	 //deleting contact
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") int cid,Principal principal,HttpSession session) {
		
		System.out.println(cid);
		Optional<Contact> contactopt = this.contactDao.findById(cid);
		Contact contact = contactopt.get();
		String name = principal.getName();
		User user = this.userDao.getUserByName(name);
		
		user.getContacts().remove(contact);
		this.userDao.save(user);
		
		
		
		if(user.getId()==contact.getUser().getId()) {
			contact.setUser(null);
		this.contactDao.delete(contact);
		}
		session.setAttribute("message", new Message("Contact Deleted Successfully......","success"));

		
		return "redirect:/user/show-contact/0";
	}
	
	// open updating contact form handler
	
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid,Model m) {
		
		m.addAttribute("title", "Update Contact");
		
		Contact contact = this.contactDao.findById(cid).get();
		
		m.addAttribute("contact",contact);
		return "normal/update_form";
	}
	
	//update contact handler
	
	@RequestMapping(value="/process-update",method=RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model model,HttpSession session,Principal principal) {
		
		
		
		try {
			
			Contact oldcontact = this.contactDao.findById(contact.getCid()).get();	
			
			if(!file.isEmpty()) {
				//file work to rewrite
				//rewrite				
				
				
				
				// delete old photo
				File deletefile = new ClassPathResource("/static/image").getFile();
				File oldfile = new File(deletefile, oldcontact.getImage());
				oldfile.delete();
				
				
				//update new photo
				
				File savefile = new ClassPathResource("/static/image").getFile();
				
				Path path = Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}else {
				contact.setImage(oldcontact.getImage());
			}
			String name = principal.getName();
			User user = this.userDao.getUserByName(name);
			contact.setUser(user);
			
			this.contactDao.save(contact);
			
			session.setAttribute("message",new Message("Your contact is updated", "success"));
		
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return "redirect:/user/"+contact.getCid()+"/contact";
	}
	
	//your profile handler
	
	@GetMapping("/profile")
	public String yourProfile(@ModelAttribute User user,Model model) {
		
		model.addAttribute("title", "Profile");
		return "normal/profile";
		
	}
	
	
	//open setting handler
	
	@GetMapping("/setting")
	public String openSetting() {
		return "normal/setting";
	}
	
	
	@PostMapping("/change-password")
	public String changePassword( @RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,@RequestParam("reenterPassword") String reenterPassword,Principal principal ,HttpSession session) {
		
		User currentUser = this.userDao.getUserByName(principal.getName());
		
		System.out.println("oldPassword"+oldPassword);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword,currentUser.getPassword())) {
			
			 //change Password
			currentUser.setPassword(bCryptPasswordEncoder.encode(newPassword));
			if(this.bCryptPasswordEncoder.matches(reenterPassword,currentUser.getPassword())) {
			this.userDao.save(currentUser);
			session.setAttribute("message", new Message("Password changes Successfully!!","success"));
			}else {
				session.setAttribute("message", new Message("Please ReEnter correct password!!","danger"));

			}
			
		}
		else {
			session.setAttribute("message", new Message("Please Enter correct password!!","danger"));

		}
		
		
		return "normal/setting";
	}
	
	
	//forgot password Implementation
	
	@RequestMapping("/forgot-password")
	public String forgotPassword() {
		return "normal/forgot-form";
	}
	
	//sending opt
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email,HttpSession session) {
		
		
		//generating 4 digit random otp
		
		Random random = new Random(1000);
		
		int otp=random.nextInt(99999);
		System.out.println("otp"+otp);
		System.out.println("email"+email);
		// sending email otp
         String message="Sending OTP for verification"+otp+"Please verify it!!";
		
		String subject="OTP";
		boolean flag = this.emailService.sendEmail(message,subject,email);		
		
		if(flag=true) {
			
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			
			return "normal/verify_otp";
			
		}
		else {
			session.setAttribute("message",new Message("Please Enter correct otp!!","danger"));				
			return "redirect:normal/forgot-form";
		}
	}
	
	// verify otp
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp,HttpSession session) {
		
		int myOtp=(Integer) session.getAttribute("myotp"); 
		
		String email=(String) session.getAttribute("email");
		
		if(myOtp==otp) {
			
			User user = this.userDao.getUserByName(email);
			if(user==null) {
				session.setAttribute("message",new Message("User Does not exists with this email","danger"));
			}else {
				
			}
			return "normal/password_change_form";
		}
		else {
			session.setAttribute("message", new Message("You have Entered Wrong Otp","danger"));
			 return "normal/setting";
		}
		
	}
	
	//change password
	
	@PostMapping("/update-password")
	public String chnagePassword(@RequestParam("newPassword") String NewPassword,@RequestParam("reEnterPassword") String reEnterPassword,HttpSession session) {
		
		String email = (String) session.getAttribute("email");
		User user = this.userDao.getUserByName(email);
		if(NewPassword.equals(reEnterPassword)) {
		user.setPassword(NewPassword);
		session.setAttribute("message", new Message("Password Changed Successfully !!","success"));

		return "redirect:/signin";
		}else {
			session.setAttribute("message", new Message("Password Dont Match !!","danger"));
			return "redirect:/user/forgot-password";
		}
		
		
		
	}

}
