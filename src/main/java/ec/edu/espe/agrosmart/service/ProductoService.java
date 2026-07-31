package ec.edu.espe.agrosmart.service;

import ec.edu.espe.agrosmart.domain.Producto;
import ec.edu.espe.agrosmart.domain.ProductoFilters;
import ec.edu.espe.agrosmart.exception.ProductoNoEncontradoException;
import ec.edu.espe.agrosmart.mapper.ProductoMapper;
import ec.edu.espe.agrosmart.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    private static final Producto PRODUCTO_GENERICO = new Producto(
            0L,
            "PRODUCTO GENÉRICO",
            "CACAO",
            BigDecimal.ONE,
            List.of("informacion@agrosmart.ec")
    );

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public Flux<Producto> obtenerProductosComercializables() {

        // fromCallable difiere la ejecución de la consulta hasta que exista
        // una suscripción al flujo.
        return Mono.fromCallable(productoRepository::findAll)

                // JPA es bloqueante. boundedElastic ejecuta la consulta fuera
                // del event loop de Netty para no bloquear las peticiones HTTP.
                .subscribeOn(Schedulers.boundedElastic())

                // Convierte la lista obtenida por JPA en emisiones individuales.
                .flatMapMany(Flux::fromIterable)

                // Convierte la entidad mutable de Hibernate al dominio inmutable.
                .map(ProductoMapper::toDominio)

                // Genera una instancia nueva con el nombre en mayúsculas.
                .map(ProductoFilters.A_MAYUSCULAS)

                // Solo permite productos con precio mayor que cero y correos.
                .filter(ProductoFilters.IS_VALID)

                // Ejecuta la acción de trazabilidad sin modificar el producto.
                .doOnNext(ProductoFilters.LOG_PRODUCTO)

                // Se emite únicamente si ningún producto superó el filtro.
                .defaultIfEmpty(PRODUCTO_GENERICO);
    }

    public Mono<Producto> buscarPorId(Long id) {

        // La consulta findById también es bloqueante y debe diferirse.
        return Mono.fromCallable(() -> productoRepository.findById(id))

                // Mueve la operación JPA a un hilo preparado para tareas bloqueantes.
                .subscribeOn(Schedulers.boundedElastic())

                // Convierte Optional.empty() en un Mono vacío.
                .flatMap(Mono::justOrEmpty)

                // Transforma la entidad encontrada al modelo de dominio.
                .map(ProductoMapper::toDominio)

                // Cuando el Mono está vacío, genera el error dentro del flujo.
                .switchIfEmpty(
                        Mono.error(new ProductoNoEncontradoException(id))
                );
    }
}