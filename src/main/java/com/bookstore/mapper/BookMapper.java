package com.bookstore.mapper;

import com.bookstore.dto.request.BookCreateRequest;
import com.bookstore.dto.request.BookUpdateRequest;
import com.bookstore.dto.response.BookDetailResponse;
import com.bookstore.dto.response.BookResponse;
import com.bookstore.dto.response.BookSummaryResponse;
import com.bookstore.model.Book;
import org.mapstruct.*;

import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface BookMapper {

     Book toEntity(BookCreateRequest request);

     void updateEntity(@MappingTarget Book existingBook, BookUpdateRequest request);

     BookResponse toResponse(Book book);

     BookSummaryResponse toSummaryResponse(Book book);

     BookDetailResponse toDetailResponse(Book book);

     List<BookSummaryResponse> toSummaryList(List<Book> books);

}