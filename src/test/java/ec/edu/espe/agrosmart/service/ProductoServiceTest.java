package ec.edu.espe.agrosmart.service;

import ec.edu.espe.agrosmart.entity.ProductoEntity;
import ec.edu.espe.agrosmart.exception.ProductoNoEncontradoException;
import ec.edu.espe.agrosmart.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

class ProductoServiceTest {

    @Test
    void obtenerProductosComercializables_conTresValidosYDosInvalidos_debeEmitirTres() {
        // Arrange
        ProductoRepository repository =
                Mockito.mock(ProductoRepository.class);

        Mockito.when(repository.findAll())
                .thenReturn(datosConTresValidosYDosInvalidos());

        ProductoService service = new ProductoService(repository);

        // Act
        Flux<?> flujo =
                service.obtenerProductosComercializables();

        // Assert
        StepVerifier.create(flujo)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void obtenerProductosComercializables_conTodosInvalidos_debeEmitirProductoGenerico() {
        // Arrange
        ProductoRepository repository =
                Mockito.mock(ProductoRepository.class);

        Mockito.when(repository.findAll())
                .thenReturn(List.of(
                        crearEntidad(
                                1L,
                                "Cacao sin precio",
                                "0.00",
                                "ventas@agrosmart.ec"
                        ),
                        crearEntidad(
                                2L,
                                "Cacao sin correos",
                                "100.00",
                                ""
                        )
                ));

        ProductoService service = new ProductoService(repository);

        // Act & Assert
        StepVerifier.create(
                        service.obtenerProductosComercializables()
                )
                .expectNextMatches(producto ->
                        producto.getId().equals(0L)
                                && producto.getNombre()
                                .equals("PRODUCTO GENÉRICO")
                )
                .verifyComplete();
    }

    @Test
    void buscarPorId_conIdInexistente_debeEmitirError() {
        // Arrange
        ProductoRepository repository =
                Mockito.mock(ProductoRepository.class);

        Mockito.when(repository.findById(9999L))
                .thenReturn(Optional.empty());

        ProductoService service = new ProductoService(repository);

        // Act & Assert
        StepVerifier.create(service.buscarPorId(9999L))
                .expectError(ProductoNoEncontradoException.class)
                .verify();
    }

    @Test
    void buscarPorId_conIdExistente_debeEmitirProducto() {
        // Arrange
        ProductoRepository repository =
                Mockito.mock(ProductoRepository.class);

        ProductoEntity entity = crearEntidad(
                1L,
                "Cacao fino de aroma",
                "120.50",
                "ventas@agrosmart.ec"
        );

        Mockito.when(repository.findById(1L))
                .thenReturn(Optional.of(entity));

        ProductoService service = new ProductoService(repository);

        // Act & Assert
        StepVerifier.create(service.buscarPorId(1L))
                .expectNextMatches(producto ->
                        producto.getId().equals(1L)
                                && producto.getNombre()
                                .equals("Cacao fino de aroma")
                )
                .verifyComplete();
    }

    private List<ProductoEntity> datosConTresValidosYDosInvalidos() {
        return List.of(
                crearEntidad(
                        1L,
                        "Cacao fino de aroma",
                        "120.50",
                        "ventas@agrosmart.ec"
                ),
                crearEntidad(
                        2L,
                        "Cacao orgánico premium",
                        "145.00",
                        "exportaciones@agrosmart.ec"
                ),
                crearEntidad(
                        3L,
                        "Cacao nacional seleccionado",
                        "98.75",
                        "pedidos@agrosmart.ec"
                ),
                crearEntidad(
                        4L,
                        "Cacao sin precio",
                        "0.00",
                        "ventas@agrosmart.ec"
                ),
                crearEntidad(
                        5L,
                        "Cacao sin notificaciones",
                        "110.00",
                        ""
                )
        );
    }

    private ProductoEntity crearEntidad(
            Long id,
            String nombre,
            String precio,
            String correos
    ) {
        ProductoEntity entity = new ProductoEntity(
                nombre,
                new BigDecimal(precio),
                100,
                "Cacao",
                correos
        );

        entity.setIdProducto(id);

        return entity;
    }
}