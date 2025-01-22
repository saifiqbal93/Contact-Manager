package com.scm.controllers;

import com.scm.entities.User;
import com.scm.forms.UserUpdateForm;
import com.scm.helpers.Helper;
import com.scm.helpers.Message;
import com.scm.helpers.MessageType;
import com.scm.services.ContactService;
import com.scm.services.ImageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.scm.services.UserService;

import java.util.UUID;

@Controller
@RequestMapping("/user")
public class UserController {

    private Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ContactService contactService;

    // user dashboard page
    @RequestMapping(value = "/dashboard")
    public String userDashboard(Authentication authentication, Model model) {
        System.out.println("User dashboard");
        User user = userService.getUserByEmail(Helper.getEmailOfLoggedInUser(authentication));
        int totalContacts = contactService.getByUserId(user.getUserId()).size();
        model.addAttribute("totalContacts",totalContacts);
        int faveContacts = contactService.getFaveContactsCountForUser(user);
        model.addAttribute("faveContacts", faveContacts);
        return "user/dashboard";
    }

    // user profile page
    @RequestMapping(value = "/profile")
    public String userProfile(Model model, Authentication authentication) {

        return "user/profile";
    }

    @GetMapping("/profile/delete/{userId}")
    public String deleteProfile(@PathVariable("userId")String userId, Authentication authentication, HttpServletRequest request){
        String username = Helper.getEmailOfLoggedInUser(authentication);
        User user = userService.getUserByEmail(username);
        if(userId.equals(user.getUserId()))
            userService.deleteUser(userId);
        if(authentication!=null)
            new SecurityContextLogoutHandler().logout(request, null, authentication);
        return "redirect:/login?logout=true&deletedProfile=true";
    }

    @GetMapping("/profile/update")
    public String updateProfile(Authentication authentication, Model model){
        User user = userService.getUserByEmail(Helper.getEmailOfLoggedInUser(authentication));
        UserUpdateForm form = new UserUpdateForm();
        form.setUpdateName(user.getName());
        form.setUpdatePhoneNumber(user.getPhoneNumber());
        form.setUpdateAbout(user.getAbout());
        form.setUpdateProfilePic(user.getProfilePic());
        model.addAttribute("userForm", form);
        return "user/profileUpdate";
    }

    @PostMapping("/profile/update")
    private String doUpdateProfile(@Valid @ModelAttribute("userForm") UserUpdateForm formData, BindingResult result,Authentication authentication){
        System.out.println(formData.getUpdateProfileImage()+" "+formData.getUpdateProfilePic());
        User user = userService.getUserByEmail(Helper.getEmailOfLoggedInUser(authentication));
        if(result.hasErrors()) {
            formData.setUpdateProfilePic(user.getProfilePic());
            return "user/profileUpdate";
        }
        user.setName(formData.getUpdateName());
        user.setAbout(formData.getUpdateAbout());
        user.setPhoneNumber(formData.getUpdatePhoneNumber());
        if(formData.getUpdateProfileImage()!=null && !formData.getUpdateProfileImage().isEmpty()){
            if(user.getImagePublicId()!=null){
                imageService.deleteImage(user.getImagePublicId());
            }
            logger.info("file is not empty");
            String fileName = UUID.randomUUID().toString();
            String imageUrl = imageService.uploadImage(formData.getUpdateProfileImage(), fileName);
            user.setImagePublicId(fileName);
            user.setProfilePic(imageUrl);
        }

        userService.updateUser(user);
        return "redirect:/user/profile?profileUpdated=true";
    }

    @GetMapping("/feedback")
    private String feedback(){
        return "user/feedback";
    }

    @PostMapping("/feedback")
    private String sendFeedback(@RequestParam("subject")String subject, @RequestParam("message")String message, Authentication authentication, HttpSession session){
        User user = userService.getUserByEmail(Helper.getEmailOfLoggedInUser(authentication));
        userService.sendFeedbackEmail(subject,message,user);
        session.setAttribute("message", Message.builder().content("Thank you for reaching out to us.").type(MessageType.green).build());
        return "user/feedback";
    }

    @GetMapping("/changePassword")
    private String changePassword(){
        return "user/changePassword";
    }

    @PostMapping("/updatePassword")
    private String updatePassword(@RequestParam("password1") String oldPassword, @RequestParam("password2") String newPassword, HttpSession session, Authentication authentication){
        User user = userService.getUserByEmail(Helper.getEmailOfLoggedInUser(authentication));
        boolean status = userService.processUpdatePassword(user, oldPassword, newPassword);
        if(!status){
            session.setAttribute("message",Message.builder().content("Incorrect old password.").type(MessageType.red).build());
            return "/user/changePassword";
        }
        return "redirect:/user/profile?updatePassword=true";
    }

}
