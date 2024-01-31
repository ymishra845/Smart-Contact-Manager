package com.smart.controller;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;


import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ssl.SslProperties.Bundles.Watch.File;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.UserRepository;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;

	//method for adding common data i.e user
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		
		String userName=principal.getName();
		System.out.println("USERNAME =" +userName);
		
		// get the user using userName
		
	   User user= userRepository.getUserByUserName(userName);
	   
	   System.out.println("USER="+user);
	   
	   model.addAttribute("userByUserName",user);
		
	}
	
	//dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
		
		String userName=principal.getName();
		System.out.println("USERNAME =" +userName);
		
		// get the user using userName
		
	   User userByUserName= userRepository.getUserByUserName(userName);
	   
	   System.out.println("USER="+userByUserName);
	   
	   model.addAttribute("userByUserName",userByUserName);
		
		return "normal/user_dashboard";
	}
	//open add form handler
	
	@GetMapping("/add-contact")
	public String openAddContactForm( Model model) {
		 
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		
		return "normal/add_contact_form";
	}
	
	//Processing add contact form
	
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file,
			Principal principal,HttpSession session) {
		
		
		try {
			
			
		String name=principal.getName();
		User user=this.userRepository.getUserByUserName(name);
		
		//processing and uploading
		
		if(file.isEmpty()) {
			//file is empty
			System.out.println("File is empty");
		}else {
			
			//file is present add it to folder and update the name to the contact
			
			contact.setImage(file.getOriginalFilename());
			
			java.io.File saveFile= new ClassPathResource("static/img").getFile();
			
			java.nio.file.Path path=Paths.get(saveFile.getAbsolutePath()+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
			
			System.out.println("Image is added");
		}
		
		contact.setUser(user);
		user.getContacts().add(contact);
		
		
		
		this.userRepository.save(user);      //contact saved in data base
		
		System.out.println("DATA " +contact);
		
		System.out.println("Added to data base");
		
		//Message success
		
		session.setAttribute("message", new Message("Your Contact is added!! Happy to Add more..", "success"));
		
		
		}catch(Exception e){
			
			System.out.println("Error "+e.getMessage());
			e.printStackTrace();
			
			session.setAttribute("message", new Message("Something went wrong try again!!!!", "danger"));
		}
		return  "normal/add_contact_form";
	}
	
	
	
	
	
	
	
}
