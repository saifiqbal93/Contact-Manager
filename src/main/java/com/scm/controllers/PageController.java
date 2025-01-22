package com.scm.controllers;

import com.scm.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.scm.entities.User;
import com.scm.forms.UserForm;
import com.scm.helpers.Message;
import com.scm.helpers.MessageType;
import com.scm.services.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.util.concurrent.ThreadLocalRandom;

@Controller
public class PageController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Value("${spring.mail.properties.domain_name}")
    private String domainName;

    @GetMapping("/")
    public String index() {
        return "redirect:/home";
    }

    @RequestMapping("/home")
    public String home(Model model) {
        System.out.println("Home page handler");
        // sending data to view
        model.addAttribute("name", "Substring Technologies");
        model.addAttribute("youtubeChannel", "Learn Code With Durgesh");
        model.addAttribute("githubRepo", "https://github.com/learncodewithdurgesh/");
        return "home";
    }

    // this is showing login page
    @GetMapping("/login")
    public String login() {
        return new String("login");
    }

    // registration page
    @GetMapping("/register")
    public String register(Model model) {

        UserForm userForm = new UserForm();
        model.addAttribute("userForm", userForm);

        return "register";
    }

    // processing register
    @RequestMapping(value = "/do-register", method = RequestMethod.POST)
    public String processRegister(@Valid @ModelAttribute UserForm userForm, BindingResult rBindingResult,
            HttpSession session) {

        System.out.println("Processing registration");
        System.out.println(userForm);

        if (rBindingResult.hasErrors()) {
            return "register";
        }

        User user = new User();
        user.setName(userForm.getName());
        user.setEmail(userForm.getEmail());
        user.setPassword(userForm.getPassword());
        user.setAbout(userForm.getAbout());
        user.setPhoneNumber(userForm.getPhoneNumber());
        user.setEnabled(false);

        User savedUser = userService.saveUser(user);

        System.out.println("user saved :");

        Message message = Message.builder().content("Registration Successful").type(MessageType.green).build();

        session.setAttribute("message", message);

        return "redirect:/register";
    }

    @PostMapping("/userMessage")
    public String userMessage(@RequestParam("name") String name, @RequestParam("email") String email, @RequestParam("messageText") String messageText){
        String body="name: " + name + "\nemail: " + email + "\nMessage: " + messageText;
        emailService.sendEmail(domainName,"Message from SCM user", body);
        return "redirect:/home?feedbackSent=true";
    }

    @GetMapping("/forgotPassword")
    private String forgotPassword(){
        return "forgotPassword";
    }

    @PostMapping("/forgotPassword")
    private String sendOTP(@RequestParam("email")String email, HttpSession session){
        User user = userService.getUserByEmail(email);
        if(user==null){
            session.setAttribute("message", Message.builder().content("Invalid email").type(MessageType.red).build());
            return "forgotPassword";
        }
        int otp = ThreadLocalRandom.current().nextInt(100000, 1000000);;
        emailService.sendEmail(email,"SCM OTP", "OTP is: "+otp);
        session.setAttribute("message",Message.builder().content("OTP sent to your email.").type(MessageType.blue).build());
        session.setAttribute("emailOTP",otp);
        session.setAttribute("email",email);
        return "verifyOtp";
    }

    @PostMapping("/verifyOTP")
    private String verifyOTP(@RequestParam("OTP") String enteredOTP, HttpSession session){
        Integer emailOTP = (Integer) session.getAttribute("emailOTP");
        if(emailOTP!=null && !emailOTP.toString().equals(enteredOTP)){
            session.setAttribute("message",Message.builder().content("Wrong OTP").type(MessageType.red).build());
            return "verifyOtp";
        }
        return "changePassword";
    }

    @PostMapping("/changePassword")
    private String changePassword(@RequestParam("password") String password, HttpSession session){
        String email = (String) session.getAttribute("email");
        boolean status = userService.updatePassword(email,password);
        if(status) {
            session.setAttribute("message", Message.builder().content("Password changes successfully!").type(MessageType.green).build());
            return "redirect:/login";
        }
        else {
            session.setAttribute("message",Message.builder().content("Something went wrong. Please try again.").type(MessageType.red).build());
            return "forgotPassword";
        }
    }

}
