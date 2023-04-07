package com.common.logging.interceptor;

import java.time.Instant;
import java.util.Objects;

import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.common.logging.config.PropertyConfig;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiRequestInterceptor  implements HandlerInterceptor{

    private PropertyConfig propertyConfig;
    
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());
    
    public ApiRequestInterceptor(PropertyConfig propertyConfig) {
        this.propertyConfig = propertyConfig;
    }
        
    @Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
	{
		logger.info("LogInterceptor PreHandle (Start)");
		logger.info("Access service: {} from: {} url: {} principal: {} Time: {}", propertyConfig.getAppName(), getClientIp(request), request.getRequestURL(),
				(request.getUserPrincipal() == null ? "" : request.getUserPrincipal().getName()),Instant.now().toEpochMilli());
		request.setAttribute("startTime",System.currentTimeMillis()); //inject new attribute 'startTime' in request header
		logger.info("LogInterceptor PreHandle(End)");
		
		return  true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {

		logger.info("LogInterceptor after view is rendered (Start)");
		long endTime = System.currentTimeMillis();
		long startTime=Long.parseLong(request.getAttribute("startTime")+"");
		logger.info("Total time taken to process request {} (in milliseconds(ms)) {} ", request.getRequestURL(),(endTime-startTime));
		logger.info("LogInterceptor after view is rendered (End)");
		
	}

	private String getClientIp(HttpServletRequest request) {
		String clientXForwardedForIp = request.getHeader("x-forwarded-for");
		logger.debug("clientXForwardedForIp :: {}", clientXForwardedForIp);
		if (Objects.nonNull(clientXForwardedForIp)) {
			return getIPFromXForwardedHeader(clientXForwardedForIp);
		} else {
			return Objects.nonNull(request.getRemoteAddr()) ? request.getRemoteAddr() : "";
		}
	}
	 private static String getValueByPattern(String value, String patternStr) {
	        String returnValue = null;
	        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(patternStr);
	        java.util.regex.Matcher matcher = pattern.matcher(value);
	        if(matcher.find()) {
	            returnValue = matcher.group();
	        }
	        return returnValue;
	    }
	private String getIPFromXForwardedHeader(String header) {
		return getValueByPattern(header, "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}").trim();
	}

}