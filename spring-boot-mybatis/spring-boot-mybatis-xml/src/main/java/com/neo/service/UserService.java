package com.neo.service;

import com.neo.mapper.UserMapper;
import com.neo.model.StringWrapper;
import com.neo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;


import java.util.List;

@Service
public class UserService {

	@Autowired
	private UserService userService;

	@Autowired
	private UserMapper userMapper;

	public List<User> getUsers() {
		List<User> users=userMapper.getAll();
		return users;
	}

    public User getUser(Long id) {
    	User user=userMapper.getOne(id);
        return user;
    }

	@Transactional
    public void saveUser(User user) {
		// 【1】saveWithTransactionalNested方法抛出异常，该方法事务回滚；同时抛出异常，导致saveUser事务回滚，因此saveWithTransactional方法事务回滚（因为saveWithTransactional方法的事务会加入到saveUser的事务中,虽然saveWithTransactional事务切面方法会调用一次commit方法，但commit方法里面会判断当前事务是否是新事务，如果是新事务，那么会提交；否则就不会提交，会随外层事务一起提交，参考AbstractPlatformTransactionManager.processCommit方法哈）
		user.setUserName("saveWithTransactional");
		userService.saveWithTransactional(user);
		user.setUserName("saveWithTransactionalNested");
		userService.saveWithTransactionalNested(user);
	}

    @Transactional
    public void saveWithTransactional(User user) {
    	userMapper.insert(user);
    }

	@Transactional(propagation = Propagation.NESTED)
	public void saveWithTransactionalNested(User user) {
		userMapper.insert(user);
		int i = 1/0;
	}


	@Transactional
    public void update(User user) {
    	userMapper.update(user);
    }

	@Transactional
    public void delete(@PathVariable("id") Long id) {
    	userMapper.delete(id);
    }
    
    
}