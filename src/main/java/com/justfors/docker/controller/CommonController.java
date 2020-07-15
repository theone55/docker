package com.justfors.docker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CommonController {


    @GetMapping
    public ResponseEntity<String> test() throws UnknownHostException, InterruptedException {
        InetAddress inetAddress = InetAddress.getLocalHost();
        System.out.println(Instant.now());
        Thread.sleep(5000);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("IP Address:- ");
        stringBuilder.append(inetAddress.getHostAddress());
        stringBuilder.append("\n");
        stringBuilder.append("Host Name:- ");
        stringBuilder.append(inetAddress.getHostName());
        stringBuilder.append("\n");
        stringBuilder.append("MachineName:- ");
        stringBuilder.append(getComputerName());

        return new ResponseEntity(stringBuilder, HttpStatus.OK);
    }

    private String getComputerName() {
        Map<String, String> env = System.getenv();
        if (env.containsKey("COMPUTERNAME"))
            return env.get("COMPUTERNAME");
        else return env.getOrDefault("HOSTNAME", "Unknown Computer");
    }


}
