package ec.edu.espe.agrosmart.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Service
public class PublicidadService {

    private final AgroSmartAIService agroSmartAIService;

    public PublicidadService(AgroSmartAIService agroSmartAIService) {
        this.agroSmartAIService = agroSmartAIService;
    }

    public Mono<String> generarPublicidad(
            String producto,
            String audiencia
    ) {
        return Mono.fromCallable(
                        () -> agroSmartAIService.generarPublicidad(
                                producto,
                                audiencia
                        )
                )
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(Duration.ofSeconds(30))
                .onErrorResume(error -> Mono.just(
                        "Publicidad no disponible en este momento"
                ));
    }
}