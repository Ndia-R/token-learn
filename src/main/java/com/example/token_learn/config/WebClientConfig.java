package com.example.token_learn.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient設定クラス
 *
 * <p>IdPへのHTTPリクエストに使用する汎用WebClientを提供します。</p>
 * <p>デフォルトタイムアウト: 30秒</p>
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        int defaultTimeout = 30;

        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, defaultTimeout * 1000)
            .responseTimeout(Duration.ofSeconds(defaultTimeout))
            .followRedirect(true)
            .doOnConnected(
                conn -> conn
                    .addHandlerLast(new ReadTimeoutHandler(defaultTimeout, TimeUnit.SECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(defaultTimeout, TimeUnit.SECONDS))
            );

        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

        return WebClient.builder().clientConnector(connector).build();
    }
}
