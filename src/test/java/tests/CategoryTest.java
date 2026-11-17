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
@Feature("Category")
@Listeners(utils.ExtentListener.class)
public class CategoryTest extends BaseTest {

    private static String createdCategoryId = "";

    @DataProvider(name = "createCategoryData")
    public Object[][] createCategoryData() {
        return DataReader.read(getProperty("category.create.data"));
    }

    @DataProvider(name = "updateCategoryData")
    public Object[][] updateCategoryData() {
        return DataReader.read(getProperty("category.update.data"));
    }

    @DataProvider(name = "getCategoryData")
    public Object[][] getCategoryData() {
        return DataReader.read(getProperty("category.get.data"));
    }
    

@DataProvider(name = "deleteCategoryData")
    public Object[][] deleteCategoryData() {
        return DataReader.read(getProperty("category.delete.data"));
    }

    // ==================== 4. CREATE CATEGORY API ====================
    
    @Test(dataProvider = "createCategoryData", priority = 4, dependsOnMethods = "tests.AuthTest.login")
    @Story("Create Category")
    @Description("Create Category API - Given, When and Then format")
    public void createCategory(Map<String, Object> testData) {

        // GIVEN - Prepare token and request body from category-create.json
    	
        String token = getToken(String.valueOf(testData.get("tokenType")));
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", testData.get("name"));
        requestBody.put("icon", testData.get("icon"));
        requestBody.put("color", testData.get("color"));

        // WHEN - Send POST request to Create Category 
        
        Response response = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                
                .when()
                .post(getProperty("category.create.endpoint"));

                System.out.println("Status Code : " + response.getStatusCode());
                System.out.println("Response Body : " + response.asPrettyString());

        // THEN - Validate response
        
        if (!isPositive(testData.get("testType"))) {
            // THEN - Common validations for negative response 
            CommonValidations.validateCommonResponse(
                    response, expectedStatus(testData.get("createcat_expectedStatus")),
                    3000, "application/json", null);
            CommonValidations.validateResponseBodyNotEmpty(response);
            response.then().body("$",anyOf(
                    hasKey("detail"), hasKey("message"), hasKey("error"), hasKey("errors")));
            return;
        }

        // THEN - Common validation for Positive responses
        
        CommonValidations.validateCommonResponse(
                response, expectedStatus(testData.get("createcat_expectedStatus")),
                3000, "application/json", "schemas/category-create-schema.json");
        CommonValidations.validateNotNull(response, "id"); // 5. ID not null
        CommonValidations.validateFieldValue(response, "name", requestBody.get("name")); // 6. Name matches request
        CommonValidations.validateFieldValue(response, "icon", requestBody.get("icon")); // 7. Icon matches request
        CommonValidations.validateFieldValue(response, "color", requestBody.get("color")); // 8. Color matches request
        CommonValidations.validateFieldNotEmpty(response, "slug"); // 9. Slug not empty
        CommonValidations.validateNotNull(response, "is_active"); // 10. Active field not null
        CommonValidations.validateDataType(response, "name", String.class); // 11. Name data type
        CommonValidations.validateHeader(response, "Content-Type", "application/json"); // 12. Response header
        CommonValidations.validateJsonKeyExists(response, "id"); // Confirm ID key exists

        // Save the created category ID. 
        
        createdCategoryId = response.jsonPath().getString("id");
    }

    // ==================== 5. GET CATEGORY API ====================
    
