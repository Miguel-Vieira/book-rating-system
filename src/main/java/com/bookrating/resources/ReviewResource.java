package com.bookrating.resources;

import com.bookrating.service.dto.CreateReviewRequest;
import com.bookrating.service.dto.MonthlyRatingDto;
import com.bookrating.service.dto.ReviewDto;
import com.bookrating.service.ReviewService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/books/{bookId}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ReviewResource {

    @Inject
    ReviewService reviewService;

    @POST
    @Path("/reviews")
    public Response createReview(@PathParam("bookId") @Min(1) long bookId,
                                 @Valid CreateReviewRequest request) {
        ReviewDto review = reviewService.createReview(bookId, request);
        return Response.status(Response.Status.CREATED).entity(review).build();
    }

    @GET
    @Path("/reviews")
    public List<ReviewDto> getReviews(@PathParam("bookId") @Min(1) long bookId) {
        return reviewService.getReviewsForBook(bookId);
    }

    @GET
    @Path("/ratings/monthly")
    public List<MonthlyRatingDto> getMonthlyRatings(@PathParam("bookId") @Min(1) long bookId) {
        return reviewService.getMonthlyRatings(bookId);
    }
}
