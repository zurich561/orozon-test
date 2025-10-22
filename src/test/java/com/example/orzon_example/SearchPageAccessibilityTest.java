package com.example.orzon_example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SearchPageAccessibilityTest {

    @LocalServerPort
    private int port;

    private WebDriver driver;

    @BeforeEach
    void setup() {
        driver = new HtmlUnitDriver(true);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void searchPageContainsAccessibleControls() {
        driver.get("http://localhost:" + port + "/");
        WebElement searchInput = driver.findElement(By.id("searchTerm"));
        assertThat(searchInput.getAttribute("aria-autocomplete")).isEqualTo("list");
        WebElement submitButton = driver.findElement(By.cssSelector("form[role='search'] button"));
        assertThat(submitButton.getText()).contains("Suchen");
    }
}
