package org.stahl.books.entity;

import io.norberg.automatter.AutoMatter;

import java.util.List;

@AutoMatter
public interface BookEntity {
    String id();
    String name();
    List<String> authors();
}
