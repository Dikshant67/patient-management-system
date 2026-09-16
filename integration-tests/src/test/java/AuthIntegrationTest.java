import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class AuthIntegrationTest {
    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "http://localhost:4004";

    }
    // 1. Arrange : arrange data
    // 2. Act  : do some operation
    // 3. Assert : validate

    // Defining request body
    @Test
    public void shouldReturnOKWithValidToken(){


        String loginPayload = """
                {
                    "email" : "testuser@test.com",
                    "password" : "password123"
                }
                """;

        Response response = given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("token",notNullValue())
                .extract()
                .response();

        System.out.println("Generated Token : "+ response.jsonPath().getString("token"));
    }
    @Test
    public void shouldReturnUnauthorisedWithInvalidLogin(){


        String loginPayload = """
                {
                    "email" : "invalid_user@test.com",
                    "password" : "password123"
                }
                """;

                 given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401);


    }
}
