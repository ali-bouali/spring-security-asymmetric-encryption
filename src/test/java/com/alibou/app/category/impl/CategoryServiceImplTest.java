package com.alibou.app.category.impl;

import com.alibou.app.category.Category;
import com.alibou.app.category.CategoryRepository;
import com.alibou.app.category.request.CategoryRequest;
import com.alibou.app.category.request.CategoryUpdateRequest;
import com.alibou.app.category.response.CategoryResponse;
import com.alibou.app.exception.BusinessException;
import com.alibou.app.todo.Todo;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl Unit Tests")
class CategoryServiceImplTest {
    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private CategoryRequest testCategoryRequest;
    private Category testCategory;
    private CategoryUpdateRequest testCategoryUpdateRequest;
    private CategoryResponse testCategoryResponse;

    @BeforeEach
    void setUp() {
        this.testCategoryRequest = CategoryRequest.builder()
                .name("CategoryReq ID")
                .description("CategoryReq Desc.")
                .build();

        this.testCategory = Category.builder()
                .id("Category ID")
                .name("Category Name")
                .description("Category Desc.")
                .build();

        this.testCategoryUpdateRequest = CategoryUpdateRequest.builder()
                .name("CategoryUpdateReq ID")
                .description("CategoryUpdateReq Desc.")
                .build();

        this.testCategoryResponse = CategoryResponse.builder()
                .id("CategoryRes ID")
                .name("CategoryRes Name")
                .description("CategoryRes Desc.")
                .build();
    }

    @Nested
    @DisplayName("Create Category Tests")
    class CreateCategoryTests {
        @Test
        @DisplayName("Should Create Category Successfully")
        void createCategorySuccessfully() {
            //Given
            String userId = "User ID";
            when(categoryRepository.findByNameAndUser(testCategoryRequest.getName(), userId))
                    .thenReturn(false);
            when(categoryMapper.toCategory(testCategoryRequest))
                    .thenReturn(testCategory);
            when(categoryRepository.save(testCategory))
                    .thenReturn(testCategory);

            // When
            final String resultId = categoryService.createCategory(testCategoryRequest, userId);

            // Then
            assertNotNull(resultId);
            assertEquals(testCategory.getId(), resultId);
            verify(categoryRepository, times(1)).findByNameAndUser(testCategoryRequest.getName(), userId);
            verify(categoryMapper, times(1)).toCategory(testCategoryRequest);
            verify(categoryRepository, times(1)).save(testCategory);
        }

        @Test
        @DisplayName("Should Throw BusinessException When Category Name Already Exists For this User")
        void createCategoryFailedAndThrowBusinessException() {
            // Given
            String userId = "User ID";
            when(categoryRepository.findByNameAndUser(testCategoryRequest.getName(), userId))
                    .thenReturn(true);

            // When
            final BusinessException exception =
                    assertThrows(
                            BusinessException.class,
                            () -> categoryService.createCategory(testCategoryRequest, userId)
                    );

            // Then
            assertEquals("CATEGORY_ALREADY_EXISTS_FOR_USER", exception.getErrorCode().name());
            verify(categoryRepository, times(1)).findByNameAndUser(testCategoryRequest.getName(), userId);
            verify(categoryRepository, never()).save(any());
            verifyNoMoreInteractions(categoryMapper);
        }
    }

