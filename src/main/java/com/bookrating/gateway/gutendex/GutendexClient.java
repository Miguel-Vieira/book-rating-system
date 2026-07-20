package com.bookrating.gateway.gutendex;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/books")
@RegisterRestClient
public interface GutendexClient {

    @GET
    GutendexSearchResponse searchBooks(@QueryParam("search") String searchTerm,
                                       @QueryParam("page") Integer page);

    @GET
    @Path("/{id}")
    GutendexBook getBook(@PathParam("id") long id);
}
