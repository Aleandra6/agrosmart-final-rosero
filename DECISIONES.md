# 🧭 DECISIONES.md — Bitácora de diseño

> **Instrucciones.** Completa **una entrada por fase**, en **primera persona** y
> **refiriéndote a tu propio código**: nombres reales de tus clases, tu tabla, tus
> líneas, tu salida real de terminal.
>
> ❌ **No puntúa** una justificación genérica que podría pegarse en cualquier proyecto
> (ej.: *"usé boundedElastic porque es una buena práctica para operaciones bloqueantes"*).
> ✅ **Sí puntúa** una justificación anclada a tu código (ej.: *"en `ProductoService`
> línea 34 envolví `productoRepository.findAll()` porque Hibernate abre la conexión
> JDBC en el hilo llamante; al probarlo sin `subscribeOn` vi en el log el hilo
> `reactor-http-nio-2`, que es el event loop de Netty"*).
>
> Estas mismas preguntas se te harán en la **defensa oral**.

---

## Datos

- **Nombre:** Ana Alejandra Rosero Chiluisa
- **Cédula:** 1753348620
- **NN (dos últimos dígitos):** 20
- **Categoría asignada (según el último dígito):** Cacao

--- 

## Fase 1 — Configuración y perfiles

**1.1** ¿Qué archivo activa el perfil `prod` y qué línea exacta lo hace?

> En mi proyecto, el perfil prod se activa desde el archivo src/main/resources/application.properties mediante la línea:

>spring.profiles.active=prod

>Esta configuración hace que Spring Boot cargue también las propiedades específicas que coloqué en application-prod.properties, como el puerto 8120, la conexión a PostgreSQL y la configuración de Hibernate.

**1.2** Pega la línea del log de arranque donde se ve tu puerto y el perfil activo.

>2026-07-30T22:55:19.692-05:00 INFO 12432 --- [agrosmart] [main] e.e.espe.agrosmart.AgrosmartApplication : The following 1 profile is active: "prod"


>2026-07-30T22:55:24.908-05:00 INFO 12432 --- [agrosmart] [main] o.s.b.web.embedded.netty.NettyWebServer : Netty started on port 8120 (http)


>2026-07-30T22:55:24.919-05:00 INFO 12432 --- [agrosmart] [main] e.e.espe.agrosmart.AgrosmartApplication : Started AgrosmartApplication in 5.844 seconds


**1.3** ¿Qué habría pasado si dejabas `ddl-auto=create-drop` en lugar de `update`?
Responde pensando en tus datos sembrados.

>Si hubiera utilizado spring.jpa.hibernate.ddl-auto=create-drop, Hibernate habría creado la tabla tbl_productos_base_20 al iniciar la aplicación, pero la habría eliminado cuando la aplicación se detuviera. En consecuencia, los cinco productos de cacao sembrados mediante mi CommandLineRunner se perderían en cada cierre.

>Al volver a iniciar, la tabla y los registros se crearían nuevamente desde cero. Esto habría impedido conservar la información y las evidencias de la base. Por eso utilicé update, que mantiene la tabla y los datos existentes, y solamente ajusta el esquema cuando es necesario.

**1.4** ¿Levantaste PostgreSQL con `compose.yaml` (Opción A) o con una instalación local
(Opción B)? ¿Qué ventaja tiene la que elegiste?

>Utilicé la Opción A mediante el archivo compose.yaml. El contenedor se llama agrosmart-postgres, utiliza la imagen postgres:16 y expone el puerto interno 5432 mediante el puerto 55432 de mi computadora.

>Elegí Docker porque ya tenía otra instalación de PostgreSQL utilizada por pgAdmin en el puerto 5432. Al publicar el contenedor en 55432, evité conflictos entre ambas instalaciones.

>En mi application-prod.properties configuré la conexión de esta manera:

>spring.datasource.url=jdbc:postgresql://127.0.0.1:55432/agrosmart_db

>Además, utilicé spring.docker.compose.enabled=false porque levanté el contenedor manualmente con Docker Compose. La principal ventaja fue contar con una base aislada y reproducible, con la base agrosmart_db y el usuario agrosmart, sin modificar mi PostgreSQL local.
---

## Fase 2 — Persistencia con JPA/Hibernate

**2.1** ¿Cuál es el nombre exacto de tu tabla y de dónde salió ese nombre?

>El nombre exacto de mi tabla es tbl_productos_base_20.

>Ese nombre se obtuvo a partir de mi semilla personal. Los dos últimos dígitos de mi cédula son 20, por lo que reemplacé NN por 20 en el nombre obligatorio tbl_productos_base_NN.

>En mi clase ProductoEntity lo configuré con:

>@Table(name = "tbl_productos_base_20")

**2.2** Pega la salida de `psql -d agrosmart_db -c "\d tbl_productos_base_NN"` y
señala dónde se ve la restricción `unique` y el `length` de 120.

```
Table "public.tbl_productos_base_20"

Column                 Type                   Nullable
id_producto            bigint                 not null
categoria              character varying(40)
correos_notificacion   character varying(500)
nombre_producto        character varying(120) not null
precio_usd             numeric(10,2)
stock_kg               integer                not null

Indexes:
"tbl_productos_base_20_pkey" PRIMARY KEY, btree (id_producto)
"uk2iiyp8xqy8qmt3bxgkn1l790f" UNIQUE CONSTRAINT, btree (nombre_producto)
```


**2.3** ¿Por qué usaste `BigDecimal` y no `double` para `precio_usd`? Relaciónalo con el
tipo que generó Hibernate en PostgreSQL.

>Usé BigDecimal en ProductoEntity y en el modelo Producto porque representa valores monetarios con precisión decimal exacta.

>Un double trabaja con representación binaria de punto flotante y puede generar pequeñas diferencias de precisión en operaciones con dinero. Eso sería inadecuado para un campo como precio_usd.

>En mi entidad declaré:

>@Column(name = "precio_usd", precision = 10, scale = 2)

>junto con:

>private BigDecimal precioUsd;

>Hibernate generó en PostgreSQL el tipo numeric(10,2), que conserva exactamente diez dígitos de precisión y dos decimales. Esto coincide con los valores sembrados, por ejemplo 120.50, 145.00 y 98.75.

**2.4** ¿Cómo hiciste idempotente tu siembra y qué pasaría en el segundo arranque si no
lo fuera? (piensa en la restricción `unique` de `nombre_producto`)

>Hice la siembra idempotente dentro del CommandLineRunner de AgrosmartApplication.

>Antes de guardar los productos, mi código comprueba:

>if (repository.count() == 0)

>Solamente cuando la tabla está vacía se ejecuta repository.saveAll(productos).

>De esta forma, en el segundo arranque la aplicación detecta que ya existen cinco registros y no vuelve a insertarlos.

>Si no hubiera agregado esa condición, Spring intentaría insertar nuevamente productos con los mismos nombres. Como nombre_producto tiene una restricción unique, el segundo arranque produciría un error de clave duplicada y la aplicación podría fallar durante la siembra.

---

## Fase 3 — Modelo inmutable y lógica funcional

**3.1** ¿Por qué tienes **dos** clases (`ProductoEntity` y `Producto`) en lugar de una?
¿Qué te impide hacer inmutable directamente la entidad de Hibernate?

>En mi proyecto separé ProductoEntity de Producto porque cumplen responsabilidades distintas.

>ProductoEntity pertenece a la capa de persistencia. Está anotada con @Entity, se mapea a tbl_productos_base_20 y tiene constructor vacío, getters y setters porque Hibernate necesita crear y materializar sus instancias.

>Producto, en cambio, pertenece al dominio. La declaré como final, con atributos private final, sin setters y con copias defensivas.

>No hice inmutable directamente ProductoEntity porque Hibernate necesita modificar sus campos al recuperar datos desde PostgreSQL. Si eliminara el constructor vacío y los setters, o hiciera todos sus atributos finales, dificultaría o impediría que el ORM construya correctamente la entidad.

**3.2** Escribe el código exacto de **tus dos** copias defensivas e indica en qué línea
está cada una.

```
// Copia defensiva de entrada, dentro del constructor de Producto
this.correosNotificacion = correosNotificacion == null
        ? new ArrayList<>()
        : new ArrayList<>(correosNotificacion);

// Copia defensiva de salida, dentro de getCorreosNotificacion()
return Collections.unmodifiableList(
        new ArrayList<>(correosNotificacion)
);
```
>La copia defensiva de entrada está en el constructor de Producto, en la asignación del atributo correosNotificacion.

>La copia defensiva de salida está en el método getCorreosNotificacion().


**3.3** ¿Por qué la copia defensiva **solo en el getter** no sería suficiente? Describe
el ataque concreto que quedaría abierto sobre **tu** clase.

>La copia defensiva solo en el getter no sería suficiente porque el constructor podría guardar directamente la referencia de la lista recibida.

>Por ejemplo, si creo un producto con una lista llamada correos, y después ejecuto:

>correos.add("intruso@correo.com");

>el estado interno del producto también cambiaría si el constructor hubiera guardado esa misma referencia.

>Aunque el getter devolviera una copia inmodificable, el objeto externo todavía tendría acceso indirecto a la lista original entregada al constructor. Por eso en mi clase hago una copia al entrar y otra copia de solo lectura al salir.

**3.4** ¿Cómo implementaste `A_MAYUSCULAS` para no mutar el `Producto` recibido?

```java
public static final Function<Producto, Producto> A_MAYUSCULAS =
        producto -> new Producto(
                producto.getId(),
                producto.getNombre().toUpperCase(),
                producto.getCategoria(),
                producto.getPrecioUsd(),
                producto.getCorreosNotificacion()
        );
```
>En ProductoFilters.A_MAYUSCULAS no modifico el objeto recibido. Creo una nueva instancia de Producto, conservo el identificador, la categoría, el precio y los correos, y únicamente transformo el nombre con toUpperCase().

>Mi prueba aMayusculas_conProductoValido_debeCrearNuevaInstancia verifica esto con assertNotSame, y también confirma que el nombre del objeto original permanece como Cacao fino de aroma, mientras que el nuevo queda como CACAO FINO DE AROMA.
---

## Fase 4 — Servicio reactivo y aislamiento del bloqueo

**4.1** Pega tu método `obtenerProductosComercializables()` completo.

```java
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
```

**4.2** ¿Qué pasa **exactamente** si eliminas
`.subscribeOn(Schedulers.boundedElastic())` de ese método? Si lo probaste, indica qué
hilo aparecía en el log antes y después.

>En mi método obtenerProductosComercializables(), la llamada bloqueante es productoRepository.findAll(). JPA/Hibernate utiliza JDBC y espera de forma síncrona la respuesta de PostgreSQL.

>Si eliminara .subscribeOn(Schedulers.boundedElastic()), esa consulta podría ejecutarse en el hilo que realiza la suscripción. Cuando la llamada proviene del controlador WebFlux, ese hilo puede ser uno de los reactor-http-nio del event loop de Netty.

>Mientras Hibernate espera la conexión y los registros de tbl_productos_base_20, ese hilo no podría atender otras solicitudes. Con varias peticiones simultáneas, el rendimiento de toda la API se degradaría aunque el controlador devuelva Flux.


**4.3** ¿Por qué `Mono.fromCallable(...)` y no `Mono.just(repository.findAll())`?
(pista: cuándo se ejecuta cada uno)

>Utilicé Mono.fromCallable(productoRepository::findAll) porque difiere la ejecución de la consulta hasta que alguien se suscribe al flujo.

>En cambio, con:

>Mono.just(productoRepository.findAll())

>primero se ejecutaría findAll() de forma inmediata y bloqueante, y solamente después se construiría el Mono. En ese caso, subscribeOn(boundedElastic()) ya no podría trasladar correctamente la consulta, porque el bloqueo habría ocurrido antes de crear el flujo.

>Con fromCallable, la consulta conserva además el comportamiento lazy propio de Reactor.

**4.4** En **tu** código, ¿dónde usaste `defaultIfEmpty` y dónde `switchIfEmpty`, y por
qué no son intercambiables en esos dos lugares?

>Usé defaultIfEmpty(PRODUCTO_GENERICO) al final de obtenerProductosComercializables().

>Después de aplicar filter(ProductoFilters.IS_VALID), el Flux puede quedar vacío si todos los productos tienen precio cero o no tienen correos. En ese caso quiero emitir directamente un valor alternativo: mi PRODUCTO_GENERICO.

>Usé switchIfEmpty(Mono.error(new ProductoNoEncontradoException(id))) en buscarPorId(Long id).

>Ahí no quiero devolver un producto por defecto. Si repository.findById(id) produce un Optional.empty(), necesito sustituir el Mono vacío por otro publisher que termine con un error.

>No son intercambiables porque defaultIfEmpty recibe un valor concreto, mientras que switchIfEmpty cambia a otro Publisher, que en mi caso es Mono.error(...).

**4.5** ¿Por qué `doOnNext` no sirve para transformar el elemento, si aparentemente
"recibe" el producto?

>En mi flujo utilicé:

>doOnNext(ProductoFilters.LOG_PRODUCTO)

>Este operador recibe cada Producto emitido para ejecutar un efecto secundario de trazabilidad: imprime su identificador y nombre.
>doOnNext no reemplaza el elemento ni devuelve uno nuevo. Después de ejecutarlo, el flujo continúa con el mismo producto.

>Para transformar sí utilicé map, concretamente:

>map(ProductoMapper::toDominio)

>y:

>map(ProductoFilters.A_MAYUSCULAS)

>Esos operadores reciben un elemento y devuelven el valor transformado que continuará por la cadena.

---

## Fase 5 — Módulo de IA con LangChain4j

**5.1** Pega tu interfaz `AgroSmartAIService` completa.

```java
package ec.edu.espe.agrosmart.service;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface AgroSmartAIService {

    @UserMessage("""
            Redacta una frase publicitaria de máximo 100 caracteres para vender \
            {{producto}} dirigido a {{audiencia}}.
            """)
    String generarPublicidad(
            @V("producto") String producto,
            @V("audiencia") String audiencia
    );
}
```

**5.2** ¿Qué hace `@V("producto")` y qué pasaría si lo quitaras dejando solo el
parámetro?

>En mi método generarPublicidad, la anotación @V("producto") relaciona el parámetro Java producto con la variable {{producto}} usada dentro de @UserMessage.

>De la misma forma, @V("audiencia") reemplaza {{audiencia}}.

>Si quitara @V("producto"), LangChain4j no tendría una asociación explícita entre el parámetro y la variable del prompt. En consecuencia, la sustitución podría fallar y el modelo recibiría el marcador sin completar o la creación del servicio produciría un error de configuración.

**5.3** ¿En qué archivo y con qué líneas configuraste el modelo? ¿Por qué **no** hizo
falta declarar un `@Bean`?

```
Configuré el modelo en src/main/resources/application-prod.properties con estas líneas:

langchain4j.open-ai.chat-model.api-key=demo

langchain4j.open-ai.chat-model.model-name=gpt-4o-mini

langchain4j.open-ai.chat-model.timeout=30s

No declaré un @Bean porque en mi build.gradle agregué los starters:

langchain4j-spring-boot-starter

y:

langchain4j-open-ai-spring-boot-starter

Estos starters leen automáticamente las propiedades y crean el modelo requerido por la interfaz anotada con @AiService.
```
**5.4** ¿Por qué la llamada a la IA también necesita `boundedElastic`, si no es una
consulta a base de datos?

>En PublicidadService, el método agroSmartAIService.generarPublicidad(producto, audiencia) realiza una llamada HTTP síncrona al proveedor del modelo.

>Aunque no sea una consulta JDBC, sigue siendo bloqueante porque el hilo espera hasta recibir una respuesta, un timeout o un error del proveedor.

>Por eso la envolví en:

>Mono.fromCallable(...)

>y después utilicé:

>.subscribeOn(Schedulers.boundedElastic())

>Así evito que la espera de red se ejecute en el event loop de Netty.

**5.5** Si tu proveedor devolvió un error durante el examen, pega el mensaje real y la
respuesta que produjo tu `onErrorResume`.

```

```
>Durante mi prueba, el proveedor respondió correctamente, por lo que onErrorResume no se ejecutó. Aun así, mantuve este operador para cubrir errores de cuota, conectividad y timeout.
---

## Fase 6 — API reactiva con WebFlux

**6.1** Pega la salida real de tus cuatro `curl`.

```
GET /api/productos
StatusCode        : 200
StatusDescription : OK
Content           : {"id":1,"nombre":"Cacao fino de aroma","categoria":"Cacao","precioUsd":120.50,"correosNotificacion":["ventas@agrosmart.ec"]},
                    {"id":2,"nombre":"CACAO ORGANICO PREMIUM","categoria":"Cacao","precioUsd"... 
RawContent        : HTTP/1.1 200 OK
                    transfer-enconding: chuked
                    Content-Type: application/json
                    
                    {"id":1,"nombre":"Cacao fino de aroma","categoria":"Cacao","precioUsd":120.50,"correosNotificacion":["ventas@agrosmart.e...
Forms             : {}
Headers           : {[transfer-enconding], [Content-Type, application/json]}
Images            : {}
InputFields       : {}
Links             : {}
ParsedHtml        : mshtml.HTMLDocumentClass
RawContentLength  : enconding


GET /api/productos/1
StatusCode        : 200
StatusDescription : OK
Content           : {"id":1,"nombre":"Cacao fino de aroma","categoria":"Cacao","precioUsd":120.50,"correosNotificacion":["ventas@agrosmart.ec"]}
RawContent        : HTTP/1.1 200 OK
                    Content-Length: 124
                    Content-Type: application/json
                    
                    {"id":1,"nombre":"Cacao fino de aroma","categoria":"Cacao","precioUsd":120.50,"correosNotificacion":["ventas@agrosmart.ec"]}
Forms             : {}
Headers           : {[Content-Length, 124], [Content-Type, application/json]}
Images            : {}
InputFields       : {}
Links             : {}
ParsedHtml        : mshtml.HTMLDocumentClass
RawContentLength  : 124

GET /api/productos/9999
HTTP/1.1 404 Not Found
Content-Type: application/json
Content-Length: 132

{"timestamp":"2026-07-31T16:55:42.672+00:00","path":"/api/productos/9999","status":404,"error":"Not Found","requestId":"8609dd78-3"}

GET /api/agrosmart/publicidad
StatusCode        : 200
StatusDescription : OK
Content           : "Descubre nuestro cacao fino de aroma: calidad excepcional para deleitar el paladar europeo."
RawContent        : HTTP/1.1 200 OK
                    Content-Length: 93
                    Content-Type: text/plain;charset=UTF-8
                    
                    "Descubre nuestro cacao fino de aroma: calidad excepcional para deleitar el paladar europeo."
Forms             : {}
Headers           : {[Content-Length, 93], [Content-Type, text/plain;charset=UTF-8]}
Images            : {}
InputFields       : {}
Links             : {}
ParsedHtml        : mshtml.HTMLDocumentClass
RawContentLength  : 93

```

**6.2** ¿Cómo lograste que el id inexistente responda **404** y no 500?

>En ProductoService.buscarPorId(Long id) convertí el Optional.empty() del repositorio en un Mono vacío mediante:

>flatMap(Mono::justOrEmpty)

>Después usé:

>switchIfEmpty(Mono.error(new ProductoNoEncontradoException(id)))

>En mi clase ProductoNoEncontradoException agregué:

>@ResponseStatus(HttpStatus.NOT_FOUND)

>De esta forma, Spring WebFlux traduce esa excepción a una respuesta HTTP 404 Not Found en lugar de responder con un error interno 500.

**6.3** ¿Qué pasaría si tu controlador devolviera `List<Producto>` en lugar de
`Flux<Producto>`? ¿Seguiría compilando? ¿Seguiría siendo no bloqueante?

>Sí podría compilar si modificara también el servicio para devolver una lista, pero dejaría de respetar la arquitectura reactiva exigida.

>Para devolver List<Producto>, tendría que materializar todo el flujo antes de responder. Eso normalmente implicaría usar una operación bloqueante como block() o ejecutar la consulta de forma imperativa.

>Aunque el controlador pudiera compilar, ya no conservaría el procesamiento por emisiones ni el comportamiento lazy de Reactor. Además, si la lista se obtuviera bloqueando el event loop de Netty, la aplicación dejaría de ser no bloqueante.

>Por eso en AgroSmartController mantuve:

>Flux<Producto> para /api/productos

>y:

>Mono<Producto> para /api/productos/{id}.

---

## Fase 7 — Pruebas unitarias

**7.1** Pega la salida real de tus pruebas (`./mvnw test` o `./gradlew test`).

```
C:\ProgramacionAvanzada\ExamenFinal\agrosmart-final-rosero>gradlew.bat clean test
Java HotSpot(TM) 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
2026-07-31T12:54:46.510-05:00  INFO 3788 --- [agrosmart] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-07-31T12:54:46.523-05:00  INFO 3788 --- [agrosmart] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-07-31T12:54:46.538-05:00  INFO 3788 --- [agrosmart] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.

BUILD SUCCESSFUL in 16s
5 actionable tasks: 5 executed
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.5.1/userguide/configuration_cache_enabling.html
```

**7.2** ¿Cuántos productos espera tu `expectNextCount(...)` y por qué ese número
concreto? Relaciónalo con tu semilla.

>En mi prueba obtenerProductosComercializables_conTresValidosYDosInvalidos_debeEmitirTres utilicé:

>expectNextCount(3)

>Espero exactamente tres productos porque mi semilla contiene cinco registros de cacao:

>Tres válidos con precio mayor que cero y al menos un correo.
>Uno inválido con precio 0.00.
>Uno inválido con correos vacíos.

>El Predicate ProductoFilters.IS_VALID descarta los dos inválidos, por lo que el Flux emite únicamente tres productos.

**7.3** ¿Por qué mockeaste `ProductoRepository` en lugar de dejar que la prueba consulte
PostgreSQL?

>Mockeé ProductoRepository con Mockito para que ProductoServiceTest fuera una prueba unitaria aislada.

>Si la prueba consultara PostgreSQL, dependería de que Docker estuviera encendido, que la base agrosmart_db existiera, que la tabla estuviera creada y que los datos fueran exactamente los esperados.

>Con el mock controlo directamente lo que devuelve findAll() y findById(). Esto hace que la prueba sea rápida, repetible y determinista, y evita que un problema de infraestructura produzca un fallo ajeno a la lógica de ProductoService.

**7.4** ¿Qué demuestra `assertNotSame` que `assertEquals` **no** demuestra en tu prueba
de copia defensiva?

>assertEquals demuestra que dos listas contienen los mismos valores.

>assertNotSame, en cambio, demuestra que no son la misma instancia en memoria.

>En mi prueba de copia defensiva, esto es importante porque el getter puede devolver una lista con el mismo contenido, pero debe ser una copia diferente. Si solo utilizara assertEquals, una implementación que devolviera directamente la lista original también podría pasar la prueba.

>Por eso utilicé ambos:

>assertNotSame(correos, correosObtenidos);

>y:

>assertEquals(correos, correosObtenidos);

**7.5** ¿Por qué una prueba de un `Flux` que no llama a `verifyComplete()` (o a
`verify()`) no está probando nada?

>Los flujos de Reactor son lazy: no ejecutan su lógica hasta que existe una suscripción.

>StepVerifier.create(flujo) solamente prepara la verificación. La ejecución real ocurre al llamar a verifyComplete() o verify().

>Sin esa llamada final, no se dispara la suscripción, no se ejecuta productoRepository.findAll(), no se aplican los operadores map, filter ni doOnNext, y tampoco se comprueba si el flujo terminó correctamente.

>Por eso en mis pruebas finalicé con:

>verifyComplete()

>para los casos exitosos, y:

>verify()

>para el caso que debía terminar con ProductoNoEncontradoException.

---

## Fase 8 — Integración y cierre

**8.1** Pega tu `git log --oneline --graph --all`.

```
Det-Pc@LAPTOP-70RNA24B MINGW64 /c/ProgramacionAvanzada/ExamenFinal/agrosmart-final-rosero (feature/pruebas)
$ git log --oneline --graph --all
* 27816b2 (HEAD -> feature/pruebas, origin/feature/pruebas) test: agrega pruebas del modelo, logica funcional, flujo reactivo e ia
*   b208539 (origin/main, origin/HEAD, main, feature/pruebas-unitarias) Merge pull request #6 from Aleandra6/feature/api-reactiva
|\
| * adb31c3 (origin/feature/api-reactiva, feature/api-reactiva) feat: expone api reactiva de productos y publicidad
|/
*   9c4d132 Merge pull request #5 from Aleandra6/feature/ia-langchain4j
|\
| * 4f7ca79 (origin/feature/ia-langchain4j, feature/ia-langchain4j) feat: integra langchain4j para publicidad de productos
| * 0092528 feat: integra servicio de pubicidad con langchain4j
* |   12cf6a7 Merge pull request #2 from Aleandra6/feature/persistencia-jpa
|\ \
| * | 2c68d7f (origin/feature/persistencia-jpa, feature/persistencia-jpa) feat: implementa persistencia JPA y siembra inicial de productos
* | |   fec2a04 Merge pull request #3 from Aleandra6/feature/modelo-inmutable
|\ \ \
| |_|/
|/| |
| * | 7eef333 (origin/feature/modelo-inmutable, feature/modelo-inmutable) feat: inicia modelo inmutable de producto
| |/
* |   ea979b5 Merge pull request #4 from Aleandra6/feature/servicio-reactivo
|\ \
| * | 1b37dc2 (origin/feature/servicio-reactivo, feature/servicio-reactivo) modificación
| * | 87d51f6 feat: agrega modelo inmutable de producto y logica funcional
| |/
| * 2c3560a (origin/feature/config-perfiles, feature/config-perfiles) chore: configura perfil prod con postgresql y puerto propio
|/
| *   e6ffd3a (refs/stash) On main: archivos temporales de main
|/|\
| | * 9dcf8bd untracked files on main: 02c6c39 chore: inicia proyecto agrosmart y registra identidad del examen
| * 2ddbfb6 index on main: 02c6c39 chore: inicia proyecto agrosmart y registra identidad del examen
|/
* 02c6c39 chore: inicia proyecto agrosmart y registra identidad del examen
* 5d7e405 Initial commit


```

**8.2** ¿Qué fase te tomó más tiempo del previsto y por qué?

>La fase que me tomó más tiempo fue la configuración inicial de PostgreSQL y Docker.

>Yo ya tenía PostgreSQL y pgAdmin instalados en mi computadora, por lo que inicialmente existió un conflicto con el puerto 5432. También se presentaron errores de autenticación entre Spring Boot y el contenedor.

>Para resolverlo publiqué el PostgreSQL de Docker en el puerto 55432, mantuve el puerto interno 5432 y configuré en application-prod.properties la URL:

>jdbc:postgresql://127.0.0.1:55432/agrosmart_db

>Después de verificar el usuario agrosmart, la base agrosmart_db y el estado del contenedor, la aplicación pudo iniciar correctamente con el perfil prod y Netty en el puerto 8120.

**8.3** Si tuvieras 30 minutos más, ¿qué mejorarías **primero** de tu entrega y por qué
esa y no otra?

>Si tuviera treinta minutos adicionales, mejoraría primero la documentación y la cobertura de pruebas.

>Agregaría pruebas adicionales para valores nulos, errores del repositorio y validaciones del controlador. También revisaría que todas las evidencias tengan nombres claros.

>Elegiría esta mejora antes que agregar nuevas funcionalidades porque el núcleo del examen ya está implementado. En este punto sería más importante aumentar la confiabilidad, la trazabilidad y la facilidad para que otra persona pueda ejecutar y revisar mi proyecto.

**8.4** Declara honestamente qué herramientas consultaste durante el examen
(documentación, apuntes, asistentes de IA) y para qué. **Esta declaración no descuenta
puntaje**; su omisión o falsedad sí constituye falta de honestidad académica.

>Durante el examen consulté el enunciado entregado por el docente y documentación técnica sobre Spring Boot, Spring Data JPA, Project Reactor, Docker, PostgreSQL y LangChain4j.

>También utilicé Gemini como apoyo para identificar errores de configuración.

>Ejecuté y verifiqué cada cambio en mi propio entorno, revisé las salidas de Gradle, Docker, PostgreSQL y curl, y adapté las soluciones a mi tabla tbl_productos_base_20, mi puerto 8120 y mi categoría Cacao.
