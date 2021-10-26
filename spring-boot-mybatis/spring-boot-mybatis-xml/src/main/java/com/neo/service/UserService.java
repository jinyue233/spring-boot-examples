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
		// 【1】saveWithTransactionalNested方法抛出异常，该方法事务（其实也是外层saveUser方法事务而已，只不过saveWithTransactionalNested方法会创建回滚点）回滚；
		// 同时抛出异常，导致saveUser事务回滚，因此saveWithTransactional方法事务回滚（因为saveWithTransactional方法的事务会加入到saveUser的事务中,
		// 虽然saveWithTransactional事务切面方法会调用一次commit方法，但commit方法里面会判断当前事务是否是新事务，
		// 如果是新事务，那么会提交；否则就不会提交，会随外层事务一起提交，参考AbstractPlatformTransactionManager.processCommit方法哈）
		/*user.setUserName("saveWithTransactional");
		userService.saveWithTransactional(user);
		user.setUserName("saveWithTransactionalNested");
		userService.saveWithTransactionalNestedAndException(user);*/

		// 【2】saveWithTransactionalNested方法抛出异常，该方法事务回滚；同时抛出异常，导致saveUser事务回滚，
		// 因为saveWithTransactionalNew方法事务跟saveUser方法事务是不同的事务，即saveWithTransactionalNew方法事务
		// 是新开事务（新开了一个数据库连接），且该事务已提交，因此saveUser事务回滚并不影响saveWithTransactionalNew方法事务
		// saveWithTransactionalNew事务切面方法会调用一次commit方法，但commit方法里面会判断当前事务是否是新事务，
		// 如果是新事务，那么会提交（正是此种情况）；否则就不会提交，会随外层事务一起提交，参考AbstractPlatformTransactionManager.processCommit方法哈）
		user.setUserName("saveWithTransactionalNew");
		userService.saveWithTransactionalNew(user);
		user.setUserName("saveWithTransactionalNested");
		userService.saveWithTransactionalNestedAndException(user);
	}
	// TODO 待分析：默认情况下是被spring事务内部检测这个异常情形，然后抛出一个异常提示：org.springframework.transaction.UnexpectedRollbackException: Transaction rolled back because it has been marked as rollback-only
	// 【3】saveWithTransactionalAndException方法是required事务，此时saveWithTransactionalAndException抛出的异常被catch住，
	// 然后随外层saveUserWithCatchException方法事务提交，此时user数据依然被插入到数据库
	@Transactional
	public void saveUserWithCatchException(User user) {
		try {
			user.setUserName("saveWithTransactionalAndException");
			userService.saveWithTransactionalAndException(user);
		} catch (Exception e) {

		}
	}

	// 【4】saveUserWithCatchException2方法是required事务，此时saveWithTransactionalNestedAndExceptionn抛出的异常导致事务回滚到savepoint，
	// 同时saveWithTransactionalNestedAndException抛出的异常被catch住，
	// 然后随外层saveUserWithCatchException2方法事务提交，此时user数据因为已经被回滚到savePoint了，所以不会插入到数据库
	@Transactional
	public void saveUserWithCatchException2(User user) {
		try {
			user.setUserName("saveWithTransactionalNestedAndException");
			userService.saveWithTransactionalNestedAndException(user);
		} catch (Exception e) {

		}
	}

	// 【5】saveUserWithCatchException3方法是required事务，此时saveWithTransactionalNewAndException抛出异常导致new事务（另一个连接事务）回滚，因此user数据不会插入到数据库，
	// 同时saveWithTransactionalNewAndException抛出的异常被catch住，因此不会影响外层事务（注意：内外层事务没有一点关系，除非内层事务方法抛出异常导致外层事务回滚）
	// 然后外层saveUserWithCatchException3方法事务提交。
	@Transactional
	public void saveUserWithCatchException3(User user) {
		try {
			user.setUserName("saveWithTransactionalNewAndException");
			userService.saveWithTransactionalNewAndException(user);
		} catch (Exception e) {

		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveWithTransactionalNew(User user) {
		userMapper.insert(user);
	}

    @Transactional
    public void saveWithTransactional(User user) {
    	userMapper.insert(user);
    }

	@Transactional
	public void saveWithTransactionalAndException(User user) {
		userMapper.insert(user);
		int i = 1/0;
	}

	@Transactional(propagation = Propagation.NESTED)
	public void saveWithTransactionalNestedAndException(User user) {
		userMapper.insert(user);
		int i = 1/0;
	}

	@Transactional(propagation = Propagation.NESTED)
	public void saveWithTransactionalNested(User user) {
		userMapper.insert(user);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveWithTransactionalNewAndException(User user) {
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