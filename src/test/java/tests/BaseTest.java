package tests;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import org.testng.annotations.BeforeClass;

import java.io.InputStream;
import java.util.Properties;

public class BaseTest {

    private static final Properties properties = new Properties();

    // Read base URL and endpoint details from config.properties.
    static {
        try (InputStream input = BaseTest.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            properties.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Unable to read config.properties", e);
        }
    }

    protected String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException("Property is missing: " + key);
        }
        return value.trim();
    }

    @BeforeClass
    public void setup() {
        // GIVEN - common API setup
        RestAssured.baseURI = getProperty("baseUri");
        RestAssured.basePath = getProperty("basePath");
        RestAssured.filters(new AllureRestAssured());
    }

    protected int expectedStatus(Object status) {
        return ((Number) status).intValue();
    }

    protected boolean isPositive(Object testType) {
        return "Positive".equalsIgnoreCase(String.valueOf(testType));
    }
}
