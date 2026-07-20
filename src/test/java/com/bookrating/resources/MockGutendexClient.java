package com.bookrating.resources;

import com.bookrating.gateway.gutendex.GutendexAuthor;
import com.bookrating.gateway.gutendex.GutendexBook;
import com.bookrating.gateway.gutendex.GutendexClient;
import com.bookrating.gateway.gutendex.GutendexSearchResponse;
import io.quarkus.test.Mock;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mock
@ApplicationScoped
@RestClient
public class MockGutendexClient implements GutendexClient {

    private static final Map<Long, GutendexBook> BOOKS = new ConcurrentHashMap<>();

    static {
        BOOKS.put(84L, new GutendexBook(84, "Frankenstein",
                List.of(new GutendexAuthor("Shelley, Mary Wollstonecraft", 1797, 1851)),
                List.of("en"), 100000));
        BOOKS.put(1342L, new GutendexBook(1342, "Pride and Prejudice",
                List.of(new GutendexAuthor("Austen, Jane", 1775, 1817)),
                List.of("en"), 80000));
        BOOKS.put(11L, new GutendexBook(11, "Alice's Adventures in Wonderland",
                List.of(new GutendexAuthor("Carroll, Lewis", 1832, 1898)),
                List.of("en"), 50000));
    }

    @Override
    public GutendexSearchResponse searchBooks(String searchTerm, Integer page) {
        List<GutendexBook> results = BOOKS.values().stream()
                .filter(b -> b.title().toLowerCase().contains(searchTerm.toLowerCase()))
                .toList();
        return new GutendexSearchResponse(results.size(), null, null, results);
    }

    @Override
    public GutendexBook getBook(long id) {
        GutendexBook book = BOOKS.get(id);
        if (book == null) {
            throw new jakarta.ws.rs.WebApplicationException("Not Found", 404);
        }
        return book;
    }
}
