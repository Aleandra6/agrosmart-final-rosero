package ec.edu.espe.agrosmart.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductoTest {

    @Test
    void getters_conDatosDelConstructor_debenDevolverLosValoresRecibidos() {
        // Arrange
        List<String> correos = List.of("ventas@agrosmart.ec");

        Producto producto = new Producto(
                1L,
                "Cacao fino de aroma",
                "Cacao",
                new BigDecimal("120.50"),
                correos
        );

        // Act & Assert
        assertEquals(1L, producto.getId());
        assertEquals("Cacao fino de aroma", producto.getNombre());
        assertEquals("Cacao", producto.getCategoria());
        assertEquals(new BigDecimal("120.50"), producto.getPrecioUsd());
        assertEquals(correos, producto.getCorreosNotificacion());
    }

    @Test
    void constructor_alModificarListaOriginal_noDebeCambiarEstadoInterno() {
        // Arrange
        List<String> correos = new ArrayList<>();
        correos.add("ventas@agrosmart.ec");

        Producto producto = new Producto(
                1L,
                "Cacao fino de aroma",
                "Cacao",
                new BigDecimal("120.50"),
                correos
        );

        // Act
        correos.add("intruso@correo.com");

        // Assert
        assertEquals(1, producto.getCorreosNotificacion().size());
        assertFalse(
                producto.getCorreosNotificacion()
                        .contains("intruso@correo.com")
        );
    }

    @Test
    void getterCorreos_debeDevolverUnaCopiaDistintaEInmodificable() {
        // Arrange
        List<String> correos = new ArrayList<>();
        correos.add("ventas@agrosmart.ec");

        Producto producto = new Producto(
                1L,
                "Cacao fino de aroma",
                "Cacao",
                new BigDecimal("120.50"),
                correos
        );

        // Act
        List<String> correosObtenidos =
                producto.getCorreosNotificacion();

        // Assert
        assertNotSame(correos, correosObtenidos);
        assertEquals(correos, correosObtenidos);

        assertThrows(
                UnsupportedOperationException.class,
                () -> correosObtenidos.add("nuevo@correo.com")
        );
    }

    @Test
    void constructor_conCorreosNulos_debeCrearListaVacia() {
        // Arrange & Act
        Producto producto = new Producto(
                2L,
                "Cacao sin correos",
                "Cacao",
                new BigDecimal("100.00"),
                null
        );

        // Assert
        assertNotNull(producto.getCorreosNotificacion());
        assertTrue(producto.getCorreosNotificacion().isEmpty());
    }
}