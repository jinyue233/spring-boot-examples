package com.neo;

import com.neo.web.UserController;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/*@EnableAspectJAutoProxy(exposeProxy = true)// 这句注解会抛出异常 TODO 待分析*/
@SpringBootApplication
@MapperScan("com.neo.mapper")
public class MybatisXmlApplication {

	public static void main(String[] args) {
		ApplicationContext ac = SpringApplication.run(MybatisXmlApplication.class, args);
		ac.getBean(UserController.class);
	}
}
