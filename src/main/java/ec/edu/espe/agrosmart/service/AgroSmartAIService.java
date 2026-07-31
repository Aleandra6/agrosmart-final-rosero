package ec.edu.espe.agrosmart.service;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface AgroSmartAIService {

    @UserMessage("""
            Genera una frase publicitaria breve y atractiva para el producto agrícola
            {{producto}}, dirigida a {{audiencia}}.
            La respuesta debe tener como máximo 30 palabras.
            """)
    String generarPublicidad(
            @V("producto") String producto,
            @V("audiencia") String audiencia
    );
}