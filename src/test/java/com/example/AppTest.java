package com.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AppTest {

    // @Test
    void testMain() {
        // Assert that calling main() does not throw any exception
        assertDoesNotThrow(() -> App.main(new String[]{}));
    }
}