    @Nested
    @DisplayName("Update Category Tests")
    class UpdateCategoryTests {
        @Test
        @DisplayName("Should Not Find The Required Category To Update And Throws EntityNotFoundException")
        void couldNotFindCategoryToUpdateAndThrowEntityNotFoundException() {
            String categoryId = "Category Non-Exist ID";
            String userId = "User ID";
            when(categoryRepository.findById(categoryId))
                    .thenReturn(Optional.empty());

            final EntityNotFoundException exception =
                    assertThrows(
                            EntityNotFoundException.class,
                            () -> categoryService.updateCategory(testCategoryUpdateRequest, categoryId, userId)
                    );

            assertEquals("No category found with id: " + categoryId, exception.getMessage());
            verify(categoryRepository, times(1)).findById(categoryId);
            verifyNoMoreInteractions(categoryMapper);
            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should Throw BusinessException Because New Category Name Already Exists For this User")
        void updateCategoryFailedBecauseOfDuplicateForThisUser() {
            String categoryId = testCategory.getId();
            String userId = "User ID";
            when(categoryRepository.findById(categoryId))
                    .thenReturn(Optional.of(testCategory));
            when(categoryRepository.findByNameAndUser(testCategoryUpdateRequest.getName(), userId))
                    .thenReturn(true);

            final BusinessException exception =
                    assertThrows(
                            BusinessException.class,
                            () -> categoryService.updateCategory(testCategoryUpdateRequest, categoryId, userId)
                    );

            assertEquals("CATEGORY_ALREADY_EXISTS_FOR_USER", exception.getErrorCode().name());
            verify(categoryRepository, times(1)).findById(categoryId);
            verify(categoryRepository, times(1)).findByNameAndUser(testCategoryUpdateRequest.getName(), userId);
            verifyNoMoreInteractions(categoryMapper);
            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should Update Category Successfully With The New Unique Name")
        void updateCategorySuccessfully() {
            String categoryId = testCategory.getId();
            String userId = "User ID";
            when(categoryRepository.findById(categoryId))
                    .thenReturn(Optional.of(testCategory));
            when(categoryRepository.findByNameAndUser(testCategoryUpdateRequest.getName(), userId))
                    .thenReturn(false);

            categoryService.updateCategory(testCategoryUpdateRequest, categoryId, userId);

            verify(categoryRepository, times(1)).findById(categoryId);
            verify(categoryRepository, times(1)).findByNameAndUser(testCategoryUpdateRequest.getName(), userId);
            verify(categoryMapper, times(1)).mergeCategory(testCategory, testCategoryUpdateRequest);
            verify(categoryRepository, times(1)).save(testCategory);
        }
    }

    @Nested
    @DisplayName("Find All By Owner Tests")
    class FindAllByOwnerTests {
        @Test
        @DisplayName("Should Find And Return All The Categories Of the Owner Of Them")
        void findAllCategoriesByOwner() {
            List<Category> categories = List.of(testCategory, testCategory);
            String userId = "User ID";
            when(categoryRepository.findAllByUserId(userId))
                    .thenReturn(categories);
            when(categoryMapper.toCategoryResponse(testCategory))
                    .thenReturn(testCategoryResponse);

            List<CategoryResponse> result = categoryService.findAllByOwner(userId);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(testCategoryResponse, result.get(0));
            verify(categoryRepository, times(1)).findAllByUserId(userId);
            verify(categoryMapper, times(2)).toCategoryResponse(testCategory);
        }

        @Test
        @DisplayName("Should Not Find The Categories Of the Owner And Return Empty List")
        void noCategoriesFoundForTheOwner() {
            String userId = "User ID";
            when(categoryRepository.findAllByUserId(userId))
                    .thenReturn(List.of());

            List<CategoryResponse> result = categoryService.findAllByOwner(userId);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(categoryRepository, times(1)).findAllByUserId(userId);
            verifyNoMoreInteractions(categoryMapper);
        }

        @Test
        @DisplayName("Should Map todoCount Correctly")
        void mapTodoCountCorrectly() {
            String userId = "User ID";
            Category categoryWithTodos = Category.builder()
                    .id("1")
                    .name("Cat")
                    .description("Desc.")
                    .todos(List.of(new Todo(), new Todo()))
                    .build();
            CategoryResponse responseOfCategoryWithTodos = CategoryResponse.builder()
                    .id("1")
                    .name("Cat.")
                    .description("Desc.")
                    .todoCount(2)
                    .build();
            when(categoryRepository.findAllByUserId(userId))
                    .thenReturn(List.of(categoryWithTodos));
            when(categoryMapper.toCategoryResponse(categoryWithTodos))
                    .thenReturn(responseOfCategoryWithTodos);

            List<CategoryResponse> result = categoryService.findAllByOwner(userId);

            assertEquals(1, result.size());
            assertEquals(2, result.get(0).getTodoCount());
            verify(categoryRepository).findAllByUserId(userId);
            verify(categoryMapper).toCategoryResponse(categoryWithTodos);
            verifyNoMoreInteractions(categoryRepository, categoryMapper);
        }
    }

    @Nested
    @DisplayName("Find Category By Id Tests")
    class FindCategoryByIdTests {
        @Test
        @DisplayName("Should Find The Category By Id")
        void findCategoryByIdSuccessfully() {
            String categoryId = testCategory.getId();
            when(categoryRepository.findById(categoryId))
                    .thenReturn(Optional.of(testCategory));
            when(categoryMapper.toCategoryResponse(testCategory))
                    .thenReturn(testCategoryResponse);

            final CategoryResponse result = categoryService.findCategoryById(categoryId);

            assertNotNull(result);
            assertEquals(testCategoryResponse, result);
            verify(categoryRepository, times(1)).findById(categoryId);
            verify(categoryMapper, times(1)).toCategoryResponse(testCategory);
        }

        @Test
        @DisplayName("Should Not Find The Category By Id")
        void couldNotFindCategoryByIdAndThrowEntityNotFoundException() {
            String categoryId = "Category Non-Exist ID";
            when(categoryRepository.findById(categoryId))
                    .thenReturn(Optional.empty());

            final EntityNotFoundException exception =
                    assertThrows(
                            EntityNotFoundException.class,
                            () -> categoryService.findCategoryById(categoryId)
                    );

            assertEquals("No category found with id: " + categoryId, exception.getMessage());
            verify(categoryRepository, times(1)).findById(categoryId);
            verifyNoMoreInteractions(categoryMapper);
        }
    }

    @Nested
    @DisplayName("Delete Category By Id Tests")
    @Disabled("deleteCategoryById Method Not Implemented Yet")
    class DeleteCategoryByIdTests {}

    @Nested
    @DisplayName("Check Category Unicity For User Tests")
    class CheckCategoryUnicityForUserTests {
        @Test
        @DisplayName("Should Be Unique Category Name For His User")
        void categoryNameIsUniqueForUser() {
            String categoryName = "Unique Category Name";
            String userId = "User ID";
            Category categoryToSave = Category.builder()
                    .id("Category ID")
                    .name(categoryName)
                    .description("Desc")
                    .build();
            when(categoryRepository.findByNameAndUser(categoryName, userId))
                    .thenReturn(false);
            when(categoryMapper.toCategory(any(CategoryRequest.class))).
                    thenReturn(categoryToSave);
            when(categoryRepository.save(any(Category.class)))
                    .thenReturn(categoryToSave);

            assertDoesNotThrow(
                    () -> categoryService.createCategory(
                            CategoryRequest.builder().name(categoryName).description("Desc.").build(), userId
                    )
            );

            verify(categoryRepository, times(1)).findByNameAndUser(categoryName, userId);
            verify(categoryMapper, times(1)).toCategory(any(CategoryRequest.class));
            verify(categoryRepository, times(1)).save(any(Category.class));
        }

        @Test
        @DisplayName("Should Not Be Unique And Throw BusinessException")
        void categoryNameIsDuplicateAndThrowsBusinessException() {
            String categoryName = "Duplicate Category Name";
            String userId = "User ID";
            when(categoryRepository.findByNameAndUser(categoryName, userId))
                    .thenReturn(true);

            final BusinessException exception =
                    assertThrows(
                            BusinessException.class,
                            () -> categoryService.createCategory(CategoryRequest.builder().name(categoryName).description("Desc.").build(), userId)
                    );

            assertEquals("CATEGORY_ALREADY_EXISTS_FOR_USER", exception.getErrorCode().name());
            verify(categoryRepository, times(1)).findByNameAndUser(categoryName, userId);
        }
    }
}