package org.springframework.cloud.kubernetes.discovery.ext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;

import java.net.UnknownHostException;

public class KubernetesAutoServiceRegistrationListener implements SmartApplicationListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(KubernetesAutoServiceRegistrationListener.class);

    private final KubernetesAutoServiceRegistration autoServiceRegistration;

    KubernetesAutoServiceRegistrationListener(KubernetesAutoServiceRegistration autoServiceRegistration) {
        this.autoServiceRegistration = autoServiceRegistration;
    }

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return WebServerInitializedEvent.class.isAssignableFrom(eventType);
    }

    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return true;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof WebServerInitializedEvent) {
            WebServerInitializedEvent event = (WebServerInitializedEvent) applicationEvent;
            try {
                autoServiceRegistration.setRegistration(event.getWebServer().getPort());
                autoServiceRegistration.start();
            } catch (UnknownHostException e) {
                LOGGER.error("Error registering to kubernetes", e);
            }
        }
    }

}
