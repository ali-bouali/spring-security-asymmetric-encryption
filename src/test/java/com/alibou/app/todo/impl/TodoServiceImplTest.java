package com.alibou.app.todo.impl;

import com.alibou.app.category.Category;
import com.alibou.app.category.CategoryRepository;
import com.alibou.app.todo.Todo;
import com.alibou.app.todo.TodoMapper;
import com.alibou.app.todo.TodoRepository;
import com.alibou.app.todo.request.TodoRequest;
import com.alibou.app.todo.request.TodoUpdateRequest;
import com.alibou.app.todo.response.TodoResponse;
import com.alibou.app.user.User;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TodoServiceImpl Unit Tests")
class TodoServiceImplTest {

    @Mock
    private TodoRepository todoRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private TodoMapper todoMapper;

    @InjectMocks
    private TodoServiceImpl todoService; // this is what we want to test

    private Category testCategory;
    private Todo testTodo;
    private TodoRequest todoRequest;
    private TodoUpdateRequest todoUpdateRequest;
    private TodoResponse todoResponse;


    @BeforeEach
    void setUp() {

        final User testUser = User.builder()
                                  .id("user-123")
                                  .firstName("John")
                                  .lastName("Doe")
                                  .email("john.doe@example.com")
                                  .build();

        this.testCategory = Category.builder()
                                    .id("category-123")
                                    .name("Work")
                                    .description("Work related todos")
                                    .build();

        this.testTodo = Todo.builder()
                            .id("todo-123")
                            .title("Test Todo")
                            .description("Test Description")
                            .startDate(LocalDate.now())
                            .endDate(LocalDate.now()
                                              .plusDays(1))
                            .startTime(LocalTime.of(9, 0))
                            .endTime(LocalTime.of(17, 0))
                            .done(false)
                            .user(testUser)
                            .category(this.testCategory)
                            .build();

        this.todoRequest = TodoRequest.builder()
                                      .title("New Todo")
                                      .description("New Description")
                                      .startDate(LocalDate.now())
                                      .endDate(LocalDate.now()
                                                        .plusDays(1))
                                      .startTime(LocalTime.of(10, 0))
                                      .endTime(LocalTime.of(18, 0))
                                      .categoryId("category-123")
                                      .build();

        this.todoUpdateRequest = TodoUpdateRequest.builder()
                                                  .title("Updated Todo")
                                                  .description("Updated Description")
                                                  .startDate(LocalDate.now())
                                                  .endDate(LocalDate.now()
                                                                    .plusDays(2))
                                                  .startTime(LocalTime.of(11, 0))
                                                  .endTime(LocalTime.of(19, 0))
                                                  .categoryId("category-123")
                                                  .build();

        this.todoResponse = TodoResponse.builder()
                                        .id("todo-1234")
                                        .title("Test tpdo")
                                        .description("Test Description")
                                        .startDate(LocalDate.now())
                                        .endDate(LocalDate.now()
                                                          .plusDays(1))
                                        .startTime(LocalTime.of(9, 0))
                                        .endTime(LocalTime.of(17, 0))
                                        .done(false)
                                        .build();
    }

    @Nested
    @DisplayName("Create Todo Tests")
    class CreateTodoTests {

