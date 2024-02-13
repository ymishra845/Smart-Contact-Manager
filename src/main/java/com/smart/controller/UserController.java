package com.smart.controller;

import java.nio.file.Files;

import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.ArrayList;

import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Path;

import org.apache.el.stream.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ssl.SslProperties.Bundles.Watch.File;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;
	
	
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
			contact.setImage("contact.png");
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
	
	
	//show contact handler
	// per page=5[n]
	//current page=0[page]
	
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page , Model m, Principal principal) {
		m.addAttribute("title","Show User Contacts");
		
		//sending the list of contacts to show contacts from database
		
		String userName=principal.getName();
		User user=this.userRepository.getUserByUserName(userName);
		
		Pageable pageable=PageRequest.of(page, 5);
		Page<Contact> contacts= (Page<Contact>) this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage", page);
		
		m.addAttribute("totalPages", contacts.getTotalPages());
		return "normal/show_contacts";
	}
	
	//showing particular contact detail
	
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId , Model model,Principal principal) {
		System.out.println("CID"+cId);
		
		java.util.Optional<Contact> contactoptional=this.contactRepository.findById(cId);
		Contact contact=contactoptional.get();
		
		String userName=principal.getName();
		User user=this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId()) {
			model.addAttribute("contact", contact);
		}
		
		model.addAttribute("contact", contact);
		return "normal/contact_detail";
	}
	
	// delete contact handler
	
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId,Model model,HttpSession httpSession,Principal principal) {
		
		java.util.Optional<Contact> contactOptional= this.contactRepository.findById(cId);
		Contact contact=contactOptional.get();
		
		
		User user= this.userRepository.getUserByUserName(principal.getName());
		
		user.getContacts().remove(contact);
		
		this.userRepository.save(user);
		
		httpSession.setAttribute("message", new Message("Contact deleted successfully", "success"));
		return "redirect:/user/show-contacts/0";
	}
	
	//Open update form handler
	
	
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId, Model m) {
		
		m.addAttribute("title", "Update Contact");
		
		Contact contact= this.contactRepository.findById(cId).get();
		m.addAttribute("contact", contact);
		return "/normal/update_form";
	}
	
	
	//update contact handler
	
	@RequestMapping(value = "/process-update",method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage")
	MultipartFile file,Model m,HttpSession session, Principal principal) {
		
		try {
			
			// old contact detail
			
			Contact oldcontact=this.contactRepository.findById(contact.getcId()).get();
			
			// if image selected than rewrite
			
			if(!file.isEmpty()) {
				
				//rewrite the file
				
				//delete old photo
				
				java.io.File deleteFile= new ClassPathResource("static/img").getFile();
				
				
				java.io.File file1= new java.io.File(deleteFile,oldcontact.getImage());
				
				file1.delete();
				
				//update new photo
				
				contact.setImage(file.getOriginalFilename());
				
				java.io.File saveFile= new ClassPathResource("static/img").getFile();
				
				java.nio.file.Path path=Paths.get(saveFile.getAbsolutePath()+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				
			}else {
				contact.setImage(oldcontact.getImage());    //if file is empty then old and new same
			}
			
			User user=this.userRepository.getUserByUserName(principal.getName());
			
			contact.setUser(user);
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your contact is updated...","success"));
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		System.out.println(contact.getName());
		
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	//Your Profile handler
	
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		
		model.addAttribute("title", "Profile Page");
		return "normal/profile";
		
	}
	
	
}
