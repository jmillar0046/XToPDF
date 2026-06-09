package com.xtopdf.xtopdf.controllers.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xtopdf.xtopdf.converters.ConverterRegistry;
import java.util.List;
import java.util.Set;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Tag;
import net.jqwik.api.Provide;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

/**
 * Property-based tests for GraphQL resolvers.
 *
 * <p><b>Validates: Requirements 22.2</b>
 */
@Tag("Feature: GraphQL API, Property 48: GraphQL Field Selection")
class GraphQLPropertyTest {

  /**
   * Property 48: GraphQL Field Selection
   *
   * <p>For any non-empty set of supported extensions in the ConverterRegistry, the
   * supportedFormats query must return a non-empty sorted list containing exactly those extensions.
   *
   * <p><b>Validates: Requirements 22.2</b>
   */
  @Property(tries = 25)
  void supportedFormatsReturnsNonEmptyListWhenConvertersExist(
      @ForAll("extensionSets") Set<String> extensions) {
    // Arrange
    ConverterRegistry registry = mock(ConverterRegistry.class);
    when(registry.getSupportedExtensions()).thenReturn(extensions);
    ConversionQueryController controller = new ConversionQueryController(registry);

    // Act
    List<String> result = controller.supportedFormats();

    // Assert
    assertThat(result).isNotEmpty();
    assertThat(result).hasSameSizeAs(extensions);
    assertThat(result).containsExactlyInAnyOrderElementsOf(extensions);
    // Verify sorted order
    assertThat(result).isSorted();
  }

  @Provide
  Arbitrary<Set<String>> extensionSets() {
    return Arbitraries.strings()
        .alpha()
        .ofMinLength(2)
        .ofMaxLength(5)
        .map(s -> "." + s.toLowerCase())
        .set()
        .ofMinSize(1)
        .ofMaxSize(20);
  }
}
