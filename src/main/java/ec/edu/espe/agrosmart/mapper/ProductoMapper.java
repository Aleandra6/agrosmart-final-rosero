package ec.edu.espe.agrosmart.mapper;

import ec.edu.espe.agrosmart.domain.Producto;
import ec.edu.espe.agrosmart.entity.ProductoEntity;

import java.util.Arrays;
import java.util.List;

public final class ProductoMapper {

    private ProductoMapper() {
        // Evita instanciar esta clase de utilidades.
    }

    public static Producto toDominio(ProductoEntity entity) {

        List<String> correos;

        if (entity.getCorreosNotificacion() == null
                || entity.getCorreosNotificacion().isBlank()) {

            correos = List.of();

        } else {

            correos = Arrays.stream(
                            entity.getCorreosNotificacion().split(",")
                    )
                    .map(String::trim)
                    .filter(correo -> !correo.isEmpty())
                    .toList();
        }

        return new Producto(
                entity.getIdProducto(),
                entity.getNombreProducto(),
                entity.getCategoria(),
                entity.getPrecioUsd(),
                correos
        );
    }
}