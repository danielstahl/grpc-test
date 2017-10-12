package org.stahl.books.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.spotify.futures.FuturesExtra;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import org.stahl.books.entity.BookEntity;
import org.stahl.books.entity.BookEntityBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BooksServiceServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(8080)
                .addService(new BooksServiceImpl())
                .build();
        System.out.println("Starting server on port 8080");
        server.start();
        System.out.println("Started server");
        server.awaitTermination();
    }

    public static class BooksServiceImpl extends BooksServiceGrpc.BooksServiceImplBase {

        private final AuthorsServiceGrpc.AuthorsServiceFutureStub authorStub;

        public BooksServiceImpl() {
            ManagedChannel authorChannel = ManagedChannelBuilder
                    .forAddress("localhost", 8081)
                    .usePlaintext(true)
                    .build();
            authorStub = AuthorsServiceGrpc.newFutureStub(authorChannel);
        }

        private static Map<String, BookEntity> bookRepository = ImmutableMap.<String, BookEntity>builder()
                .put("1", new BookEntityBuilder().id("1").name("First book").addAuthor("1").build())
                .put("2", new BookEntityBuilder().id("2").name("Second book").addAuthor("2").build())
                .build();

        @Override
        public void getBook(BookRequest request, StreamObserver<BookResponse> responseObserver) {


            Optional<ListenableFuture<BookResponse>> optionalBookResponse =
                    Optional.ofNullable(bookRepository.get(request.getId()))
                            .map(book -> fromEntities(book, getAuthors(book)));

            if(optionalBookResponse.isPresent()) {
                FuturesExtra.addSuccessCallback(optionalBookResponse.get(),
                        bookResponse -> {
                            responseObserver.onNext(bookResponse);
                            responseObserver.onCompleted();
                        });
            } else {
                responseObserver.onError(
                        Status.NOT_FOUND
                                .withDescription("Book " + request.getId() + " not found")
                                .asRuntimeException());
                responseObserver.onCompleted();
            }
        }

        private ListenableFuture<List<Author>> getAuthors(BookEntity bookEntity) {
            ListenableFuture<AuthorsResponse> futureResponse =
                    authorStub.getAuthors(AuthorsRequest.newBuilder().addAllIds(bookEntity.authors()).build());

            return Futures.transform(futureResponse,
                    AuthorsResponse::getAuthorsList,
                    MoreExecutors.directExecutor());
        }

        private static ListenableFuture<BookResponse> fromEntities(BookEntity bookEntity, ListenableFuture<List<Author>> authorsFuture) {
            return Futures.transform(authorsFuture, authors ->
                    BookResponse.newBuilder().setBook(
                    Book.newBuilder()
                            .setId(bookEntity.id())
                            .setName(bookEntity.name())
                            .addAllAuthors(authors))
                            .build(), MoreExecutors.directExecutor());
        }

    }
}

