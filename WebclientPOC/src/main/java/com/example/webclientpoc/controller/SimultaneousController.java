package com.example.webclientpoc.controller;

import com.example.webclientpoc.Token;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class SimultaneousController {



    @GetMapping("/token/{id}")
    public String getSampleResponse(String id)
    {
        return id;
    }



}
