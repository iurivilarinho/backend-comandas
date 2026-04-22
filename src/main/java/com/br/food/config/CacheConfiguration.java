package com.br.food.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.br.food.service.ProductService;

@Configuration
public class CacheConfiguration {

	@Bean
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager(ProductService.MENU_PRODUCTS_CACHE);
	}
}
