package com.neo.web;

import java.util.List;

import com.neo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.neo.model.User;

@RestController
public class UserController {
	
	@Autowired
	private UserService userService;

	public UserController() {
        System.out.println("================UserController====================");
    }
	
	@RequestMapping("/getUsers")
	public List<User> getUsers() {
		List<User> users=userService.getUsers();
		return users;
	}
	
    @RequestMapping("/getUser")
    public User getUser(Long id) {
    	User user=userService.getUser(id);
        return user;
    }
    
    @RequestMapping("/add")
    public void save(User user) {
    	userService.saveUser(user);
    }
    
    @RequestMapping(value="update")
    public void update(User user) {
    	userService.update(user);
    }
    
    @RequestMapping(value="/delete/{id}")
    public void delete(@PathVariable("id") Long id) {
    	userService.delete(id);
    }

}