        @Test
        @DisplayName("Should create todo successfully when valid valid request and category exists")
        void shouldCreateTodoSuccessfully() {
            // Given
            final String userId = "user-123";
            when(TodoServiceImplTest.this.categoryRepository.findByIdAndUserId(TodoServiceImplTest.this.todoRequest.getCategoryId(), userId))
                    .thenReturn(Optional.of(TodoServiceImplTest.this.testCategory));
            when(TodoServiceImplTest.this.todoMapper.toTodo(TodoServiceImplTest.this.todoRequest)).thenReturn(TodoServiceImplTest.this.testTodo);
            when(TodoServiceImplTest.this.todoRepository.save(any(Todo.class))).thenReturn(TodoServiceImplTest.this.testTodo);


            // When
            final String result = TodoServiceImplTest.this.todoService.createTodo(TodoServiceImplTest.this.todoRequest, userId);

            // Then
            assertNotNull(result);
            assertEquals("todo-123", result);
            verify(TodoServiceImplTest.this.categoryRepository, times(1)).findByIdAndUserId(TodoServiceImplTest.this.todoRequest.getCategoryId(),
                                                                                            userId);
            verify(TodoServiceImplTest.this.todoMapper, times(1)).toTodo(TodoServiceImplTest.this.todoRequest);
            verify(TodoServiceImplTest.this.todoRepository, times(1)).save(TodoServiceImplTest.this.testTodo);

            // Verify that category is set on todo
            verify(TodoServiceImplTest.this.todoRepository).save(argThat(todo -> todo.getCategory() != null && todo.getCategory()
                                                                                                                   .getId()
                                                                                                                   .equals("category-123")));

        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when category not found")
        void shouldThrowEntityNotFoundExceptionWhenCategoryNotFound() {
            // Given
            final String userId = "user-123";
            when(TodoServiceImplTest.this.categoryRepository.findByIdAndUserId(TodoServiceImplTest.this.todoRequest.getCategoryId(), userId))
                    .thenReturn(Optional.empty());

            // When & Then
            final EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> TodoServiceImplTest.this.todoService.createTodo(TodoServiceImplTest.this.todoRequest, userId)
            );

            assertEquals("No category was found for that user with id " + TodoServiceImplTest.this.todoRequest.getCategoryId(),
                         exception.getMessage());
            verify(TodoServiceImplTest.this.categoryRepository, times(1)).findByIdAndUserId(TodoServiceImplTest.this.todoRequest.getCategoryId(),
                                                                                            userId);
            verifyNoInteractions(TodoServiceImplTest.this.todoMapper);
            verifyNoInteractions(TodoServiceImplTest.this.todoRepository);

        }

        @Test
        @DisplayName("Should Handle null cateforyId in request")
        void shouldHandleNullCatIdInRequest() {
            // Given
            final String userId = "user-123";
            TodoServiceImplTest.this.todoRequest.setCategoryId(null);
            when(TodoServiceImplTest.this.categoryRepository.findByIdAndUserId(null, userId))
                    .thenReturn(Optional.empty());

            // When & Then
            final EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> TodoServiceImplTest.this.todoService.createTodo(TodoServiceImplTest.this.todoRequest, userId)
            );

