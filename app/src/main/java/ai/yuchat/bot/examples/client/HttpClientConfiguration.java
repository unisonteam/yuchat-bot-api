package ai.yuchat.bot.examples.client;

import ai.yuchat.bot.event.invoker.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class HttpClientConfiguration {
    @Value("${yuchat.url}") String yuchatUrl;
    @Value("${yuchat.token}") String yuchatToken;

    @Bean
    public YuchatHttpClient httpClient() {
        ApiClient client = new ApiClient();
        client.setScheme("https");
        client.setHost(yuchatUrl);
        client.setRequestInterceptor(
                req -> System.out.println("[HTTP] Send request: " + req)
        );
        client.setResponseInterceptor(
               resp -> System.out.println("[HTTP] Got response: " + resp)
        );

        client.setRequestInterceptor(builder -> builder.header("Authorization", "Bearer " + yuchatToken));

        return new YuchatHttpClient(client);
    }
}
