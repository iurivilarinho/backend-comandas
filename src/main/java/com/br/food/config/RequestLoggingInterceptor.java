package com.br.food.config;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
	private static final String REQUEST_ID_ATTRIBUTE = "requestId";
	private static final String REQUEST_START_ATTRIBUTE = "requestStart";
	private static final String REQUEST_ID_HEADER = "X-Request-Id";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		String requestId = UUID.randomUUID().toString();
		request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
		request.setAttribute(REQUEST_START_ATTRIBUTE, System.currentTimeMillis());
		response.setHeader(REQUEST_ID_HEADER, requestId);
		LOGGER.info("[{}] Started {} {}", requestId, request.getMethod(), request.getRequestURI());
		return true;
	}

	@Override
	public void afterCompletion(
			HttpServletRequest request,
			HttpServletResponse response,
			Object handler,
			Exception exception) {
		String requestId = String.valueOf(request.getAttribute(REQUEST_ID_ATTRIBUTE));
		Object startValue = request.getAttribute(REQUEST_START_ATTRIBUTE);
		long startedAt = startValue instanceof Long ? (Long) startValue : System.currentTimeMillis();
		long duration = System.currentTimeMillis() - startedAt;
		LOGGER.info("[{}] Finished {} {} with status {} in {} ms",
				requestId,
				request.getMethod(),
				request.getRequestURI(),
				response.getStatus(),
				duration);
	}
}
