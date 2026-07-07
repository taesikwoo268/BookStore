package com.bookstore.mapper;

import com.bookstore.dto.response.AuthorSummaryResponse;
import com.bookstore.model.Author;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AuthorMapper {

    AuthorSummaryResponse toSummaryResponse(Author author);

    List<AuthorSummaryResponse> toSummaryList(List<Author> authors);

}