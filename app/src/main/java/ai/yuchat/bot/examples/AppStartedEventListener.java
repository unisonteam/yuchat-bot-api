package ai.yuchat.bot.examples;


import ai.yuchat.bot.examples.service.YuchatBotService;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Start listening updates when the application is ready. */
@Component
public class AppStartedEventListener {
    private final YuchatBotService service;

    public AppStartedEventListener(YuchatBotService service) {
        this.service = service;
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        service.listenUpdates();
    }
}
