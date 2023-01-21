package com.fullcycle.admin.catalogo.application.category.create;

import com.fullcycle.admin.catalogo.IntegrationTest;
import com.fullcycle.admin.catalogo.domain.category.CategoryGateway;
import com.fullcycle.admin.catalogo.infrastructure.category.persistence.CategoryRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@IntegrationTest
public class CreateCategoryUseCaseIT {

  @Autowired
  private CreateCategoryUseCase useCase;

  @Autowired
  private CategoryRepository categoryRepository;

  @SpyBean
  private CategoryGateway categoryGateway;

  @Test
  public void givenAValidCommand_whenCallsCreateCategory_shouldReturnCategoryId() {
    final var expectedName = "Filmes";
    final var expectedDescription = "A categoria mais assistida";
    final var expectedIsActive = true;

    assertEquals(0, categoryRepository.count());

    final var aCommand =
        CreateCategoryCommand.with(expectedName, expectedDescription, expectedIsActive);

    final var actualOutput = useCase.execute(aCommand).get();

    assertNotNull(actualOutput);
    assertNotNull(actualOutput.id());

    assertEquals(1, categoryRepository.count());

    final var actualCategory =
        categoryRepository.findById(actualOutput.id()).get();

    assertEquals(expectedName, actualCategory.getName());
    assertEquals(expectedDescription, actualCategory.getDescription());
    assertEquals(expectedIsActive, actualCategory.isActive());
    assertNotNull(actualCategory.getCreatedAt());
    assertNotNull(actualCategory.getUpdatedAt());
    Assertions.assertNull(actualCategory.getDeletedAt());
  }

  @Test
  public void givenAInvalidName_whenCallsCreateCategory_thenShouldReturnDomainException() {
    final String expectedName = null;
    final var expectedDescription = "A categoria mais assistida";
    final var expectedIsActive = true;
    final var expectedErrorMessage = "'name' should not be null";
    final var expectedErrorCount = 1;

    assertEquals(0, categoryRepository.count());

    final var aCommand =
        CreateCategoryCommand.with(expectedName, expectedDescription, expectedIsActive);

    final var notification = useCase.execute(aCommand).getLeft();

    assertEquals(expectedErrorCount, notification.getErrors().size());
    assertEquals(expectedErrorMessage, notification.firstError().message());

    assertEquals(0, categoryRepository.count());

    verify(categoryGateway, times(0)).create(any());
  }

  @Test
  public void givenAValidCommandWithInactiveCategory_whenCallsCreateCategory_shouldReturnInactiveCategoryId() {
    final var expectedName = "Filmes";
    final var expectedDescription = "A categoria mais assistida";
    final var expectedIsActive = false;

    assertEquals(0, categoryRepository.count());

    final var aCommand =
        CreateCategoryCommand.with(expectedName, expectedDescription, expectedIsActive);

    final var actualOutput = useCase.execute(aCommand).get();

    assertNotNull(actualOutput);
    assertNotNull(actualOutput.id());

    assertEquals(1, categoryRepository.count());

    final var actualCategory =
        categoryRepository.findById(actualOutput.id()).get();

    assertEquals(expectedName, actualCategory.getName());
    assertEquals(expectedDescription, actualCategory.getDescription());
    assertEquals(expectedIsActive, actualCategory.isActive());
    assertNotNull(actualCategory.getCreatedAt());
    assertNotNull(actualCategory.getUpdatedAt());
    assertNotNull(actualCategory.getDeletedAt());
  }

  @Test
  public void givenAValidCommand_whenGatewayThrowsRandomException_shouldReturnAException() {
    final var expectedName = "Filmes";
    final var expectedDescription = "A categoria mais assistida";
    final var expectedIsActive = true;
    final var expectedErrorCount = 1;
    final var expectedErrorMessage = "Gateway error";

    final var aCommand =
        CreateCategoryCommand.with(expectedName, expectedDescription, expectedIsActive);

    doThrow(new IllegalStateException(expectedErrorMessage))
        .when(categoryGateway).create(any());

    final var notification = useCase.execute(aCommand).getLeft();

    assertEquals(expectedErrorCount, notification.getErrors().size());
    assertEquals(expectedErrorMessage, notification.firstError().message());
  }
}