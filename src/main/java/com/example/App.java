package com.example;

import java.util.logging.Logger;

public class App {

    @Generated("coverage-ignore")
    private static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        logger.info("Hello Jenkins!");  // Instead of System.out.println
    }
}
