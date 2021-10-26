package com.neo.config;


import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.boot.autoconfigure.transaction.PlatformTransactionManagerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
/*@EnableAspectJAutoProxy(exposeProxy = true)// 这句注解会抛出异常 TODO 待分析*/
@Configuration
public class Config {

    public Config() {
        System.out.println("============Config=================");
    }
    // 【谨慎使用】setGlobalRollbackOnParticipationFailure(false);的情况下，并且为双层required事务的情况：
    // 此时若内层事务方法抛出异常，外层事务方法catch住异常，此时内层事务不会回滚，外层事务会正常提交（实质内层事务也是外层事务而已）。
    // 此时内层事务的增删改数据行为会被提交，可能导致逻辑不一致。所以spring事务在这种情况下会回滚事务（内外层事务）并且抛出异常
    // @Bean // 这个@Bean方法先注释掉，恢复spring默认行为，需要调试再打开即可
    public PlatformTransactionManagerCustomizer platformTransactionManagerCustomizer() {
        return transactionManager -> {
            ((DataSourceTransactionManager)transactionManager).setGlobalRollbackOnParticipationFailure(false);
        };
    }

    /**
     * 这种方式得有相应的mybatis配置，参考MybatisAutoConfiguration的源码
     */
    @Component
    static class CustomConfigurationCustomizer implements ConfigurationCustomizer{
        public CustomConfigurationCustomizer() {
            System.out.println("============Config.CustomConfigurationCustomizer=================");
        }
        @Override
        public void customize(org.apache.ibatis.session.Configuration configuration) {
            System.out.println();
            //configuration.getTypeHandlerRegistry().register(new SexEnumTypeHandler());
        }
    }
}
