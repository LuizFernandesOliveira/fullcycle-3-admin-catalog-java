package com.fullcycle.admin.catalogo.infrastructure.category.persistence;

import com.fullcycle.admin.catalogo.domain.category.Category;
import com.fullcycle.admin.catalogo.MySQLGatewayTest;
import org.hibernate.PropertyValueException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@MySQLGatewayTest
public class CategoryRepositoryTest {

  @Autowired
  private CategoryRepository categoryRepository;

  @Test
  public void givenAnInvalidNullName_whenCallsSave_shouldReturnError() {
    final var expectedPropertyName = "name";
    final var expectedMessage = "not-null property references a null or transient value : com.fullcycle.admin.catalogo.infrastructure.category.persistence.CategoryJpaEntity.name";

    final var aCategory = Category.newCategory("Filmes", "A categoria mais assistida", true);

    final var anEntity = CategoryJpaEntity.from(aCategory);
    anEntity.setName(null);

    final var actualException =
        assertThrows(DataIntegrityViolationException.class, () -> categoryRepository.save(anEntity));

    final var actualCause =
        assertInstanceOf(PropertyValueException.class, actualException.getCause());

    assertEquals(expectedPropertyName, actualCause.getPropertyName());
    assertEquals(expectedMessage, actualCause.getMessage());
  }

  @Test
  public void givenAnInvalidNullCreatedAt_whenCallsSave_shouldReturnError() {
    final var expectedPropertyName = "createdAt";
    final var expectedMessage = "not-null property references a null or transient value : com.fullcycle.admin.catalogo.infrastructure.category.persistence.CategoryJpaEntity.createdAt";

    final var aCategory = Category.newCategory("Filmes", "A categoria mais assistida", true);

    final var anEntity = CategoryJpaEntity.from(aCategory);
    anEntity.setCreatedAt(null);

    final var actualException =
        assertThrows(DataIntegrityViolationException.class, () -> categoryRepository.save(anEntity));

    final var actualCause =
        assertInstanceOf(PropertyValueException.class, actualException.getCause());

    assertEquals(expectedPropertyName, actualCause.getPropertyName());
    assertEquals(expectedMessage, actualCause.getMessage());
  }

  @Test
  public void givenAnInvalidNullUpdatedAt_whenCallsSave_shouldReturnError() {
    final var expectedPropertyName = "updatedAt";
    final var expectedMessage = "not-null property references a null or transient value : com.fullcycle.admin.catalogo.infrastructure.category.persistence.CategoryJpaEntity.updatedAt";

    final var aCategory = Category.newCategory("Filmes", "A categoria mais assistida", true);

    final var anEntity = CategoryJpaEntity.from(aCategory);
    anEntity.setUpdatedAt(null);

    final var actualException =
        assertThrows(DataIntegrityViolationException.class, () -> categoryRepository.save(anEntity));

    final var actualCause =
        assertInstanceOf(PropertyValueException.class, actualException.getCause());

    assertEquals(expectedPropertyName, actualCause.getPropertyName());
    assertEquals(expectedMessage, actualCause.getMessage());
  }
}
