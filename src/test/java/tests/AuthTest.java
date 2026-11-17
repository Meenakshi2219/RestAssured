package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import utils.CommonValidations;
import utils.DataReader;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("InfoEz API")
@Feature("Authentication")
@Listeners(utils.ExtentListener.class)
public class AuthTest extends BaseTest {

    public static String adminToken = "";
    public static String accessToken = "";

    @DataProvider(name = "registerData")
    public Object[][] registerData() {
        return DataReader.read(getProperty("auth.register.data"));
    }

    @DataProvider(name = "loginData")
    public Object[][] loginData() {
        return DataReader.read(getProperty("auth.login.data"));
    }

    @DataProvider(name = "getMeData")
    public Object[][] getMeData() {
        return DataReader.read(getProperty("auth.me.data"));
    }

    // ==================== 1. REGISTER API ====================
    @Test(dataProvider = "registerData", priority = 1)
    @Story("Register")
    @Description("Register API - Given, When and Then format")
    public void register(Map<String, Object> testData) {

        // GIVEN - Prepare request body from register.json
    	
        String email = String.valueOf(testData.get("email"));
        if (email.contains("${unique}")) {
            email = email.replace("${unique}", String.valueOf(System.currentTimeMillis()));
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", testData.get("name"));
        requestBody.put("email", email);
        requestBody.put("password", testData.get("password"));
        requestBody.put("role", testData.get("role"));
        requestBody.put("phone", testData.get("phone"));

        // WHEN - Send POST request to Register
        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                
                .when()
                .post(getProperty("auth.register.endpoint"));
        
        System.out.println("Status Code : " + response.getStatusCode());
        System.out.println("Response Body : " + response.asPrettyString());

        // THEN - Validate response one by one
        if (!isPositive(testData.get("testType"))) {
        	
            // THEN - Common validations for negative response 
            CommonValidations.validateCommonResponse(
                    response, expectedStatus(testData.get("expectedStatus")),
                    3000, "application/json", null);
            CommonValidations.validateResponseBodyNotEmpty(response);
            CommonValidations.validateJsonKeyExists(response, "detail"); // Negative error response
            return;
        }

        // THEN - Common validations for positive response 
        CommonValidations.validateCommonResponse(
                response, expectedStatus(testData.get("expectedStatus")),
                3000, "application/json", "schemas/auth-user-token-schema.json");
        CommonValidations.validateFieldNotEmpty(response, "token"); // 5. Token should not be empty
        CommonValidations.validateNotNull(response, "user.id"); // 6. User ID should not be null
        CommonValidations.validateFieldValue(response, "user.name", requestBody.get("name")); // 7. Name should match request
        CommonValidations.validateFieldValue(response, "user.email", email); // 8. Email should match request
        CommonValidations.validateFieldValue(response, "user.role", requestBody.get("role")); // 9. Role should match request
        CommonValidations.validateFieldNotEmpty(response, "user.status"); // 10. User status should not be empty
        CommonValidations.validateHeader(response, "Content-Type", "application/json"); // 11. Response header
        CommonValidations.validateJsonKeyExists(response, "user"); // 12. User key exists
        response.then().body("user.password", nullValue()); // API-specific: password should not be returned
        CommonValidations.validateDataType(response, "user.name", String.class); // 12. Name data type should be String
    }

    // ==================== 2. LOGIN API ====================
    @Test(dataProvider = "loginData", priority = 2)
    @Story("Login")
    @Description("Login API - Given, When and Then format")
    public void login(Map<String, Object> testData) {

        // GIVEN - Prepare login request body from login.json
    	
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", testData.get("email"));
        requestBody.put("password", testData.get("password"));

        // WHEN - Send POST request to Login
        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                
                .when()
                .post(getProperty("auth.login.endpoint"));
        System.out.println("Status Code : " + response.getStatusCode());
        System.out.println("Response Body : " + response.asPrettyString());

        // THEN - Validate response
        if (!isPositive(testData.get("testType"))) {
        	
            // THEN - Common validations for negative response 
            CommonValidations.validateCommonResponse(
                    response, expectedStatus(testData.get("expectedStatus")),
                    3000, "application/json", null);
            CommonValidations.validateResponseBodyNotEmpty(response);
            CommonValidations.validateJsonKeyExists(response, "detail");
            return;
        }

        // THEN - Common validations for positive response 
        
        CommonValidations.validateCommonResponse(
                response, expectedStatus(testData.get("expectedStatus")),
                3000, "application/json", "schemas/auth-user-token-schema.json");
        CommonValidations.validateFieldNotEmpty(response, "token"); // 5. Token
        CommonValidations.validateNotNull(response, "user.id"); // 6. User ID
        CommonValidations.validateFieldValue(response, "user.email", requestBody.get("email")); // 7. Email
        CommonValidations.validateFieldNotEmpty(response, "user.role"); // 8. Role
        CommonValidations.validateFieldNotEmpty(response, "user.status"); // 9. Status
        CommonValidations.validateDataType(response, "user.email", String.class); // 10. Email data type
        CommonValidations.validateHeader(response, "Content-Type", "application/json"); // 11. Response header
        CommonValidations.validateJsonKeyExists(response, "user"); // 12. User key exists

        // Save token for the next APIs
        
        String token = response.jsonPath().getString("token");
        String role = response.jsonPath().getString("user.role");
        if ("admin".equalsIgnoreCase(role)) {
            adminToken = token;
        } else {
            accessToken = token;
        }
    }

    // ==================== 3. GET ME API ====================
    
    @Test(dataProvider = "getMeData", priority = 3, dependsOnMethods = "login")
    @Story("Get Current User")
    @Description("Get Me API - admin, access and invalid token cases")
    public void getMe(Map<String, Object> testData) {

        // GIVEN - Select token based on get-me.json
    	
        String tokenType = String.valueOf(testData.get("tokenType"));
        String token = getToken(tokenType);

        // WHEN - Send GET request to Get Me
        Response response = given()
                .header("Authorization", "Bearer " + token)
                
                .when()
                .get(getProperty("auth.me.endpoint"));
        System.out.println("Status Code : " + response.getStatusCode());
        System.out.println("Response Body : " + response.asPrettyString());

        // THEN - Validate response
        if ("invalid".equalsIgnoreCase(tokenType)) {
        	
            // THEN - Common validations for invalid-token response 
            CommonValidations.validateCommonResponse(
                    response, expectedStatus(testData.get("expectedStatus")),
                    3000, "application/json", null);
            CommonValidations.validateResponseBodyNotEmpty(response);
            CommonValidations.validateJsonKeyExists(response, "detail");
            return;
        }

     // THEN - Common validations for positive response 
        
        CommonValidations.validateCommonResponse(
                response, expectedStatus(testData.get("expectedStatus")),
                3000, "application/json", "schemas/auth-me-schema.json");
        CommonValidations.validateNotNull(response, "id"); // 5. ID
        CommonValidations.validateFieldNotEmpty(response, "name"); // 6. Name
        CommonValidations.validateFieldNotEmpty(response, "email"); // 7. Email
        CommonValidations.validateFieldNotEmpty(response, "role"); // 8. Role
        CommonValidations.validateFieldNotEmpty(response, "status"); // 9. Status
        CommonValidations.validateDataType(response, "name", String.class); // 10. Name data type
        CommonValidations.validateHeader(response, "Content-Type", "application/json"); // 11. Response header
        CommonValidations.validateJsonKeyExists(response, "email"); // 12. Email key exists
        response.then().body("email", matchesPattern("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")); // 10. Email format
    }

    private String getToken(String tokenType) {
        if ("admin".equalsIgnoreCase(tokenType)) return adminToken;
        if ("access".equalsIgnoreCase(tokenType)) return accessToken;
        if ("invalid".equalsIgnoreCase(tokenType)) return "invalid_token";
        throw new IllegalArgumentException("Unknown tokenType in get-me.json: " + tokenType);
    }
}
