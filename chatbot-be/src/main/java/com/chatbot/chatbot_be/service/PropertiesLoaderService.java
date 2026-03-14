package com.chatbot.chatbot_be.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class PropertiesLoaderService {
	

    private final Map<String, String> questionQueryMap = new HashMap<>();

    @PostConstruct
    public void loadAllProperties() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
     //   Resource[] resources = resolver.getResources("classpath:queries/*.properties");
        Resource[] resources = resolver.getResources("classpath*:/*.properties");

        for (Resource resource : resources) {
            try (InputStream input = resource.getInputStream()) {
                Properties props = new Properties();
                props.load(input);
                props.forEach((key, value) -> questionQueryMap.put(key.toString(), value.toString()));
                System.out.println("Loaded file: " + resource.getFilename());
            }
        }
        System.out.println("✅ Total questions loaded: " + questionQueryMap.size());
    }

    public Map<String, String> getAll() {
        return questionQueryMap;
    }

}
