package br.com.ml.mktplace.orders.adapter.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Configuração do banco de dados - Database Config
 * 
 * Configura:
 * - DataSource com HikariCP (connection pool otimizado)
 * - JPA/Hibernate properties específicas para PostgreSQL + PostGIS
 * - Transaction management
 * - Configurações otimizadas por ambiente (dev, staging, prod)
 * 
 * ADRs relacionados:
 * - ADR-001: PostgreSQL com PostGIS
 * - ADR-016: Configuração por arquivos de propriedades
 */
@Configuration
@EnableJpaRepositories(basePackages = "br.com.ml.mktplace.orders.adapter.outbound.persistence")
@EnableTransactionManagement
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${app.database.pool.maximum-pool-size:20}")
    private int maximumPoolSize;

    @Value("${app.database.pool.minimum-idle:5}")
    private int minimumIdle;

    @Value("${app.database.pool.connection-timeout:30000}")
    private long connectionTimeout;

    @Value("${app.database.pool.idle-timeout:600000}")
    private long idleTimeout;

    @Value("${app.database.pool.max-lifetime:1800000}")
    private long maxLifetime;

    /**
     * Configura o DataSource principal com HikariCP.
     * HikariCP é o connection pool padrão do Spring Boot por ser o mais performático.
     */
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        
        // Pool settings otimizados
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        
        // Pool naming para monitoring
        config.setPoolName("OrdersHikariPool");
        
        // Health check query para PostgreSQL
        config.setConnectionTestQuery("SELECT 1");
        
        // Forçar autoCommit=false
        config.setAutoCommit(false);
        
        // Configurações específicas para PostgreSQL
        config.addDataSourceProperty("reWriteBatchedInserts", "true");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        
        return new HikariDataSource(config);
    }

    /**
     * Configura o EntityManagerFactory com propriedades específicas do PostgreSQL + PostGIS.
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("br.com.ml.mktplace.orders.adapter.outbound.persistence.entity");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        factory.setJpaVendorAdapter(vendorAdapter);
        
        Properties jpaProperties = new Properties();
        configureJpaProperties(jpaProperties);
        factory.setJpaProperties(jpaProperties);
        
        return factory;
    }

    /**
     * Configura o gerenciador de transações JPA.
     */
    @Bean
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }

    /**
     * Configurações JPA específicas por ambiente.
     */
    private void configureJpaProperties(Properties jpaProperties) {
        // Configurações básicas Hibernate
        jpaProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        jpaProperties.setProperty("hibernate.hbm2ddl.auto", "validate");
        jpaProperties.setProperty("hibernate.show_sql", "false");
        jpaProperties.setProperty("hibernate.format_sql", "false");
        
        // Performance settings
        jpaProperties.setProperty("hibernate.jdbc.batch_size", "25");
        jpaProperties.setProperty("hibernate.order_inserts", "true");
        jpaProperties.setProperty("hibernate.order_updates", "true");
        jpaProperties.setProperty("hibernate.jdbc.batch_versioned_data", "true");
        jpaProperties.setProperty("hibernate.connection.provider_disables_autocommit", "true");
        
        // Connection settings
        jpaProperties.setProperty("hibernate.connection.isolation", "2"); // READ_COMMITTED
        jpaProperties.setProperty("hibernate.temp.use_jdbc_metadata_defaults", "false");
        
        // PostGIS specific
        jpaProperties.setProperty("hibernate.spatial.connection_finder", "org.hibernate.spatial.dialect.postgis.PostGISConnectionFinder");
    }

    /**
     * Configurações específicas para ambiente de desenvolvimento.
     */
    @Configuration
    @Profile("dev")
    static class DevelopmentConfig {
        
        @Bean
        public Properties developmentJpaProperties() {
            Properties properties = new Properties();
            properties.setProperty("hibernate.show_sql", "true");
            properties.setProperty("hibernate.format_sql", "true");
            properties.setProperty("hibernate.use_sql_comments", "true");
            properties.setProperty("logging.level.org.hibernate.SQL", "DEBUG");
            properties.setProperty("logging.level.org.hibernate.type.descriptor.sql.BasicBinder", "TRACE");
            return properties;
        }
    }

    /**
     * Configurações específicas para ambiente de produção.
     */
    @Configuration
    @Profile("prod")
    static class ProductionConfig {
        
        @Bean
        public Properties productionJpaProperties() {
            Properties properties = new Properties();
            // Configurações de performance para produção
            properties.setProperty("hibernate.generate_statistics", "false");
            properties.setProperty("hibernate.cache.use_second_level_cache", "true");
            properties.setProperty("hibernate.cache.region.factory_class", "org.hibernate.cache.jcache.JCacheRegionFactory");
            return properties;
        }
    }
}