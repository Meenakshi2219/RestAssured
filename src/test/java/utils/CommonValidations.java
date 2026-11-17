package utils;

import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.testng.Assert;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.lessThan;


public final class CommonValidations {

    private CommonValidations() {
    	//Utility class.. object creation is not required
    }
    public static ValidatableResponse validateCommonResponse(
            Response response,
            int expectedStatusCode,
            long maxTimeInMillis,
            String expectedContentType,
            String schemaFile
    ) {
        ValidatableResponse then = response.then()
                .statusCode(expectedStatusCode)
                .time(lessThan(maxTimeInMillis))
                .contentType(containsString(expectedContentType));

        if (schemaFile != null && !schemaFile.trim().isEmpty()) {
            then.body(matchesJsonSchemaInClasspath(schemaFile));
        }

        return then;
    }

    // 1. Status code validation
    public static void validateStatusCode(Response response, int expectedStatusCode) {
        Assert.assertEquals(
                response.getStatusCode(),
                expectedStatusCode,
                "Status code validation failed."
        );
    }

    // 2. Response time validation (maximum time in milliseconds)
    public static void validateResponseTime(Response response, long maxTimeInMillis) {
        Assert.assertTrue(
                response.getTime() <= maxTimeInMillis,
                "Response time is too high. Actual: " + response.getTime()
                        + " ms; Maximum allowed: " + maxTimeInMillis + " ms."
        );
    }

    // 3. Content-Type validation
    public static void validateContentType(Response response, String expectedContentType) {
        String actualContentType = response.getContentType();

        Assert.assertNotNull(actualContentType, "Content-Type header is missing.");
        Assert.assertTrue(
                actualContentType.toLowerCase().contains(expectedContentType.toLowerCase()),
                "Content-Type mismatch. Expected to contain: " + expectedContentType
                        + "; Actual: " + actualContentType
        );
    }

    // 4. JSON schema validation
    // schemaFile example: "schemas/category-create-schema.json"
    public static void validateSchema(Response response, String schemaFile) {
        response.then().body(matchesJsonSchemaInClasspath(schemaFile));
    }

    // 5. Not-null validation for a JSON response field
    // jsonPath example: "id" or "data.id"
    public static void validateNotNull(Response response, String jsonPath) {
        Object actualValue = response.jsonPath().get(jsonPath);

        Assert.assertNotNull(
                actualValue,
                "Expected field '" + jsonPath + "' should not be null."
        );
    }

    // 6. Data type validation for a JSON response field
    // Examples: String.class, Integer.class, Boolean.class, List.class, Map.class
    public static void validateDataType(
            Response response,
            String jsonPath,
            Class<?> expectedType
    ) {
        Object actualValue = response.jsonPath().get(jsonPath);

        Assert.assertNotNull(
                actualValue,
                "Cannot validate data type. Field '" + jsonPath + "' is null or missing."
        );

        Assert.assertTrue(
                expectedType.isInstance(actualValue),
                "Data type mismatch for '" + jsonPath + "'. Expected: "
                        + expectedType.getSimpleName() + "; Actual: "
                        + actualValue.getClass().getSimpleName()
        );
    }

    // 7. Expected field value validation
    public static void validateFieldValue(
            Response response,
            String jsonPath,
            Object expectedValue
    ) {
        Object actualValue = response.jsonPath().get(jsonPath);

        Assert.assertEquals(
                actualValue,
                expectedValue,
                "Field value mismatch for '" + jsonPath + "'."
        );
    }

    // 8. Field should not be empty (use for String fields)
    public static void validateFieldNotEmpty(Response response, String jsonPath) {
        String actualValue = response.jsonPath().getString(jsonPath);

        Assert.assertNotNull(actualValue, "Field '" + jsonPath + "' is null.");
        Assert.assertFalse(
                actualValue.trim().isEmpty(),
                "Field '" + jsonPath + "' should not be empty."
        );
    }

    // 9. Response header validation
    public static void validateHeader(
            Response response,
            String headerName,
            String expectedValue
    ) {
        String actualValue = response.getHeader(headerName);

        Assert.assertNotNull(actualValue, "Response header '" + headerName + "' is missing.");
        Assert.assertTrue(
                actualValue.contains(expectedValue),
                "Header value mismatch for '" + headerName + "'. Expected to contain: "
                        + expectedValue + "; Actual: " + actualValue
        );
    }

    // 10. Array minimum-size validation
    // jsonPath example: "data" or "items"
    public static void validateArrayMinSize(
            Response response,
            String jsonPath,
            int minimumSize
    ) {
        java.util.List<?> actualList = response.jsonPath().getList(jsonPath);

        Assert.assertNotNull(actualList, "Array '" + jsonPath + "' is missing or null.");
        Assert.assertTrue(
                actualList.size() >= minimumSize,
                "Array '" + jsonPath + "' size is less than expected. Minimum: "
                        + minimumSize + "; Actual: " + actualList.size()
        );
    }

    // 11. Response body should not be empty
    public static void validateResponseBodyNotEmpty(Response response) {
        String responseBody = response.asString();

        Assert.assertNotNull(responseBody, "Response body is null.");
        Assert.assertFalse(responseBody.trim().isEmpty(), "Response body is empty.");
    }

    // 12. Root-level JSON key existence validation
    // Example: validateJsonKeyExists(response, "id")
    public static void validateJsonKeyExists(Response response, String key) {
        java.util.Map<String, Object> responseMap = response.jsonPath().getMap("$");

        Assert.assertNotNull(responseMap, "Response is not a JSON object.");
        Assert.assertTrue(
                responseMap.containsKey(key),
                "Expected JSON key '" + key + "' is missing from the response."
        );
    }
    

 // 13. No field in the root-level JSON object should be null
    
     public static void validateNoNullFields(Response response) {
         java.util.Map<String, Object> responseMap = response.jsonPath().getMap("$");
  
         Assert.assertNotNull(responseMap, "Response is not a JSON object.");
         responseMap.forEach((key, value) ->
                 Assert.assertNotNull(
                         value,
                         "Field '" + key + "' should not be null."
                 )
         );
     }
  
     // 14. Token should be available (not null and not empty)
   
     public static void validateTokenAvailable(String token, String tokenType) {
         Assert.assertNotNull(token, tokenType + " token is null.");
         Assert.assertFalse(
                 token.trim().isEmpty(),
                 tokenType + " token should be available."
         );
     }
}
