package com.bookrating.resources;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class ReviewResourceTest {

    @Test
    void submitValidReview() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"rating\": 4, \"review\": \"Witty and sharp, Austen at her best\"}")
                .when().post("/api/books/1342/reviews")
                .then()
                .statusCode(201)
                .body("book_id", equalTo(1342))
                .body("rating", equalTo(4))
                .body("review", equalTo("Witty and sharp, Austen at her best"))
                .body("id", notNullValue());
    }

    @Test
    void rejectRatingAbove5() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"rating\": 6, \"review\": \"Off the scale!\"}")
                .when().post("/api/books/84/reviews")
                .then()
                .statusCode(400);
    }

    @Test
    void rejectNegativeRating() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"rating\": -1, \"review\": \"This is wrong\"}")
                .when().post("/api/books/84/reviews")
                .then()
                .statusCode(400);
    }

    @Test
    void rejectEmptyReviewText() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"rating\": 3, \"review\": \"\"}")
                .when().post("/api/books/84/reviews")
                .then()
                .statusCode(400);
    }

    @Test
    void rejectMissingReviewField() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"rating\": 3}")
                .when().post("/api/books/84/reviews")
                .then()
                .statusCode(400);
    }

    @Test
    void listReviewsAfterCreation() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"rating\": 5, \"review\": \"Down the rabbit hole we go\"}")
                .when().post("/api/books/11/reviews")
                .then().statusCode(201);

        given()
                .when().get("/api/books/11/reviews")
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(1)))
                .body("[0].review", equalTo("Down the rabbit hole we go"));
    }

    @Test
    void monthlyRatingsAggregation() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"rating\": 3, \"review\": \"Decent but overhyped\"}")
                .when().post("/api/books/1342/reviews")
                .then().statusCode(201);

        given()
                .when().get("/api/books/1342/ratings/monthly")
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(1)))
                .body("[0].month", notNullValue())
                .body("[0].average_rating", notNullValue());
    }
}
