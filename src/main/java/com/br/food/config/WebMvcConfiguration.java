package com.br.food.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

	private final RequestLoggingInterceptor requestLoggingInterceptor;
	private final PlanFeatureInterceptor planFeatureInterceptor;

	public WebMvcConfiguration(RequestLoggingInterceptor requestLoggingInterceptor,
			PlanFeatureInterceptor planFeatureInterceptor) {
		this.requestLoggingInterceptor = requestLoggingInterceptor;
		this.planFeatureInterceptor = planFeatureInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(requestLoggingInterceptor);
		registry.addInterceptor(planFeatureInterceptor).addPathPatterns("/financial/**", "/product-profit/**");
	}
}
