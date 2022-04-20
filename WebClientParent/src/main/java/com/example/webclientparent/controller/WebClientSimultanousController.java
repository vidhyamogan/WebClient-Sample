package com.example.webclientparent.controller;

import model.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/public")
public class WebClientSimultanousController {

    @Autowired
    WebClient webClient;


    @PostMapping("/tokens")
    public Flux<Token> getTokens(@RequestBody List<Integer> tokenIds)
    {
         return this.fetchTokens(tokenIds);
    }


    @PostMapping("/parallel/tokens")
    public ParallelFlux<Token> getParallelTokens(@RequestBody List<Integer> tokenIds)
    {
        return this.fetchTokensParallel(tokenIds);
    }

    //squential calls upto 256 default no#, took 12sec to complete all the calls
    public Flux<Token> fetchTokens(List<Integer> tokenIds) {
        return Flux.fromIterable(tokenIds)
                .flatMap(this::getToken);
    }

    //parallel calls , took 375ms to complete the same no of calls in fetchTokens ()
    public ParallelFlux<Token> fetchTokensParallel(List<Integer> tokenIds) {
        return Flux.fromIterable(tokenIds)
                .parallel()
                .flatMap(this::getToken);
    }

    public Mono<Token> getToken(int id) {
        return webClient.get()
                .uri("http://localhost:9898/token/{id}", id)
                .retrieve()
                .bodyToMono(Token.class);
    }
}
