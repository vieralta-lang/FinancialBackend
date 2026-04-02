package org.acme.transaction;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
class TransactionResourceTest {

    private Long accountId;
    private Long categoryId;

    @BeforeEach
    void setup() {
        // Create a test account
        accountId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Test Account",
                            "type": "CHECKING",
                            "currency": "BRL",
                            "initialBalance": 5000.00
                        }
                        """)
                .when().post("/accounts")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        // Create a test category
        categoryId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Groceries",
                            "type": "EXPENSE"
                        }
                        """)
                .when().post("/categories")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    @Test
    void testCreateExpenseTransaction() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "account": {"id": %d},
                            "category": {"id": %d},
                            "type": "EXPENSE",
                            "amount": 150.00,
                            "date": "2026-03-15",
                            "description": "Weekly groceries"
                        }
                        """.formatted(accountId, categoryId))
                .when().post("/transactions")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("type", is("EXPENSE"))
                .body("amount", is(150.0f));

        // Verify account balance was updated
        given()
                .when().get("/accounts/" + accountId)
                .then()
                .statusCode(200)
                .body("currentBalance", is(4850.0f));
    }

    @Test
    void testCreateTransfer() {
        // Create a second account
        Long toAccountId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Savings Account",
                            "type": "SAVINGS",
                            "currency": "BRL",
                            "initialBalance": 1000.00
                        }
                        """)
                .when().post("/accounts")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "fromAccountId": %d,
                            "toAccountId": %d,
                            "amount": 500.00,
                            "description": "Monthly savings",
                            "date": "2026-03-15"
                        }
                        """.formatted(accountId, toAccountId))
                .when().post("/transactions/transfer")
                .then()
                .statusCode(201);

        // Check source account balance decreased
        given()
                .when().get("/accounts/" + accountId)
                .then()
                .statusCode(200)
                .body("currentBalance", is(4500.0f));

        // Check destination account balance increased
        given()
                .when().get("/accounts/" + toAccountId)
                .then()
                .statusCode(200)
                .body("currentBalance", is(1500.0f));
    }
}
