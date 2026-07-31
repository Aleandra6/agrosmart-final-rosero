package ec.edu.espe.agrosmart.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;

class PublicidadServiceTest {

    @Test
    void generarPublicidad_cuandoLaIaResponde_debeEmitirElTexto() {
        // Arrange
        AgroSmartAIService aiService =
                Mockito.mock(AgroSmartAIService.class);

        Mockito.when(
                aiService.generarPublicidad(
                        anyString(),
                        anyString()
                )
        ).thenReturn(
                "Cacao ecuatoriano de calidad para exportadores europeos."
        );

        PublicidadService service =
                new PublicidadService(aiService);

        // Act & Assert
        StepVerifier.create(
                        service.generarPublicidad(
                                "Cacao fino de aroma",
                                "exportadores europeos"
                        )
                )
                .expectNext(
                        "Cacao ecuatoriano de calidad para exportadores europeos."
                )
                .verifyComplete();
    }

    @Test
    void generarPublicidad_cuandoLaIaFalla_debeEmitirMensajeDeRespaldo() {
        // Arrange
        AgroSmartAIService aiService =
                Mockito.mock(AgroSmartAIService.class);

        Mockito.when(
                aiService.generarPublicidad(
                        anyString(),
                        anyString()
                )
        ).thenThrow(
                new RuntimeException("429 Too Many Requests")
        );

        PublicidadService service =
                new PublicidadService(aiService);

        // Act & Assert
        StepVerifier.create(
                        service.generarPublicidad(
                                "Cacao fino de aroma",
                                "exportadores europeos"
                        )
                )
                .expectNext(
                        "Publicidad no disponible en este momento"
                )
                .verifyComplete();
    }
}