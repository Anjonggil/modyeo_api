package com.co.kr.modyeo.api.category.service.impl;

import com.co.kr.modyeo.api.category.domain.dto.request.CategoryRequest;
import com.co.kr.modyeo.api.category.domain.dto.response.CategoryDetail;
import com.co.kr.modyeo.api.category.domain.dto.response.CategoryResponse;
import com.co.kr.modyeo.api.category.domain.dto.search.CategorySearch;
import com.co.kr.modyeo.api.category.domain.entity.Category;
import com.co.kr.modyeo.api.category.repository.CategoryRepository;
import com.co.kr.modyeo.api.category.service.CategoryService;
import com.co.kr.modyeo.common.exception.ApiException;
import com.co.kr.modyeo.common.exception.code.CategoryErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Category createCategory(CategoryRequest categoryRequest) {
        overlapCategoryCheck(categoryRequest);
        Category category = CategoryRequest.toEntity(categoryRequest);
        return categoryRepository.save(category);
    }

    @Override
    public List<CategoryResponse> getCategories(CategorySearch categorySearch) {
        List<Category> categoryList = categoryRepository.searchCategory(categorySearch);
        return categoryList.stream().map(CategoryResponse::toRes).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Category updateCategory(CategoryRequest categoryUpdateRequest) {
        overlapCategoryCheck(categoryUpdateRequest);
        Category category = categoryRepository.findById(categoryUpdateRequest.getId())
                .orElseThrow(() -> ApiException.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .errorMessage(CategoryErrorCode.NOT_FOUND_CATEGORY.getMessage())
                        .errorCode(CategoryErrorCode.NOT_FOUND_CATEGORY.getCode())
                        .build());

        category.changeCategory(categoryUpdateRequest.getName());
        return category;
    }

    @Override
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> ApiException.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .errorMessage(CategoryErrorCode.NOT_FOUND_CATEGORY.getMessage())
                        .errorCode(CategoryErrorCode.NOT_FOUND_CATEGORY.getCode())
                        .build());

        categoryRepository.delete(category);
    }

    @Override
    public CategoryDetail getCategory(Long categoryId) {
        return CategoryDetail.toDto(categoryRepository.findById(categoryId).orElseThrow(
                ()-> ApiException.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .errorMessage(CategoryErrorCode.NOT_FOUND_CATEGORY.getMessage())
                        .errorCode(CategoryErrorCode.NOT_FOUND_CATEGORY.getCode())
                        .build()));
    }

    private void overlapCategoryCheck(CategoryRequest categoryRequest){
        Category findCategory = categoryRepository.findByName(categoryRequest.getName());
        if (findCategory != null){
            throw ApiException.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .errorCode(CategoryErrorCode.OVERLAP_CATEGORY.getCode())
                    .errorMessage(CategoryErrorCode.OVERLAP_CATEGORY.getMessage())
                    .build();

        }
    }
}
