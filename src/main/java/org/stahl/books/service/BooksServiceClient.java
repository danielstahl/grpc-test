package org.stahl.books.service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BooksServiceClient {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 8080)
                .usePlaintext(true)
                .build();

        BooksServiceGrpc.BooksServiceBlockingStub stub =
                BooksServiceGrpc.newBlockingStub(channel);

        BookRequest bookRequest = BookRequest.newBuilder().setId("2").build();

        BookResponse bookResponse = stub.getBook(bookRequest);

        long start = System.currentTimeMillis();
        bookResponse = stub.getBook(bookRequest);
        long end = System.currentTimeMillis();

        System.out.println(
                "\nBookRequest\n" + bookRequest +
                "\nBookResponse\n" + bookResponse +
                "\n and took " + (end - start) + " millis to run");

    }

}
