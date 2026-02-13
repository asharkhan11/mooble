//package in.ashar.mooble.configuration;
//
//import org.springframework.cache.annotation.EnableCaching;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.cache.RedisCacheConfiguration;
//import org.springframework.data.redis.cache.RedisCacheManager;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//
//import java.time.Duration;
//
//@Configuration
//public class RedisCacheConfig {
//
//    @Bean
//    public RedisCacheConfiguration cacheConfiguration() {
//        return RedisCacheConfiguration.defaultCacheConfig()
//                .entryTtl(Duration.ofHours(1))   // default TTL
//                .disableCachingNullValues();
//    }
//
//    @Bean
//    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
//        return RedisCacheManager.builder(factory)
//                .cacheDefaults(cacheConfiguration())
//                .transactionAware()
//                .build();
//    }
//}
