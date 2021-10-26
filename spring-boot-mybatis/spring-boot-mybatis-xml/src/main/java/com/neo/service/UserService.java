package com.neo.service;

import com.neo.mapper.UserMapper;
import com.neo.model.User;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.web.bind.annotation.PathVariable;


import java.io.IOException;
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
	// 默认情况下是被spring事务内部检测这个异常情形，然后抛出一个异常提示：org.springframework.transaction.UnexpectedRollbackException: Transaction rolled back because it has been marked as rollback-only
	// 【3】内层事务方法saveWithTransactionalAndException是required事务，此时saveWithTransactionalAndException抛出的异常是runtimeException即除0异常，
	// 因为spring的事务默认runtimeException和Error会回滚，因为此种情况下内层事务是加入到外层事务中的，回滚需要依托于外层事务回滚哈，因此内部事务方法抛出的异常
	// 会进入TransactionAspectSupport的txInfo.getTransactionManager().rollback(txInfo.getTransactionStatus());这句代码逻辑，这句代码逻辑不会做真正的回滚，但会标记rollback-only，
	// 后面runtimeException除0异常虽被外层事务方法catch住，因此外层事务执行commit前，会先检查rollback-only标记，此时发现标记rollback-only为true，此时spring外层事务（也包括了内层事务）会回滚，且spring事务内部直接抛出UnexpectedRollbackException，
	// 最后user数据自然不会被插入到数据库。

	// 大家来思考另一个问题，就是怎么双层required事务的情况下，怎么能做到内层事务方法抛出异常，被外层事务方法catch住了，然后依然提交整个事务（内外层事务都是同一个事务而已）呢？
	// 答案参考配置类的platformTransactionManagerCustomizer方法即可（谨慎使用哈，否则会造成事务不一致行为！）
	@Transactional
	public void saveUserWithCatchException(User user) {
		try {
			user.setUserName("saveWithTransactionalAndException");
			userService.saveWithTransactionalAndException(user);
			// ((UserService)AopContext.currentProxy()).saveWithTransactionalAndException(user); // 另一个种解决自调用事务失效的办法，需要结合@EnableAspectJAutoProxy(exposeProxy = true)使用
		} catch (Exception e) {

		}
	}


	// 【4】内层事务方法saveWithTransactionalAndException是required事务，此时saveWithTransactionalAndException抛出的异常是runtimeException即除0异常，
	// 因为spring的事务默认runtimeException和Error会回滚，因为此种情况下内层事务是加入到外层事务中的，回滚需要依托于外层事务回滚哈，因此内部事务方法抛出的异常
	// 会进入TransactionAspectSupport的txInfo.getTransactionManager().rollback(txInfo.getTransactionStatus());这句代码逻辑，这句代码逻辑不会做真正的回滚，但会标记rollback-only，
	// 后面runtimeException除0异常虽被外层事务方法catch住，因此外层事务执行commit前，会先检查rollback-only标记，此时发现标记rollback-only为true，此时spring事务内部直接抛出UnexpectedRollbackException，
	// 最后user数据自然不会被插入到数据库。
	// 默认情况下是被spring事务内部检测这个异常情形，然后抛出一个异常提示：org.springframework.transaction.UnexpectedRollbackException: Transaction rolled back because it has been marked as rollback-only
	// 此时怎样能让外层事务方法不抛出这个异常，只是回滚就行了呢？外层事务方法的catch代码块加TransactionInterceptor.currentTransactionStatus().setRollbackOnly();即可
	@Transactional
	public void saveUserWithCatchException5(User user) {
		try {
			user.setUserName("saveWithTransactionalAndException");
			userService.saveWithTransactionalAndException(user);
		} catch (Exception e) {
			// 设置这个AbstractTransactionStatus的rollbackOnly属性为true，那么内层事务方法抛出异常被外层事务方法catch住后，
			// 此时spring不会抛出UnexpectedRollbackException，只是将外层事务回滚。但内层事务依然会标记rollback-only标记哈。
			// 即外层事务执行commit时会有两个检查，第一个是检查rollbackOnly，若其为true，那么此时直接回滚外层事务（其实也包括了此种测试用例的内层事务），然后直接return了；
			// 第二个是检查rollback-only标记，此时除了回滚外层事务（其实也包括了此种测试用例的内层事务），还会抛出UnexpectedRollbackException异常。
			// 最后，只有上面的检查都不满足的情况下，才会真正的commit。参考AbstractPlatformTransactionManager.commit方法即可
			TransactionInterceptor.currentTransactionStatus().setRollbackOnly();// 刚好命中了第一个检查哈
		}
	}

	// 【5】内层事务方法saveWithTransactionalAndCheckException方法是required事务，此时saveWithTransactionalAndCheckException抛出的异常是check异常IOException，
	// 但@Transactional事务注解的rollbackFor没指定受检异常（因为spring的事务默认runtimeException和Error才会回滚），因此内部事务方法抛出的异常
	// 不会进入TransactionAspectSupport的txInfo.getTransactionManager().rollback(txInfo.getTransactionStatus());这句代码逻辑即不会标记rollback-only，
	// 后面check异常IOException被外层事务方法catch住，因此当外层事务正常提交时由于没有标记rollback-only，所以spring事务内部也就不会抛出UnexpectedRollbackException，
	// 最后user数据被正常插入，然后随外层saveUserWithCatchException方法事务提交，此时user数据依然被插入到数据库
	@Transactional
	public void saveUserWithCatchException4(User user) {
		try {
			user.setUserName("saveWithTransactionalAndCheckException");
			userService.saveWithTransactionalAndCheckException(user);
		} catch (Exception e) {

		}
	}


	// 【6】saveUserWithCatchException2方法是required事务，此时saveWithTransactionalNestedAndExceptionn抛出的异常导致事务回滚到savepoint，
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

	// 【7】saveUserWithCatchException3方法是required事务，此时saveWithTransactionalNewAndException抛出异常导致new事务（另一个连接事务）回滚，因此user数据不会插入到数据库，
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
	public void saveWithTransactionalAndCheckException(User user) throws IOException {
		userMapper.insert(user);
		throw new IOException();
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
	public void saveWithTransactionalAndException(User user) throws Exception{
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