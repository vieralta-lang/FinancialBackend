package org.acme.report;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class ReportResourceTest {

    private Long accountId;
    private Long categoryId;

    @BeforeEach
    void setup() {
        accountId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Report Test Account",
                            "type": "CHECKING",
                            "currency": "BRL",
                            "initialBalance": 10000.00
                        }
                        """)
                .when().post("/accounts")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        categoryId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Report Category",
                            "type": "EXPENSE"
                        }
                        """)
                .when().post("/categories")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        // Create some transactions
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "account": {"id": %d},
                            "category": {"id": %d},
                            "type": "EXPENSE",
                            "amount": 200.00,
                            "date": "2026-03-10",
                            "description": "Test expense"
                        }
                        """.formatted(accountId, categoryId))
                .when().post("/transactions")
                .then().statusCode(201);
    }

    @Test
    void testMonthlySummary() {
        given()
                .queryParam("from", "2026-03-01")
                .queryParam("to", "2026-03-31")
                .when().get("/reports/summary")
                .then()
                .statusCode(200)
                .body("from", is("2026-03-01"))
                .body("to", is("2026-03-31"));
    }

    @Test
    void testByCategory() {
        given()
                .queryParam("from", "2026-03-01")
                .queryParam("to", "2026-03-31")
                .queryParam("type", "EXPENSE")
                .when().get("/reports/by-category")
                .then()
                .statusCode(200);
    }

    @Test
    void testSummaryRequiresDateParams() {
        given()
                .when().get("/reports/summary")
                .then()
                .statusCode(400);
    }
}
