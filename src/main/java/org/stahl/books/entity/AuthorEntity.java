package org.stahl.books.entity;

import io.norberg.automatter.AutoMatter;

@AutoMatter
public interface AuthorEntity {
    String id();
    String name();
}
