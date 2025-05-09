package com.blog.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class UserAccessInterceptor implements HandlerInterceptor {

    private final CloudWatchClient cloudWatchClient;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    public UserAccessInterceptor(CloudWatchClient cloudWatchClient) {
        this.cloudWatchClient = cloudWatchClient;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String clientIp = extractClientIp(request);
        String accessTime = FORMATTER.format(Instant.now());
        String api = request.getRequestURI();
        String method = request.getMethod();

        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        long usedMemory = allocatedMemory - freeMemory;
        double usagePercentage = (double) usedMemory / maxMemory * 100;
        String percentage = String.format("%.2f", usagePercentage).concat("%");

        MetricDatum datum = MetricDatum.builder()
                .metricName("MyApiTrace")
                .timestamp(Instant.now())
                .value(1.0)
                .unit(StandardUnit.COUNT)
                .dimensions(
                        Dimension.builder().name("ClientIP").value(clientIp).build(),
                        Dimension.builder().name("AccessTime").value(accessTime).build(),
                        Dimension.builder().name("api").value(api).build(),
                        Dimension.builder().name("method").value(method).build(),
                        Dimension.builder().name("usagePercentage").value(percentage).build()
                )
                .build();

        PutMetricDataRequest putMetricDataRequest = PutMetricDataRequest.builder()
                .namespace("MyAppNamespace")
                .metricData(datum)
                .build();

        cloudWatchClient.putMetricData(putMetricDataRequest);
        return true;
    }

    private String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // In case of multiple IPs, take the first one
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "Unknown";
    }
}
