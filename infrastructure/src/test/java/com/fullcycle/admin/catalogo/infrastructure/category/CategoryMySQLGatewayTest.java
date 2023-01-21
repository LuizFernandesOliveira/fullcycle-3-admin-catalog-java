package com.fullcycle.admin.catalogo.infrastructure.category;

import com.fullcycle.admin.catalogo.MySQLGatewayTest;
import com.fullcycle.admin.catalogo.domain.category.Category;
import com.fullcycle.admin.catalogo.domain.category.CategoryID;
import com.fullcycle.admin.catalogo.domain.pagination.SearchQuery;
import com.fullcycle.admin.catalogo.infrastructure.category.persistence.CategoryJpaEntity;
import com.fullcycle.admin.catalogo.infrastructure.category.persistence.CategoryRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@MySQLGatewayTest
public class CategoryMySQLGatewayTest {

  @Autowired
  private CategoryMySQLGateway categoryGateway;

  @Autowired
  private CategoryRepository categoryRepository;

  @Test
  public void givenAValidCategory_whenCallsCreate_shouldReturnANewCategory() {
    final var expectedName = "Filmes";
    final var expectedDescription = "A categoria mais assistida";
    final var expectedIsActive = true;

    final var aCategory = Category.newCategory(expectedName, expectedDescription, expectedIsActive);

    assertEquals(0, categoryRepository.count());

    final var actualCategory = categoryGateway.create(aCategory);

    assertEquals(1, categoryRepository.count());

    assertEquals(aCategory.getId(), actualCategory.getId());
    assertEquals(expectedName, actualCategory.getName());
    assertEquals(expectedDescription, actualCategory.getDescription());
    assertEquals(expectedIsActive, actualCategory.isActive());
    assertEquals(aCategory.getCreatedAt(), actualCategory.getCreatedAt());
    assertEquals(aCategory.getUpdatedAt(), actualCategory.getUpdatedAt());
    assertEquals(aCategory.getDeletedAt(), actualCategory.getDeletedAt());
    assertNull(actualCategory.getDeletedAt());

    final var actualEntity = categoryRepository.findById(aCategory.getId().getValue()).get();

    assertEquals(aCategory.getId().getValue(), actualEntity.getId());
    assertEquals(expectedName, actualEntity.getName());
    assertEquals(expectedDescription, actualEntity.getDescription());
    assertEquals(expectedIsActive, actualEntity.isActive());
    assertEquals(aCategory.getCreatedAt(), actualEntity.getCreatedAt());
    assertEquals(aCategory.getUpdatedAt(), actualEntity.getUpdatedAt());
    assertEquals(aCategory.getDeletedAt(), actualEntity.getDeletedAt());
    assertNull(actualEntity.getDeletedAt());
  }

  @Test
  public void givenAValidCategory_whenCallsUpdate_shouldReturnCategoryUpdated() {
    final var expectedName = "Filmes";
    final var expectedDescription = "A categoria mais assistida";
    final var expectedIsActive = true;

    final var aCategory = Category.newCategory("Film", null, expectedIsActive);

    assertEquals(0, categoryRepository.count());

    categoryRepository.saveAndFlush(CategoryJpaEntity.from(aCategory));

    assertEquals(1, categoryRepository.count());

    final var actualInvalidEntity = categoryRepository.findById(aCategory.getId().getValue()).get();

    assertEquals("Film", actualInvalidEntity.getName());
    assertNull(actualInvalidEntity.getDescription());
    assertEquals(expectedIsActive, actualInvalidEntity.isActive());

    final var aUpdatedCategory = aCategory.clone()
        .update(expectedName, expectedDescription, expectedIsActive);

    final var actualCategory = categoryGateway.update(aUpdatedCategory);

    assertEquals(1, categoryRepository.count());

    assertEquals(aCategory.getId(), actualCategory.getId());
    assertEquals(expectedName, actualCategory.getName());
    assertEquals(expectedDescription, actualCategory.getDescription());
    assertEquals(expectedIsActive, actualCategory.isActive());
    assertEquals(aCategory.getCreatedAt(), actualCategory.getCreatedAt());
    Assertions.assertTrue(aCategory.getUpdatedAt().isBefore(actualCategory.getUpdatedAt()));
    assertEquals(aCategory.getDeletedAt(), actualCategory.getDeletedAt());
    assertNull(actualCategory.getDeletedAt());

    final var actualEntity = categoryRepository.findById(aCategory.getId().getValue()).get();

    assertEquals(aCategory.getId().getValue(), actualEntity.getId());
    assertEquals(expectedName, actualEntity.getName());
    assertEquals(expectedDescription, actualEntity.getDescription());
    assertEquals(expectedIsActive, actualEntity.isActive());
    assertEquals(aCategory.getCreatedAt(), actualEntity.getCreatedAt());
    Assertions.assertTrue(aCategory.getUpdatedAt().isBefore(actualCategory.getUpdatedAt()));
    assertEquals(aCategory.getDeletedAt(), actualEntity.getDeletedAt());
    assertNull(actualEntity.getDeletedAt());
  }

