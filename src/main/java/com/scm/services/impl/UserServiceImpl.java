package com.scm.services.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.scm.services.ContactService;
import com.scm.services.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.scm.entities.User;
import com.scm.helpers.AppConstants;
import com.scm.helpers.Helper;
import com.scm.helpers.ResourceNotFoundException;
import com.scm.repsitories.UserRepo;
import com.scm.services.EmailService;
import com.scm.services.UserService;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ContactService contactService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private  Helper helper;

    @Override
    public User saveUser(User user) {
        String userId = UUID.randomUUID().toString();
        user.setUserId(userId);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoleList(List.of(AppConstants.ROLE_USER));

        logger.info(user.getProvider().toString());

        String emailToken = UUID.randomUUID().toString();
        user.setEmailToken(emailToken);

        User savedUser = userRepo.save(user);
        String emailLink = helper.getLinkForEmailVerification(emailToken);
        emailService.sendEmail(savedUser.getEmail(), "Verify Account : Smart  Contact Manager", emailLink);
        return savedUser;
    }

    @Override
    public Optional<User> getUserById(String id) {
        return userRepo.findById(id);
    }

    @Override
    public Optional<User> updateUser(User user) {

        User user2 = userRepo.findById(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user2.setName(user.getName());
        user2.setEmail(user.getEmail());
        user2.setPassword(user.getPassword());
        user2.setAbout(user.getAbout());
        user2.setPhoneNumber(user.getPhoneNumber());
        user2.setProfilePic(user.getProfilePic());
        user2.setEnabled(user.isEnabled());
        user2.setEmailVerified(user.isEmailVerified());
        user2.setPhoneVerified(user.isPhoneVerified());
        user2.setProvider(user.getProvider());
        user2.setProviderUserId(user.getProviderUserId());
        // save the user in database
        User save = userRepo.save(user2);
        return Optional.ofNullable(save);

    }

    @Override
    public void deleteUser(String id) {
        User user2 = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if(user2.getImagePublicId()!=null)
            imageService.deleteImage(user2.getImagePublicId());
        contactService.deleteAllImagesOfContactsOfUser(user2);
        userRepo.delete(user2);
    }

    @Override
    public boolean isUserExist(String userId) {
        User user2 = userRepo.findById(userId).orElse(null);
        return user2 != null ? true : false;
    }

    @Override
    public boolean isUserExistByEmail(String email) {
        User user = userRepo.findByEmail(email).orElse(null);
        return user != null ? true : false;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email).orElse(null);

    }

    @Override
    public void sendFeedbackEmail(String subject, String message, User user) {
        emailService.sendEmail(user.getEmail(),subject,message);
    }

    @Override
    public boolean updatePassword(String email, String password) {
        User user = userRepo.findByEmail(email).orElse(null);
        if(user==null){
            return false;
        }
        user.setPassword(passwordEncoder.encode(password));
        userRepo.save(user);
        return true;
    }

    @Override
    public boolean processUpdatePassword(User user, String oldPassword, String newPassword) {
        if(!passwordEncoder.matches(oldPassword,user.getPassword()))
            return false;
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
        return true;
    }

}
