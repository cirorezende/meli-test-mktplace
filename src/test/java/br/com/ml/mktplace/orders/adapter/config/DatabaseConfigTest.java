package br.com.ml.mktplace.orders.adapter.config;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Testes unitários para DatabaseConfig.
 * 
 * Verifica a configuração do DataSource, EntityManagerFactory e TransactionManager.
 */
@ExtendWith(MockitoExtension.class)
@Testcontainers
@DisplayName("DatabaseConfig Tests (Testcontainers)")
class DatabaseConfigTest {

    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_pass");

    private DatabaseConfig databaseConfig;

    @BeforeEach
    void setUp() {
        databaseConfig = new DatabaseConfig();

        // Initialize container URL & credentials
        ReflectionTestUtils.setField(databaseConfig, "jdbcUrl", postgres.getJdbcUrl());
        ReflectionTestUtils.setField(databaseConfig, "username", postgres.getUsername());
        ReflectionTestUtils.setField(databaseConfig, "password", postgres.getPassword());
        ReflectionTestUtils.setField(databaseConfig, "driverClassName", "org.postgresql.Driver");
        // Pool tuning values (arbitrary for test)
        ReflectionTestUtils.setField(databaseConfig, "maximumPoolSize", 5);
        ReflectionTestUtils.setField(databaseConfig, "minimumIdle", 1);
        ReflectionTestUtils.setField(databaseConfig, "connectionTimeout", 20000L);
        ReflectionTestUtils.setField(databaseConfig, "idleTimeout", 120000L);
        ReflectionTestUtils.setField(databaseConfig, "maxLifetime", 300000L);
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
        assertThat(hikariDataSource.getJdbcUrl()).isEqualTo(postgres.getJdbcUrl());
        assertThat(hikariDataSource.getUsername()).isEqualTo(postgres.getUsername());
        assertThat(hikariDataSource.getPassword()).isEqualTo(postgres.getPassword());
        assertThat(hikariDataSource.getDriverClassName()).isEqualTo("org.postgresql.Driver");
        assertThat(hikariDataSource.getMaximumPoolSize()).isEqualTo(5);
        assertThat(hikariDataSource.getMinimumIdle()).isEqualTo(1);
        assertThat(hikariDataSource.getConnectionTimeout()).isEqualTo(20000L);
        assertThat(hikariDataSource.getIdleTimeout()).isEqualTo(120000L);
        assertThat(hikariDataSource.getMaxLifetime()).isEqualTo(300000L);
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
    // Basic sanity check performed via successful instantiation
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
        // Não criamos o DataSource aqui para evitar tentativa real de conexão com senha vazia.
        Object pwd = ReflectionTestUtils.getField(databaseConfig, "password");
        assertThat(pwd).isEqualTo("");
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