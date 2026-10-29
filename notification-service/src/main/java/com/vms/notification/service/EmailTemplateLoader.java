package com.vms.notification.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplateLoader {

    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");

    private final ResourceLoader resourceLoader;

    public EmailTemplateLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public EmailTemplate load(String notificationType) {
        if (notificationType == null
                || notificationType.isBlank()
                || !TEMPLATE_PATTERN.matcher(notificationType).matches()) {
            throw new IllegalArgumentException("Invalid notificationType");
        }

        String templatePath = "classpath:email-templates/" + notificationType;
        Resource subjectResource = resourceLoader.getResource(templatePath + "/subject.txt");
        Resource contentResource = resourceLoader.getResource(templatePath + "/content.txt");

        try {
            if (!subjectResource.exists() || !contentResource.exists()) {
                throw new IllegalArgumentException("Unsupported notificationType: " + notificationType);
            }

            return new EmailTemplate(
                    subjectResource.getContentAsString(StandardCharsets.UTF_8).trim(),
                    contentResource.getContentAsString(StandardCharsets.UTF_8).trim());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load template files", exception);
        }
    }
}
