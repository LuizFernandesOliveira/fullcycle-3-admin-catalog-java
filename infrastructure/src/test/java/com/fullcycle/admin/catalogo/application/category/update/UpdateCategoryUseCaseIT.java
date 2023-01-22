package com.fullcycle.admin.catalogo.application.category.update;

import com.fullcycle.admin.catalogo.IntegrationTest;
import com.fullcycle.admin.catalogo.domain.category.Category;
import com.fullcycle.admin.catalogo.domain.category.CategoryGateway;
import com.fullcycle.admin.catalogo.domain.exceptions.NotFoundException;
import com.fullcycle.admin.catalogo.infrastructure.category.persistence.CategoryJpaEntity;
import com.fullcycle.admin.catalogo.infrastructure.category.persistence.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@IntegrationTest
public class UpdateCategoryUseCaseIT {

  @Autowired
  private UpdateCategoryUseCase useCase;

  @Autowired
  private CategoryRepository categoryRepository;

  @SpyBean
  private CategoryGateway categoryGateway;

  @Test
  public void givenAValidCommand_whenCallsUpdateCategory_shouldReturnCategoryId() {
    final var aCategory =
        Category.newCategory("Film", null, true);

    save(aCategory);

    final var expectedName = "Filmes";
    final var expectedDescription = "A categoria mais assistida";
    final var expectedIsActive = true;
    final var expectedId = aCategory.getId();

    final var aCommand = UpdateCategoryCommand.with(
        expectedId.getValue(),
        expectedName,
        expectedDescription,
        expectedIsActive
    );

    assertEquals(1, categoryRepository.count());

    final var actualOutput = useCase.execute(aCommand).get();

    assertNotNull(actualOutput);
    assertNotNull(actualOutput.id());

    final var actualCategory =
        categoryRepository.findById(expectedId.getValue()).get();

    assertEquals(expectedName, actualCategory.getName());
    assertEquals(expectedDescription, actualCategory.getDescription());
    assertEquals(expectedIsActive, actualCategory.isActive());
    assertEquals(aCategory.getCreatedAt(), actualCategory.getCreatedAt());
    assertTrue(aCategory.getUpdatedAt().isBefore(actualCategory.getUpdatedAt()));
    assertNull(actualCategory.getDeletedAt());
  }

  @Test
  public void givenAInvalidName_whenCallsUpdateCategory_thenShouldReturnDomainException() {
    final var aCategory =
        Category.newCategory("Film", null, true);

    save(aCategory);

    final var expectedDescription = "A categoria mais assistida";
    final var expectedIsActive = true;
    final var expectedId = aCategory.getId();

    final var expectedErrorMessage = "'name' should not be null";
    final var expectedErrorCount = 1;

    final var aCommand =
        UpdateCategoryCommand.with(expectedId.getValue(), null, expectedDescription, expectedIsActive);

    final var notification = useCase.execute(aCommand).getLeft();

    assertEquals(expectedErrorCount, notification.getErrors().size());
    assertEquals(expectedErrorMessage, notification.firstError().message());

    verify(categoryGateway, times(0)).update(any());
  }

  @Test
  public void givenAValidInactivateCommand_whenCallsUpdateCategory_shouldReturnInactiveCategoryId() {
    final var aCategory =
        Category.newCategory("Film", null, true);

    save(aCategory);

    final var expectedName = "Filmes";
    final var expectedDescription = "A categoria mais assistida";
    final var expectedIsActive = false;
    final var expectedId = aCategory.getId();

    final var aCommand = UpdateCategoryCommand.with(
        expectedId.getValue(),
        expectedName,
        expectedDescription,
        expectedIsActive
    );

    assertTrue(aCategory.isActive());
    assertNull(aCategory.getDeletedAt());

    final var actualOutput = useCase.execute(aCommand).get();

    assertNotNull(actualOutput);
    assertNotNull(actualOutput.id());

    final var actualCategory =
        categoryRepository.findById(expectedId.getValue()).get();

    assertEquals(expectedName, actualCategory.getName());
    assertEquals(expectedDescription, actualCategory.getDescription());
    assertEquals(expectedIsActive, actualCategory.isActive());
    assertEquals(aCategory.getCreatedAt(), actualCategory.getCreatedAt());
    assertTrue(aCategory.getUpdatedAt().isBefore(actualCategory.getUpdatedAt()));
    assertNotNull(actualCategory.getDeletedAt());
  }

  @Test
  public void givenAValidCommand_whenGatewayThrowsRandomException_shouldReturnAException() {
    final var aCategory =
        Category.newCategory("Film", null, true);

    save(aCategory);

    final var expectedName = "Filmes";
    final var expectedDescription = "A categoria mais assistida";
    final var expectedIsActive = true;
    final var expectedId = aCategory.getId();
    final var expectedErrorCount = 1;
    final var expectedErrorMessage = "Gateway error";

    final var aCommand = UpdateCategoryCommand.with(
        expectedId.getValue(),
        expectedName,
        expectedDescription,
        expectedIsActive
    );

    doThrow(new IllegalStateException(expectedErrorMessage))
        .when(categoryGateway).update(any());

    final var notification = useCase.execute(aCommand).getLeft();

    assertEquals(expectedErrorCount, notification.getErrors().size());
    assertEquals(expectedErrorMessage, notification.firstError().message());

    final var actualCategory =
        categoryRepository.findById(expectedId.getValue()).get();

    assertEquals(aCategory.getName(), actualCategory.getName());
    assertEquals(aCategory.getDescription(), actualCategory.getDescription());
    assertEquals(aCategory.isActive(), actualCategory.isActive());
    assertEquals(aCategory.getCreatedAt(), actualCategory.getCreatedAt());
    assertEquals(aCategory.getUpdatedAt(), actualCategory.getUpdatedAt());
    assertEquals(aCategory.getDeletedAt(), actualCategory.getDeletedAt());
  }

  @Test
  public void givenACommandWithInvalidID_whenCallsUpdateCategory_shouldReturnNotFoundException() {
    final var expectedName = "Filmes";
    final var expectedDescription = "A categoria mais assistida";
    final var expectedIsActive = false;
    final var expectedId = "123";
    final var expectedErrorMessage = "Category with ID 123 was not found";

    final var aCommand = UpdateCategoryCommand.with(
        expectedId,
        expectedName,
        expectedDescription,
        expectedIsActive
    );

    final var actualException =
        assertThrows(NotFoundException.class, () -> useCase.execute(aCommand));

    assertEquals(expectedErrorMessage, actualException.getMessage());
  }

  private void save(final Category... aCategory) {
    categoryRepository.saveAllAndFlush(
        Arrays.stream(aCategory)
            .map(CategoryJpaEntity::from)
            .toList()
    );
  }
}