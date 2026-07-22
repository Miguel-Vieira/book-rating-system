package com.bookrating.gateway.gutendex;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/books/")
@RegisterRestClient
public interface GutendexClient {

    @GET
    @Timeout(5000)
    @Retry(maxRetries = 2, delay = 500)
    GutendexSearchResponse searchBooks(@QueryParam("search") String searchTerm,
                                       @QueryParam("page") Integer page);

    @GET
    @Path("/{id}/")
    @Timeout(5000)
    @Retry(maxRetries = 2, delay = 500)
    GutendexBook getBook(@PathParam("id") long id);
}
