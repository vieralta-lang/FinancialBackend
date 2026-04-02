package org.acme.crypto;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
class CryptoResourceTest {

    @Test
    void testCreateAndListHoldings() {
        // Create a BTC holding
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "coinId": "bitcoin",
                            "symbol": "BTC",
                            "quantity": 0.5,
                            "averagePurchasePrice": 60000.00,
                            "purchaseCurrency": "USD",
                            "notes": "My Bitcoin stack"
                        }
                        """)
                .when().post("/crypto/holdings")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("coinId", is("bitcoin"))
                .body("symbol", is("BTC"));

        // List holdings
        given()
                .when().get("/crypto/holdings")
                .then()
                .statusCode(200);
    }

    @Test
    void testGetNonExistentHolding() {
        given()
                .when().get("/crypto/holdings/99999")
                .then()
                .statusCode(404);
    }
}
