package com.vms.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

class EmailTemplateLoaderTest {

    private final EmailTemplateLoader loader = new EmailTemplateLoader(new DefaultResourceLoader());

    @Test
    void shouldLoadTemplateFilesForNotificationType() {
        EmailTemplate template = loader.load("order_confirmation");

        assertEquals("Order confirmation", template.subject());
        assertEquals("Your order has been received and is being processed.", template.content());
    }

    @Test
    void shouldRejectUnsupportedNotificationType() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loader.load("missing_template"));

        assertEquals("Unsupported notificationType: missing_template", exception.getMessage());
    }

    @Test
    void shouldRejectInvalidNotificationTypeFormat() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loader.load("../secret"));

        assertEquals("Invalid notificationType", exception.getMessage());
    }
}
