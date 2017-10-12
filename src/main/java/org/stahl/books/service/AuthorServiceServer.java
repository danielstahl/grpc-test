package org.stahl.books.service;

import com.google.common.collect.ImmutableMap;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.stahl.books.entity.AuthorEntity;
import org.stahl.books.entity.AuthorEntityBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AuthorServiceServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(8081)
                .addService(new AuthorServiceImpl())
                .build();
        System.out.println("Starting author server on port 8081");
        server.start();
        System.out.println("Started author server");
        server.awaitTermination();
    }

    public static class AuthorServiceImpl extends AuthorsServiceGrpc.AuthorsServiceImplBase {

        private static Map<String, AuthorEntity> authorRepository = ImmutableMap.<String, AuthorEntity>builder()
                .put("1", new AuthorEntityBuilder().id("1").name("First author").build())
                .put("2", new AuthorEntityBuilder().id("2").name("Second author").build())
                .build();

        @Override
        public void getAuthors(AuthorsRequest request, StreamObserver<AuthorsResponse> responseObserver) {

            List<Optional<AuthorEntity>> optionalAuthors =
                    request.getIdsList().stream()
                            .map(id -> Optional.ofNullable(authorRepository.get(id)))
                            .collect(Collectors.toList());

            if(optionalAuthors.stream().anyMatch(authorEntity -> !authorEntity.isPresent())) {

                responseObserver.onError(
                        Status.NOT_FOUND
                                .withDescription("Not all authors found")
                                .asRuntimeException());
            } else {
                List<Author> authors = optionalAuthors.stream()
                        .map(Optional::get)
                        .map(AuthorServiceImpl::toAuthor)
                        .collect(Collectors.toList());
                AuthorsResponse response = AuthorsResponse
                        .newBuilder()
                        .addAllAuthors(authors)
                        .build();
                responseObserver.onNext(response);
            }

            responseObserver.onCompleted();
        }

        private static Author toAuthor(AuthorEntity authorEntity) {
            return Author.newBuilder()
                    .setId(authorEntity.id())
                    .setName(authorEntity.name())
                    .build();
        }
    }
}
