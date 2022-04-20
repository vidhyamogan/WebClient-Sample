package com.example.webclientparent;


import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.internal.StringUtil;
import model.HttpRequestContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import javax.xml.transform.Source;

import java.time.Duration;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@Configuration
public class WebClientConfiguration {

    @Bean
    public WebClient getWebClient(){
       return    WebClient.builder()
                .baseUrl("http://localhost:8081")
                .defaultHeader(CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE)
                .build();

    }


    public WebClient WebClientConfiguration()
    {
        return WebClient
                .builder()
                .baseUrl("http://localhost:8081")
                .exchangeStrategies(exchangeStrategies())
                .filter(getExchangeFilterFunction())
                .filter(ExchangeFilterFunction.ofResponseProcessor(this::getExchangeFilterResponseFunction))
                .clientConnector(getReactorClientHttpConnector()).build();
    }

    //custom connection provider
    private ConnectionProvider getConnectionProvider()
    {
       return ConnectionProvider
                .builder("providerName")
                .maxConnections(5)
                .pendingAcquireTimeout(Duration.ofSeconds(5000))
                .maxIdleTime(Duration.ofSeconds(5))
                .build();

    }

    //add differnet statergies
    private ExchangeStrategies exchangeStrategies()
    {
        return ExchangeStrategies
                .builder()
                .codecs(clientCodecConfigurer -> {
                    clientCodecConfigurer.customCodecs().register(new ByteArrayHttpMessageConverter());
                    clientCodecConfigurer.customCodecs().register(new StringHttpMessageConverter());
                    clientCodecConfigurer.customCodecs().register(new ResourceHttpMessageConverter());
                    clientCodecConfigurer.customCodecs().register(new SourceHttpMessageConverter<Source>());
                    if(Boolean.TRUE)
                    {
                        clientCodecConfigurer.customCodecs().register(new Jackson2CborDecoder());
                    }

                }).build();
    }

    private ReactorClientHttpConnector getReactorClientHttpConnector()
    {
        return new ReactorClientHttpConnector(HttpClient
                .create(getConnectionProvider())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
                .doOnConnected(connection -> connection.addHandler(new ReadTimeoutHandler(100))));
    }


    //add intercepts to the request and response
    private ExchangeFilterFunction getExchangeFilterFunction()
    {
        ExchangeFilterFunction function = (request,nextFilter)->{
            String[] var;
            String headerName;
            String[] incomingheader = request.headers().keySet().toArray(new String[0]);
            var = incomingheader;
            for(int i=0;i<incomingheader.length;++i)
            {
                headerName = var[i];
                if(headerName.contains("host")) request.headers().remove(headerName);
            }

            return nextFilter.exchange(request);
        };

        return function;
    }

    //add intercepts to the response
    private Mono<ClientResponse> getExchangeFilterResponseFunction(ClientResponse clientResponse) {
        String userRole = clientResponse.headers().asHttpHeaders().containsKey("userRole")?"user":null;
        if(!StringUtils.isEmpty(userRole))
        {
            HttpRequestContext.getData().put("userRole",userRole);
        }
        return Mono.just(clientResponse);
    }
}