  @Test
  public void givenAPrePersistedCategoryAndValidCategoryId_whenTryToDeleteIt_shouldDeleteCategory() {
    final var aCategory = Category.newCategory("Filmes", null, true);

    assertEquals(0, categoryRepository.count());

    categoryRepository.saveAndFlush(CategoryJpaEntity.from(aCategory));

    assertEquals(1, categoryRepository.count());

    categoryGateway.deleteById(aCategory.getId());

    assertEquals(0, categoryRepository.count());
  }

  @Test
  public void givenInvalidCategoryId_whenTryToDeleteIt_shouldDeleteCategory() {
    assertEquals(0, categoryRepository.count());

    categoryGateway.deleteById(CategoryID.from("invalid"));

    assertEquals(0, categoryRepository.count());
  }

  @Test
  public void givenAPrePersistedCategoryAndValidCategoryId_whenCallsFindById_shouldReturnCategory() {
    final var expectedName = "Filmes";
    final var expectedDescription = "A categoria mais assistida";
    final var expectedIsActive = true;

    final var aCategory = Category.newCategory(expectedName, expectedDescription, expectedIsActive);

    assertEquals(0, categoryRepository.count());

    categoryRepository.saveAndFlush(CategoryJpaEntity.from(aCategory));

    assertEquals(1, categoryRepository.count());

    final var actualCategory = categoryGateway.findById(aCategory.getId()).get();

    assertEquals(1, categoryRepository.count());

    assertEquals(aCategory.getId(), actualCategory.getId());
    assertEquals(expectedName, actualCategory.getName());
    assertEquals(expectedDescription, actualCategory.getDescription());
    assertEquals(expectedIsActive, actualCategory.isActive());
    assertEquals(aCategory.getCreatedAt(), actualCategory.getCreatedAt());
    assertEquals(aCategory.getUpdatedAt(), actualCategory.getUpdatedAt());
    assertEquals(aCategory.getDeletedAt(), actualCategory.getDeletedAt());
    assertNull(actualCategory.getDeletedAt());
  }

  @Test
  public void givenValidCategoryIdNotStored_whenCallsFindById_shouldReturnEmpty() {
    assertEquals(0, categoryRepository.count());

    final var actualCategory = categoryGateway.findById(CategoryID.from("empty"));

    Assertions.assertTrue(actualCategory.isEmpty());
  }

  @Test
  public void givenPrePersistedCategories_whenCallsFindAll_shouldReturnPaginated() {
    final var expectedPage = 0;
    final var expectedPerPage = 1;
    final var expectedTotal = 3;

    final var filmes = Category.newCategory("Filmes", null, true);
    final var series = Category.newCategory("Séries", null, true);
    final var documentarios = Category.newCategory("Documentários", null, true);

    assertEquals(0, categoryRepository.count());

    categoryRepository.saveAll(List.of(
        CategoryJpaEntity.from(filmes),
        CategoryJpaEntity.from(series),
        CategoryJpaEntity.from(documentarios)
    ));

    assertEquals(3, categoryRepository.count());

    final var query = new SearchQuery(0, 1, "", "name", "asc");
    final var actualResult = categoryGateway.findAll(query);

    assertEquals(expectedPage, actualResult.currentPage());
    assertEquals(expectedPerPage, actualResult.perPage());
    assertEquals(expectedTotal, actualResult.total());
    assertEquals(expectedPerPage, actualResult.items().size());
    assertEquals(documentarios.getId(), actualResult.items().get(0).getId());
  }

  @Test
  public void givenEmptyCategoriesTable_whenCallsFindAll_shouldReturnEmptyPage() {
    final var expectedPage = 0;
    final var expectedPerPage = 1;
    final var expectedTotal = 0;

    assertEquals(0, categoryRepository.count());

    final var query = new SearchQuery(0, 1, "", "name", "asc");
    final var actualResult = categoryGateway.findAll(query);

    assertEquals(expectedPage, actualResult.currentPage());
    assertEquals(expectedPerPage, actualResult.perPage());
    assertEquals(expectedTotal, actualResult.total());
    assertEquals(0, actualResult.items().size());
  }

