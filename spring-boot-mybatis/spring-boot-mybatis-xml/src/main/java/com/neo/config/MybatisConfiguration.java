package com.neo.config;


import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class MybatisConfiguration {

    public MybatisConfiguration() {
        System.out.println("============MybatisConfiguration=================");
    }

    /**
     * 这种方式得有相应的mybatis配置，参考MybatisAutoConfiguration的源码
     */
    @Component
    static class CustomConfigurationCustomizer implements ConfigurationCustomizer{

        @Override
        public void customize(org.apache.ibatis.session.Configuration configuration) {
            //configuration.getTypeHandlerRegistry().register(new SexEnumTypeHandler());
        }
    }
}
