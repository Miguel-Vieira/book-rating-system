package com.bookrating.resources;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class BookResourceTest {

    @Test
    void searchByTitle() {
        given()
                .queryParam("title", "Frankenstein")
                .when().get("/api/books")
                .then()
                .statusCode(200)
                .body("totalResults", greaterThanOrEqualTo(1))
                .body("books[0].title", equalTo("Frankenstein"))
                .body("books[0].authors[0].name", equalTo("Shelley, Mary Wollstonecraft"));
    }

    @Test
    void searchRequiresTitle() {
        given().when().get("/api/books").then().statusCode(400);
    }

    @Test
    void searchRejectsBlankTitle() {
        given()
                .queryParam("title", "   ")
                .when().get("/api/books")
                .then()
                .statusCode(400);
    }

    @Test
    void bookDetailsIncludesMetadata() {
        given()
                .when().get("/api/books/84")
                .then()
                .statusCode(200)
                .body("id", equalTo(84))
                .body("title", equalTo("Frankenstein"))
                .body("authors[0].name", equalTo("Shelley, Mary Wollstonecraft"))
                .body("rating", notNullValue());
    }

    @Test
    void unknownBookReturns404() {
        given().when().get("/api/books/999999").then().statusCode(404);
    }

    @Test
    void topBooksEndpoint() {
        // seed a review first
        given()
                .contentType(ContentType.JSON)
                .body("{\"rating\": 5, \"review\": \"Genuinely unsettling, loved it\"}")
                .when().post("/api/books/84/reviews")
                .then().statusCode(201);

        given()
                .queryParam("limit", 5)
                .when().get("/api/books/top")
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(1)))
                .body("[0].book_id", equalTo(84));
    }
}
