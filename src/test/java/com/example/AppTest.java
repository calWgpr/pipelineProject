package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AppTest {

    @Test
    void testRun() {
        App app = new App();
        String result = app.run();
        assertEquals("App started", result);
    }
}
