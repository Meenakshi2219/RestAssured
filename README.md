# InfoEz Rest Assured - Beginner Friendly Project

This project uses the simple Rest Assured pattern in every API test:

```java
given()       // request setup: headers, content type, body
.when()       // API action: post(), get(), put()
.then()       // response checks: status code, body, response time
```

## API test classes

| Class | API | HTTP method | Test data file |
|---|---|---|---|
| AuthTest | Register | POST | testdata/register.json |
| AuthTest | Login | POST | testdata/login.json |
| AuthTest | Get Me | GET | testdata/me.json |
| CategoryTest | Create Category | POST | testdata/category-create.json |
| CategoryTest | Get Categories | GET | testdata/category-get.json |
| CategoryTest | Update Category | PUT | testdata/category-update.json |

## How test data works

Each JSON object is one test case. `DataReader` reads the JSON file and TestNG `@DataProvider` passes each object to the test method. The request body is created inside the **Given** section using the values from that object.

## How to read a test method

1. **GIVEN**: Prepare token, headers and request body.
2. **WHEN**: Call the endpoint using `post()`, `get()` or `put()`.
3. **THEN**: Check expected status code first, then response time, content type, schema and response fields.

Each validation is written on a separate line with a numbered comment to make it easy to understand. Negative test data checks the expected error status and error response.

## API chaining

The successful Create Category response ID is saved in `createdCategoryId`. Update Category uses that ID in its URL. Login saves admin and business-owner tokens for later authenticated requests.

## Configuration

Update `src/test/resources/config.properties` if the API base URL or endpoint paths change. All six endpoint paths and the JSON data file paths are maintained there.

## Run tests

```bash
mvn clean test
```

## Important note

The endpoint paths and expected status codes are based on the supplied project/Postman data. The API server must be reachable to verify runtime behavior. This project package was prepared, but the live APIs and Maven compilation were not executed in this environment. If the API returns a different response structure (especially Update Category), adjust the corresponding `then()` checks to match the actual response.


## Reusable common validations in the THEN section

`src/test/java/utils/CommonValidations.java` contains the common reusable validation methods.
Import it in both `AuthTest.java` and `CategoryTest.java`:

```java
import utils.CommonValidations;
```

The four validations used by almost every API (status code, response time, content type and
JSON schema) are grouped into one reusable method. Call it in the `THEN` section:

```java
// THEN - common validations
CommonValidations.validateCommonResponse(
        response,
        201,                                      // expected status code
        3000,                                     // maximum response time in milliseconds
        "application/json",                      // expected content type
        "schemas/category-create-schema.json"   // schema file
);

// API-specific validations remain below the common validations
CommonValidations.validateNotNull(response, "id");
CommonValidations.validateFieldValue(response, "name", requestBody.get("name"));
```

For negative test cases, where the success schema does not apply, pass `null` as the last
argument. The method still checks status code, response time and content type:

```java
CommonValidations.validateCommonResponse(
        response, 400, 3000, "application/json", null);
```

The same call pattern is already added to all six API tests in `AuthTest.java` and
`CategoryTest.java`. Endpoint-specific checks (such as token, category ID, name and error body)
remain in the respective `THEN` section so the tests are easy to understand.

### Other reusable validation methods

1. `validateNotNull(response, "id")`
2. `validateDataType(response, "name", String.class)`
3. `validateFieldValue(response, "name", expectedValue)`
4. `validateFieldNotEmpty(response, "slug")`
5. `validateHeader(response, "Content-Type", "application/json")`
6. `validateArrayMinSize(response, "$", 1)`
7. `validateResponseBodyNotEmpty(response)`
8. `validateJsonKeyExists(response, "id")`

Use only the checks that apply to the endpoint. For example, array-size validation is for list
responses; request-vs-response value validation is for endpoints that return the submitted field.