            assertNotNull(exception);
            assertEquals("No category was found for that user with id " + TodoServiceImplTest.this.todoRequest.getCategoryId(),
                         exception.getMessage());
            verify(TodoServiceImplTest.this.categoryRepository, times(1)).findByIdAndUserId(TodoServiceImplTest.this.todoRequest.getCategoryId(),
                                                                                            userId);
            verifyNoInteractions(TodoServiceImplTest.this.todoMapper);
            verifyNoInteractions(TodoServiceImplTest.this.todoRepository);

        }

    }

    @Nested
    @DisplayName("Update Todo Tests")
    class UpdateTodoTests {

        @Test
        @DisplayName("Should update successfully a Todo when todo and category exist")
        void shouldSuccessfullyUpdateTodo() {
            // Given
            final String userId = "user-123";
            final String todoId = "todo-123";
            when(TodoServiceImplTest.this.todoRepository.findById(todoId)).thenReturn(Optional.of(TodoServiceImplTest.this.testTodo));
            when(TodoServiceImplTest.this.categoryRepository.findByIdAndUserId(TodoServiceImplTest.this.testTodo.getCategory()
                                                                                                                .getId(), userId))
                    .thenReturn(Optional.of(TodoServiceImplTest.this.testCategory));
            when(TodoServiceImplTest.this.todoRepository.save(any(Todo.class))).thenReturn(TodoServiceImplTest.this.testTodo);

            // When
            TodoServiceImplTest.this.todoService.updateTodo(TodoServiceImplTest.this.todoUpdateRequest, todoId, userId);

            // Then
            verify(TodoServiceImplTest.this.todoRepository, times(1)).findById(todoId);
            verify(TodoServiceImplTest.this.categoryRepository).findByIdAndUserId(TodoServiceImplTest.this.testTodo.getCategory()
                                                                                                                   .getId(), userId);
            verify(TodoServiceImplTest.this.todoMapper).mergerTodo(TodoServiceImplTest.this.testTodo, TodoServiceImplTest.this.todoUpdateRequest);
            verify(TodoServiceImplTest.this.todoRepository).save(TodoServiceImplTest.this.testTodo);

            // Verify category is set
            assertEquals(TodoServiceImplTest.this.testCategory, TodoServiceImplTest.this.testTodo.getCategory());

        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when Todo not found")
        void shouldThrowEntityNotFoundExceptionWhenTodoNotFound() {
            final String userId = "user-123";
            final String todoId = "todo-123";
            when(TodoServiceImplTest.this.todoRepository.findById(todoId)).thenReturn(Optional.empty());

            final EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> TodoServiceImplTest.this.todoService.updateTodo(TodoServiceImplTest.this.todoUpdateRequest, todoId, userId)
            );

            assertEquals("Todo not found with id: " + todoId, exception.getMessage());
            verify(TodoServiceImplTest.this.todoRepository, times(1)).findById(todoId);
            verifyNoInteractions(TodoServiceImplTest.this.categoryRepository);
            verifyNoInteractions(TodoServiceImplTest.this.todoMapper);
            verify(TodoServiceImplTest.this.todoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when Category not found")
        void shouldThrowEntityNotFoundExceptionWhenCategoryNotFound() {
            final String userId = "user-123";
            final String todoId = "todo-123";
            when(TodoServiceImplTest.this.todoRepository.findById(todoId)).thenReturn(Optional.of(TodoServiceImplTest.this.testTodo));
            when(TodoServiceImplTest.this.categoryRepository.findByIdAndUserId(TodoServiceImplTest.this.todoUpdateRequest.getCategoryId(),
                                                                               userId)).thenReturn(Optional.empty());

            final EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> TodoServiceImplTest.this.todoService.updateTodo(TodoServiceImplTest.this.todoUpdateRequest, todoId, userId)
            );

            assertEquals("No category was found for that user with id " + TodoServiceImplTest.this.todoUpdateRequest.getCategoryId(),
                         exception.getMessage());
            verify(TodoServiceImplTest.this.todoRepository, times(1)).findById(todoId);
            verify(TodoServiceImplTest.this.categoryRepository).findByIdAndUserId(TodoServiceImplTest.this.todoUpdateRequest.getCategoryId(), userId);
            verifyNoInteractions(TodoServiceImplTest.this.todoMapper);
            verify(TodoServiceImplTest.this.todoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Find Todo By Id test")
    class FindTodoByIdTests {

        @Test
        @DisplayName("Should return todo response when todo exists")
        void shouldReturnTodoResponse() {
            // Given
            final String todoId = "todo-123";
            when(TodoServiceImplTest.this.todoRepository.findById(todoId))
                    .thenReturn(Optional.of(TodoServiceImplTest.this.testTodo));
            when(TodoServiceImplTest.this.todoMapper.toTodoResponse(TodoServiceImplTest.this.testTodo))
                    .thenReturn(TodoServiceImplTest.this.todoResponse);

            // When
            final TodoResponse result = TodoServiceImplTest.this.todoService.findTodoById(todoId);

            // Then
            assertNotNull(result);
            assertEquals(TodoServiceImplTest.this.todoResponse, result);
            verify(TodoServiceImplTest.this.todoRepository).findById(todoId);
            verify(TodoServiceImplTest.this.todoMapper).toTodoResponse(TodoServiceImplTest.this.testTodo);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when todo not found")
        void shouldThrowEntityNotFoundExceptionWhenTodoNotFound() {
            // Given
            final String todoId = "non-existing-todo";
            when(TodoServiceImplTest.this.todoRepository.findById(todoId))
                    .thenReturn(Optional.empty());

            // When & Then
            final EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> TodoServiceImplTest.this.todoService.findTodoById(todoId)
            );

            assertEquals("No todo found with id " + todoId, exception.getMessage());
            verify(TodoServiceImplTest.this.todoRepository).findById(todoId);
            verifyNoInteractions(TodoServiceImplTest.this.todoMapper);

        }

        @Test
        @DisplayName("Should handle null todo ID")
        void shouldHandleNullId() {
            // Given
            when(TodoServiceImplTest.this.todoRepository.findById(null))
                    .thenReturn(Optional.empty());

            // When & Then
            final EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> TodoServiceImplTest.this.todoService.findTodoById(null)
            );

            assertEquals("No todo found with id null", exception.getMessage());
            verify(TodoServiceImplTest.this.todoRepository).findById(null);
            verifyNoInteractions(TodoServiceImplTest.this.todoMapper);

        }


    }
}