  @Test
  public void givenFollowPagination_whenCallsFindAllWithPage1_shouldReturnPaginated() {
    var expectedPage = 0;
    final var expectedPerPage = 1;
    final var expectedTotal = 3;

    final var filmes = Category.newCategory("Filmes", null, true);
    final var series = Category.newCategory("Séries", null, true);
    final var documentarios = Category.newCategory("Documentários", null, true);

    assertEquals(0, categoryRepository.count());

    categoryRepository.saveAll(List.of(
        CategoryJpaEntity.from(filmes),
        CategoryJpaEntity.from(series),
        CategoryJpaEntity.from(documentarios)
    ));

    assertEquals(3, categoryRepository.count());

    var query = new SearchQuery(0, 1, "", "name", "asc");
    var actualResult = categoryGateway.findAll(query);

    assertEquals(expectedPage, actualResult.currentPage());
    assertEquals(expectedPerPage, actualResult.perPage());
    assertEquals(expectedTotal, actualResult.total());
    assertEquals(expectedPerPage, actualResult.items().size());
    assertEquals(documentarios.getId(), actualResult.items().get(0).getId());

    // Page 1
    expectedPage = 1;

    query = new SearchQuery(1, 1, "", "name", "asc");
    actualResult = categoryGateway.findAll(query);

    assertEquals(expectedPage, actualResult.currentPage());
    assertEquals(expectedPerPage, actualResult.perPage());
    assertEquals(expectedTotal, actualResult.total());
    assertEquals(expectedPerPage, actualResult.items().size());
    assertEquals(filmes.getId(), actualResult.items().get(0).getId());

    // Page 2
    expectedPage = 2;

    query = new SearchQuery(2, 1, "", "name", "asc");
    actualResult = categoryGateway.findAll(query);

    assertEquals(expectedPage, actualResult.currentPage());
    assertEquals(expectedPerPage, actualResult.perPage());
    assertEquals(expectedTotal, actualResult.total());
    assertEquals(expectedPerPage, actualResult.items().size());
    assertEquals(series.getId(), actualResult.items().get(0).getId());
  }

  @Test
  public void givenPrePersistedCategoriesAndDocAsTerms_whenCallsFindAllAndTermsMatchsCategoryName_shouldReturnPaginated() {
    final var expectedPage = 0;
    final var expectedPerPage = 1;
    final var expectedTotal = 1;

    final var filmes = Category.newCategory("Filmes", null, true);
    final var series = Category.newCategory("Séries", null, true);
    final var documentarios = Category.newCategory("Documentários", null, true);

    assertEquals(0, categoryRepository.count());

    categoryRepository.saveAll(List.of(
        CategoryJpaEntity.from(filmes),
        CategoryJpaEntity.from(series),
        CategoryJpaEntity.from(documentarios)
    ));

    assertEquals(3, categoryRepository.count());

    final var query = new SearchQuery(0, 1, "doc", "name", "asc");
    final var actualResult = categoryGateway.findAll(query);

    assertEquals(expectedPage, actualResult.currentPage());
    assertEquals(expectedPerPage, actualResult.perPage());
    assertEquals(expectedTotal, actualResult.total());
    assertEquals(expectedPerPage, actualResult.items().size());
    assertEquals(documentarios.getId(), actualResult.items().get(0).getId());
  }

  @Test
  public void givenPrePersistedCategoriesAndMaisAssistidaAsTerms_whenCallsFindAllAndTermsMatchsCategoryDescription_shouldReturnPaginated() {
    final var expectedPage = 0;
    final var expectedPerPage = 1;
    final var expectedTotal = 1;

    final var filmes = Category.newCategory("Filmes", "A categoria mais assistida", true);
    final var series = Category.newCategory("Séries", "Uma categoria assistida", true);
    final var documentarios = Category.newCategory("Documentários", "A categoria menos assistida", true);

    assertEquals(0, categoryRepository.count());

    categoryRepository.saveAll(List.of(
        CategoryJpaEntity.from(filmes),
        CategoryJpaEntity.from(series),
        CategoryJpaEntity.from(documentarios)
    ));

    assertEquals(3, categoryRepository.count());

    final var query = new SearchQuery(0, 1, "MAIS ASSISTIDA", "name", "asc");
    final var actualResult = categoryGateway.findAll(query);

    assertEquals(expectedPage, actualResult.currentPage());
    assertEquals(expectedPerPage, actualResult.perPage());
    assertEquals(expectedTotal, actualResult.total());
    assertEquals(expectedPerPage, actualResult.items().size());
    assertEquals(filmes.getId(), actualResult.items().get(0).getId());
  }

  @Test
  public void givenPrePersistedCategories_whenCallsExistsByIds_shouldReturnIds() {
    // given
    final var filmes = Category.newCategory("Filmes", "A categoria mais assistida", true);
    final var series = Category.newCategory("Séries", "Uma categoria assistida", true);
    final var documentarios = Category.newCategory("Documentários", "A categoria menos assistida", true);

    assertEquals(0, categoryRepository.count());

    categoryRepository.saveAll(List.of(
        CategoryJpaEntity.from(filmes),
        CategoryJpaEntity.from(series),
        CategoryJpaEntity.from(documentarios)
    ));

    assertEquals(3, categoryRepository.count());

    final var expectedIds = List.of(filmes.getId(), series.getId());

    final var ids = List.of(filmes.getId(), series.getId(), CategoryID.from("123"));

    // when
    final var actualResult = categoryGateway.existsByIds(ids);

    Assertions.assertTrue(
        expectedIds.size() == actualResult.size() &&
            expectedIds.containsAll(actualResult)
    );
  }
}