package com.neo.mapper;

import java.util.List;
import java.util.Map;

import com.neo.model.StringWrapper;
import com.neo.service.UserService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import com.neo.model.User;
import com.neo.enums.UserSexEnum;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserMapperTest {

	@Autowired
	private UserMapper userMapper;
	@Autowired
	private UserService userService;


	@Test
	public void testSaveAndUpdate() throws Exception {
		userService.saveUser(new User(" 987654  ", "  987654 ", UserSexEnum.MAN, new StringWrapper(" 987654 ")));
	}

	// TODO QUESTION:为何结合spring这里不用sqlSession.commit也会提交呢？而单独使用Mybatis需要sqlSession.commit呢
	@Test
	@Transactional
	@Rollback(value = false)
	public void testInsert() throws Exception {
		userMapper.insert(new User(" 11  ", "  a123456 ", UserSexEnum.MAN, new StringWrapper(" aaNickName ")));
		userMapper.insert(new User("  22  ", "b123456   ", UserSexEnum.WOMAN, new StringWrapper(" bbNickName ")));
		userMapper.insert(new User("  33  ", "c123456", UserSexEnum.WOMAN, new StringWrapper(" ccNickName ")));

		// Assert.assertEquals(3, userMapper.getAll().size());
	}

	@Test
	public void testQuery() throws Exception {
		List<User> users = userMapper.getAll();
		if(users==null || users.size()==0){
			System.out.println("is null");
		}else{
			System.out.println(users.toString());
		}
	}
	
	
	@Test
	public void testUpdate() throws Exception {
		User user = userMapper.getOne(6l);
		System.out.println(user.toString());
		user.setNickName(new StringWrapper("neo"));
		userMapper.update(user);
		Assert.assertTrue(("neo".equals(userMapper.getOne(6l).getNickName())));
	}

	@Test
	public void testExecuteAnySelectSql() throws Exception {
		String sql = "select * from users;";
		List<Map> userMap = userMapper.executeAnySelectSql(sql);
		System.out.println(userMap);
	}

}