    @Test(dataProvider = "getCategoryData", priority = 5)
    @Story("Get Categories")
    @Description("Get Categories API - Given, When and Then format")
    public void getCategories(Map<String, Object> testData) {

        // GIVEN - No authorization header is required 
        // WHEN - Send GET request to Category 
        Response response = given()
                .when()
                .get(getProperty("category.get.endpoint"));
               System.out.println("Status Code : " + response.getStatusCode());
               System.out.println("Response Body : " + response.asPrettyString());

        // THEN - Validate response
        
        CommonValidations.validateCommonResponse(
                response, expectedStatus(testData.get("expectedStatus")),
                3000, "application/json", "schemas/category-list-schema.json");
        CommonValidations.validateResponseBodyNotEmpty(response); // 5. Response body not empty
        CommonValidations.validateDataType(response, "$", java.util.List.class); // 6. Response is an array
        CommonValidations.validateArrayMinSize(response, "$", 1); // 7. At least one category is returned
        CommonValidations.validateHeader(response, "Content-Type", "application/json"); // 8. Response header
    }

//    // ==================== 6. UPDATE CATEGORY API ====================
//    @Test(dataProvider = "updateCategoryData", priority = 6, dependsOnMethods = "createCategory")
//    @Story("Update Category")
//    @Description("Update Category API - Uses category ID saved by Create Category API")
//    public void updateCategory(Map<String, Object> testData) {
//
//        // GIVEN - Use category ID from Create API and update body from category-update.json
//        if (createdCategoryId == null || createdCategoryId.trim().isEmpty()) {
//            throw new RuntimeException("Category ID is empty. Check Create Category API first.");
//        }
//        String token = getToken(String.valueOf(testData.get("tokenType")));
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("name", testData.get("update_name"));
//        requestBody.put("icon", testData.get("update_icon"));
//        requestBody.put("color", testData.get("update_color"));
//
//        // WHEN - Send PUT request to Update Category endpoint
//        Response response = given()
//                .header("Authorization", "Bearer " + token)
//                .contentType(ContentType.JSON)
//                .body(requestBody)
//                .when()
//                .put(getProperty("category.update.endpoint") + "/" + createdCategoryId);
//
//        // THEN - Validate response
//        if (!isPositive(testData.get("testType"))) {
//            // THEN - Common validations for negative response (schema is not applicable).
//            CommonValidations.validateCommonResponse(
//                    response, expectedStatus(testData.get("updatecat_expectedStatus")),
//                    3000, "application/json", null);
//            CommonValidations.validateResponseBodyNotEmpty(response);
//            response.then().body(anyOf(
//                    hasKey("detail"), hasKey("message"), hasKey("error"), hasKey("errors")));
//            return;
//        }
//
//        // THEN - Reusable common validations: status code, response time and content type.
//        // Update response schema is not applied until the API response structure is confirmed.
//        CommonValidations.validateCommonResponse(
//                response, expectedStatus(testData.get("updatecat_expectedStatus")),
//                3000, "application/json", null);
//        CommonValidations.validateNotNull(response, "id"); // 4. ID not null
//        CommonValidations.validateFieldValue(response, "name", requestBody.get("name")); // 5. Updated name
//        CommonValidations.validateFieldValue(response, "icon", requestBody.get("icon")); // 6. Updated icon
//        CommonValidations.validateFieldValue(response, "color", requestBody.get("color")); // 7. Updated color
//        CommonValidations.validateDataType(response, "name", String.class); // 8. Name data type
//        CommonValidations.validateHeader(response, "Content-Type", "application/json"); // 9. Response header
//        CommonValidations.validateJsonKeyExists(response, "id"); // 10. ID key exists
//    }

 // ==================== 7. DELETE CATEGORY API ====================
    
    @Test(dataProvider = "deleteCategoryData", priority = 7, dependsOnMethods = "createCategory")
    @Story("Delete Category")
    @Description("Delete Category API - Positive uses ID saved by Create Category API, Negative uses c_id from data file")
    public void deleteCategory(Map<String, Object> testData) {
 
        // GIVEN - Prepare token and category ID
    	
        String tokenType = String.valueOf(testData.get("tokenType"));
        String token = getToken(tokenType);
        boolean positive = isPositive(testData.get("testType"));
        String categoryId;
 
        if ("invalid".equalsIgnoreCase(tokenType)) {
           
            categoryId = (createdCategoryId == null || createdCategoryId.trim().isEmpty())
                    ? "valid_placeholder_id" : createdCategoryId;
        } else if (positive) {
            if (createdCategoryId == null || createdCategoryId.trim().isEmpty()) {
                throw new RuntimeException("Category ID is empty. Check Create Category API first.");
            }
            categoryId = createdCategoryId;
        } else {
            Object dataId = testData.get("c_id");
            categoryId = (dataId == null || String.valueOf(dataId).trim().isEmpty())
                    ? "invalid_id" : String.valueOf(dataId);
        }
 
        // WHEN - Send DELETE request to Delete Category 
        
        Response response = given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete(getProperty("category.delete.endpoint") + "/" + categoryId);
        System.out.println("Status Code : " + response.getStatusCode());
        System.out.println("Response Body : " + response.asPrettyString());
 
        // THEN - Validate response
        if (!positive) {
            // THEN - Common validations for negative response 
        	
            CommonValidations.validateCommonResponse(
                    response, expectedStatus(testData.get("deletecat_expectedStatus")),
                    3000, "application/json", null);
            CommonValidations.validateResponseBodyNotEmpty(response);
            response.then().statusCode(anyOf(
                    is(400), is(401), is(403), is(404), is(422), is(500)));
            response.then().body("$", anyOf(
                    hasKey("detail"), hasKey("message"), hasKey("error"), hasKey("errors")));
            return;
        }
 
        // THEN - Common validations for positive response 
        
        CommonValidations.validateCommonResponse(
                response, expectedStatus(testData.get("deletecat_expectedStatus")),
                3000, "application/json", "schemas/category-delete-schema.json");
        CommonValidations.validateJsonKeyExists(response, "ok");           // 5. "ok" key exists
        CommonValidations.validateNotNull(response, "ok");                 // 6. "ok" not null
        CommonValidations.validateDataType(response, "ok", Boolean.class); // 7. "ok" is boolean
        CommonValidations.validateFieldValue(response, "ok", true);        // 8. "ok" is true
        CommonValidations.validateHeader(response, "Content-Type", "application/json"); // 9. Response header
        CommonValidations.validateNoNullFields(response);                  // 10. No field is null
        CommonValidations.validateTokenAvailable(token, tokenType);        // 11. Token is available
    }
    private String getToken(String tokenType) {
        if ("admin".equalsIgnoreCase(tokenType)) return AuthTest.adminToken;
        if ("access".equalsIgnoreCase(tokenType)) return AuthTest.accessToken;
        if ("invalid".equalsIgnoreCase(tokenType)) return "invalid_token";
        throw new IllegalArgumentException("Unknown tokenType in category test data: " + tokenType);
    }
}
