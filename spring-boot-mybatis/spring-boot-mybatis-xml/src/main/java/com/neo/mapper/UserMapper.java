package com.neo.mapper;

import java.util.List;
import java.util.Map;
import com.neo.model.User;

public interface UserMapper {
	
	List<User> getAll();
	
	User getOne(Long id);

	void insert(User user);

	void update(User user);

	void delete(Long id);

	List<Map> executeAnySelectSql(String sql);

}