package com.bookstore.mapper;

import com.bookstore.dto.request.BookCreateRequest;
import com.bookstore.dto.request.BookUpdateRequest;
import com.bookstore.dto.response.BookDetailResponse;
import com.bookstore.dto.response.BookResponse;
import com.bookstore.dto.response.BookSummaryResponse;
import com.bookstore.model.Book;
import com.bookstore.model.Category;
import org.mapstruct.*;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Component
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface BookMapper {

     Book toEntity(BookCreateRequest request);

     void updateEntity(@MappingTarget Book existingBook, BookUpdateRequest request);

     BookResponse toResponse(Book book);

     @Mapping(target = "authorName", source = "author.name")
     @Mapping(target = "categoryNames", expression = "java(getCategoryNames(book))")
     BookSummaryResponse toSummaryResponse(Book book);

     BookDetailResponse toDetailResponse(Book book);

     List<BookSummaryResponse> toSummaryList(List<Book> books);

     default List<String> getCategoryNames(Book book) {
          if (book == null || book.getCategories() == null || book.getCategories().isEmpty()) {
               return new ArrayList<>();  // Trả về empty list thay vì null
          }
          return book.getCategories().stream()
                  .map(Category::getName)
                  .collect(Collectors.toList());
     }
}