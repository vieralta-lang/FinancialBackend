package org.acme.category;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
class CategoryResourceTest {

    @Test
    void testCreateCategory() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Food",
                            "type": "EXPENSE",
                            "icon": "utensils",
                            "color": "#FF5733"
                        }
                        """)
                .when().post("/categories")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", is("Food"))
                .body("type", is("EXPENSE"));
    }

    @Test
    void testFilterByType() {
        // Create an income category first
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Salary",
                            "type": "INCOME"
                        }
                        """)
                .when().post("/categories")
                .then()
                .statusCode(201);

        given()
                .queryParam("type", "INCOME")
                .when().get("/categories")
                .then()
                .statusCode(200);
    }
}
