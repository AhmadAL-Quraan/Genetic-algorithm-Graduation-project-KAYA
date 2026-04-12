package com.kaya;

import com.kaya.algorithm.run.SchedulerApp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KayaApplication {

    public static void main(String[] args) {
        SchedulerApp.run();
        //SpringApplication.run(KayaApplication.class, args);
    }

}
