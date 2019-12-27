package pl.piomin.services.api.registration;

import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;

public class KubernetesAutoServiceRegistrationListener implements SmartApplicationListener {

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
            int port = event.getWebServer().getPort();
            autoServiceRegistration.setPort(port);
            autoServiceRegistration.start();
        }
    }

}
