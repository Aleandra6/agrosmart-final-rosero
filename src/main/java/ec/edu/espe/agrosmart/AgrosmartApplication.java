package ec.edu.espe.agrosmart;

import ec.edu.espe.agrosmart.entity.ProductoEntity;
import ec.edu.espe.agrosmart.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.List;

@SpringBootApplication
public class AgrosmartApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgrosmartApplication.class, args);
	}

	@Bean
	CommandLineRunner sembrarProductos(ProductoRepository repository) {
		return args -> {
			if (repository.count() == 0) {
				List<ProductoEntity> productos = List.of(
						new ProductoEntity(
								"Cacao fino de aroma",
								new BigDecimal("120.50"),
								250,
								"Cacao",
								"ventas@agrosmart.ec"
						),
						new ProductoEntity(
								"Cacao orgánico premium",
								new BigDecimal("145.00"),
								180,
								"Cacao",
								"exportaciones@agrosmart.ec,comercial@agrosmart.ec"
						),
						new ProductoEntity(
								"Cacao nacional seleccionado",
								new BigDecimal("98.75"),
								320,
								"Cacao",
								"pedidos@agrosmart.ec"
						),
						new ProductoEntity(
								"Cacao sin precio",
								new BigDecimal("0.00"),
								100,
								"Cacao",
								"ventas@agrosmart.ec"
						),
						new ProductoEntity(
								"Cacao sin notificaciones",
								new BigDecimal("110.00"),
								140,
								"Cacao",
								""
						)
				);

				repository.saveAll(productos);
			}
		};
	}
}