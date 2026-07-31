package ec.edu.espe.agrosmart.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductoFiltersTest {

    @Test
    void isValid_conPrecioPositivoYCorreos_debeRetornarTrue() {
        // Arrange
        Producto producto = new Producto(
                1L,
                "Cacao fino de aroma",
                "Cacao",
                new BigDecimal("120.50"),
                List.of("ventas@agrosmart.ec")
        );

        // Act
        boolean resultado =
                ProductoFilters.IS_VALID.test(producto);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void isValid_conPrecioCero_debeRetornarFalse() {
        // Arrange
        Producto producto = new Producto(
                2L,
                "Cacao sin precio",
                "Cacao",
                BigDecimal.ZERO,
                List.of("ventas@agrosmart.ec")
        );

        // Act
        boolean resultado =
                ProductoFilters.IS_VALID.test(producto);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void isValid_sinCorreos_debeRetornarFalse() {
        // Arrange
        Producto producto = new Producto(
                3L,
                "Cacao sin notificaciones",
                "Cacao",
                new BigDecimal("110.00"),
                List.of()
        );

        // Act
        boolean resultado =
                ProductoFilters.IS_VALID.test(producto);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void aMayusculas_conProductoValido_debeCrearNuevaInstancia() {
        // Arrange
        Producto original = new Producto(
                1L,
                "Cacao fino de aroma",
                "Cacao",
                new BigDecimal("120.50"),
                List.of("ventas@agrosmart.ec")
        );

        // Act
        Producto convertido =
                ProductoFilters.A_MAYUSCULAS.apply(original);

        // Assert
        assertNotSame(original, convertido);
        assertEquals("CACAO FINO DE AROMA", convertido.getNombre());
        assertEquals("Cacao fino de aroma", original.getNombre());
        assertEquals(original.getId(), convertido.getId());
        assertEquals(
                original.getCorreosNotificacion(),
                convertido.getCorreosNotificacion()
        );
    }
}