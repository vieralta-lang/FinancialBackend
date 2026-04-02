package org.acme.account;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
class AccountResourceTest {

    @Test
    void testCreateAndListAccounts() {
        // Create an account
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Main Checking",
                            "type": "CHECKING",
                            "currency": "BRL",
                            "initialBalance": 1000.00
                        }
                        """)
                .when().post("/accounts")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", is("Main Checking"))
                .body("currentBalance", is(1000.0f));

        // List accounts
        given()
                .when().get("/accounts")
                .then()
                .statusCode(200);
    }

    @Test
    void testGetNonExistentAccount() {
        given()
                .when().get("/accounts/99999")
                .then()
                .statusCode(404);
    }
}
