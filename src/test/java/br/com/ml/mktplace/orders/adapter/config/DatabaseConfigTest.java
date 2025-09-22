package br.com.ml.mktplace.orders.adapter.config;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes unitários para DatabaseConfig.
 * 
 * Verifica a configuração do DataSource, EntityManagerFactory e TransactionManager.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DatabaseConfig Tests")
class DatabaseConfigTest {

    private DatabaseConfig databaseConfig;

    @BeforeEach
    void setUp() {
        databaseConfig = new DatabaseConfig();
        
        // Set test properties using reflection
        ReflectionTestUtils.setField(databaseConfig, "jdbcUrl", 
            "jdbc:postgresql://localhost:5432/test_db");
        ReflectionTestUtils.setField(databaseConfig, "username", "test_user");
        ReflectionTestUtils.setField(databaseConfig, "password", "test_pass");
        ReflectionTestUtils.setField(databaseConfig, "driverClassName", 
            "org.postgresql.Driver");
        ReflectionTestUtils.setField(databaseConfig, "maximumPoolSize", 10);
        ReflectionTestUtils.setField(databaseConfig, "minimumIdle", 2);
        ReflectionTestUtils.setField(databaseConfig, "connectionTimeout", 20000L);
        ReflectionTestUtils.setField(databaseConfig, "idleTimeout", 300000L);
        ReflectionTestUtils.setField(databaseConfig, "maxLifetime", 900000L);
    }

    @Test
    @DisplayName("Should create HikariDataSource with correct configuration")
    void shouldCreateHikariDataSourceWithCorrectConfiguration() {
        // When
        DataSource dataSource = databaseConfig.dataSource();
        
        // Then
        assertNotNull(dataSource);
        assertThat(dataSource).isInstanceOf(HikariDataSource.class);
        
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        assertThat(hikariDataSource.getJdbcUrl()).isEqualTo("jdbc:postgresql://localhost:5432/test_db");
        assertThat(hikariDataSource.getUsername()).isEqualTo("test_user");
        assertThat(hikariDataSource.getPassword()).isEqualTo("test_pass");
        assertThat(hikariDataSource.getDriverClassName()).isEqualTo("org.postgresql.Driver");
        assertThat(hikariDataSource.getMaximumPoolSize()).isEqualTo(10);
        assertThat(hikariDataSource.getMinimumIdle()).isEqualTo(2);
        assertThat(hikariDataSource.getConnectionTimeout()).isEqualTo(20000L);
        assertThat(hikariDataSource.getIdleTimeout()).isEqualTo(300000L);
        assertThat(hikariDataSource.getMaxLifetime()).isEqualTo(900000L);
    }

    @Test
    @DisplayName("Should configure HikariCP with correct pool name")
    void shouldConfigureHikariCPWithCorrectPoolName() {
        // When
        DataSource dataSource = databaseConfig.dataSource();
        
        // Then
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        assertThat(hikariDataSource.getPoolName()).isEqualTo("OrdersHikariPool");
    }

    @Test
    @DisplayName("Should configure HikariCP with PostgreSQL optimizations")
    void shouldConfigureHikariCPWithPostgreSQLOptimizations() {
        // When
        DataSource dataSource = databaseConfig.dataSource();
        
        // Then
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        
        // Verify PostgreSQL-specific properties are set
        Properties dataSourceProperties = hikariDataSource.getDataSourceProperties();
        assertThat(dataSourceProperties.getProperty("reWriteBatchedInserts")).isEqualTo("true");
        assertThat(dataSourceProperties.getProperty("cachePrepStmts")).isEqualTo("true");
        assertThat(dataSourceProperties.getProperty("prepStmtCacheSize")).isEqualTo("250");
        assertThat(dataSourceProperties.getProperty("prepStmtCacheSqlLimit")).isEqualTo("2048");
        assertThat(dataSourceProperties.getProperty("useServerPrepStmts")).isEqualTo("true");
    }

    @Test
    @DisplayName("Should create EntityManagerFactory with correct configuration")
    void shouldCreateEntityManagerFactoryWithCorrectConfiguration() {
        // Given
        DataSource dataSource = databaseConfig.dataSource();
        
        // When
        LocalContainerEntityManagerFactoryBean factory = 
            databaseConfig.entityManagerFactory(dataSource);
        
        // Then
        assertNotNull(factory);
        assertThat(factory.getDataSource()).isSameAs(dataSource);
        
        // Verify packages to scan using reflection since getter might not be available
        Object packagesToScan = ReflectionTestUtils.getField(factory, "packagesToScan");
        assertThat(packagesToScan).isNotNull();
    }

    @Test
    @DisplayName("Should create JpaTransactionManager")
    void shouldCreateJpaTransactionManager() {
        // Given
        DataSource dataSource = databaseConfig.dataSource();
        LocalContainerEntityManagerFactoryBean emf = 
            databaseConfig.entityManagerFactory(dataSource);
        
        // When
        PlatformTransactionManager transactionManager = 
            databaseConfig.transactionManager(emf);
        
        // Then
        assertNotNull(transactionManager);
        assertThat(transactionManager).isInstanceOf(JpaTransactionManager.class);
    }

    @Test
    @DisplayName("Should create development config properties")
    void shouldCreateDevelopmentConfigProperties() {
        // Given
        DatabaseConfig.DevelopmentConfig devConfig = new DatabaseConfig.DevelopmentConfig();
        
        // When
        Properties props = devConfig.developmentJpaProperties();
        
        // Then
        assertNotNull(props);
        assertThat(props.getProperty("hibernate.show_sql")).isEqualTo("true");
        assertThat(props.getProperty("hibernate.format_sql")).isEqualTo("true");
        assertThat(props.getProperty("hibernate.use_sql_comments")).isEqualTo("true");
        assertThat(props.getProperty("logging.level.org.hibernate.SQL")).isEqualTo("DEBUG");
        assertThat(props.getProperty("logging.level.org.hibernate.type.descriptor.sql.BasicBinder"))
            .isEqualTo("TRACE");
    }

    @Test
    @DisplayName("Should create production config properties")
    void shouldCreateProductionConfigProperties() {
        // Given
        DatabaseConfig.ProductionConfig prodConfig = new DatabaseConfig.ProductionConfig();
        
        // When
        Properties props = prodConfig.productionJpaProperties();
        
        // Then
        assertNotNull(props);
        assertThat(props.getProperty("hibernate.generate_statistics")).isEqualTo("false");
        assertThat(props.getProperty("hibernate.cache.use_second_level_cache")).isEqualTo("true");
        assertThat(props.getProperty("hibernate.cache.region.factory_class"))
            .isEqualTo("org.hibernate.cache.jcache.JCacheRegionFactory");
    }

    @Test
    @DisplayName("Should handle empty password gracefully")
    void shouldHandleEmptyPasswordGracefully() {
        // Given
        ReflectionTestUtils.setField(databaseConfig, "password", "");
        
        // When
        DataSource dataSource = databaseConfig.dataSource();
        
        // Then
        assertNotNull(dataSource);
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        assertThat(hikariDataSource.getPassword()).isEqualTo("");
    }

    @Test
    @DisplayName("Should configure health check query")
    void shouldConfigureHealthCheckQuery() {
        // When
        DataSource dataSource = databaseConfig.dataSource();
        
        // Then
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        assertThat(hikariDataSource.getConnectionTestQuery()).isEqualTo("SELECT 1");
    }
}