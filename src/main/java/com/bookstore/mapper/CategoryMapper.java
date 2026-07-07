package com.bookstore.mapper;

import com.bookstore.dto.response.CategoryDetailResponse;
import com.bookstore.dto.response.CategorySummaryResponse;
import com.bookstore.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CategoryMapper {

    CategorySummaryResponse toSummaryResponse(Category category);

    CategoryDetailResponse toDetailResponse(Category category);

    List<CategorySummaryResponse> toSummaryList(List<Category> categories);

}