package com.example.webclientpoc.controller;

import com.example.webclientpoc.Token;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;

@RestController
@RequestMapping("/webClient")
public class WebClientController {



    @GetMapping("/test")
    public String getSampleResponse()
    {
        return "Response using webclient";
    }


    @PostMapping(value = "/postResponse")
    public String getSamplePostResponse(@RequestBody Token token){
        return "Response using post method"+token.getTokenId();


    }
}
