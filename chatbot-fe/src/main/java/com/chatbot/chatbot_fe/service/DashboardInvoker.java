package com.chatbot.chatbot_fe.service;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * DashboardInvoker — populates dashboard widget values dynamically.
 *
 * Each metric in dashboardData has a "url" field.
 * This service calls that URL and puts the returned "value" back into the metric map.
 *
 * Example metric:
 *   { "key": "Total Employees", "type": "count", "value": null, "url": "/emp/count/all" }
 *
 * After invocation:
 *   { "key": "Total Employees", "type": "count", "value": 142, "url": "/emp/count/all" }
 *
 * URLs starting with "/" are treated as local FE calls (http://localhost:9091).
 * Full URLs (http://...) are called directly.
 */
@Service
public class DashboardInvoker {

    private static final Logger log = LoggerFactory.getLogger(DashboardInvoker.class);

    @Value("${server.port:9091}")
    private String fePort;

    private final RestTemplate rest = new RestTemplate();

    /**
     * Iterates dashboardData, calls each metric's URL, and fills in the "value".
     *
     * @param dashboardData  List of module maps, each with a "metrics" list
     * @return the same list with "value" fields populated
     */
    public List<Map<String, Object>> populateDashboardValues(
            List<Map<String, Object>> dashboardData) {

        for (Map<String, Object> moduleMap : dashboardData) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> metrics =
                    (List<Map<String, Object>>) moduleMap.get("metrics");

            if (metrics == null) continue;

            for (Map<String, Object> metric : metrics) {
                String url = (String) metric.get("url");
                if (url == null || url.isBlank()) continue;

                try {
                    String fullUrl = url.startsWith("http")
                            ? url
                            : "http://localhost:" + fePort + url;

                    log.debug("DashboardInvoker calling: {}", fullUrl);

                    ResponseEntity<Map<String, Object>> resp = rest.exchange(
                            fullUrl, HttpMethod.GET, null,
                            new ParameterizedTypeReference<Map<String, Object>>() {});

                    if (resp.getBody() != null && resp.getBody().containsKey("value")) {
                        metric.put("value", resp.getBody().get("value"));
                        log.debug("Populated '{}' = {}", metric.get("key"), metric.get("value"));
                    }

                } catch (Exception e) {
                    // Non-fatal — widget will show 0 / empty
                    log.warn("DashboardInvoker failed for url '{}': {}", url, e.getMessage());
                    if (metric.get("value") == null) {
                        // Set safe defaults so the UI doesn't break
                        String type = (String) metric.getOrDefault("type", "count");
                        metric.put("value", "list".equals(type) ? java.util.Collections.emptyList() : 0);
                    }
                }
            }
        }
        return dashboardData;
    }
}