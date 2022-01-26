package com.study.gulimall.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static javafx.scene.input.KeyCode.R;

@Configuration
public class SentinelGatewayConfig {

    public SentinelGatewayConfig(){
        GatewayCallbackManager.setBlockHandler(new BlockRequestHandler() {
            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange serverWebExchange, Throwable throwable) {
                Mono<ServerResponse> body = ServerResponse.ok().body(Mono.just("{'msg':'请求流量过大','code':10002}"), String.class);
                return body;
            }
        });
    }

}
