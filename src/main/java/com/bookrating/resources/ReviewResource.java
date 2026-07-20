package com.bookrating.resources;

import com.bookrating.service.dto.CreateReviewRequest;
import com.bookrating.service.dto.MonthlyRatingDto;
import com.bookrating.service.dto.ReviewDto;
import com.bookrating.service.ReviewService;
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
    public List<ReviewDto> getReviews(@PathParam("bookId") @Min(1) long bookId,
                                      @QueryParam("page") @DefaultValue("1") @Min(1) int page,
                                      @QueryParam("size") @DefaultValue("20") @Min(1) int size) {
        return reviewService.getReviewsForBook(bookId, page, Math.min(size, 100));
    }

    @GET
    @Path("/ratings/monthly")
    public List<MonthlyRatingDto> getMonthlyRatings(@PathParam("bookId") @Min(1) long bookId) {
        return reviewService.getMonthlyRatings(bookId);
    }
}
