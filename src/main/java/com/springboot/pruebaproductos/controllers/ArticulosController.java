package com.springboot.pruebaproductos.controllers;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.springboot.pruebaproductos.documents.Articulos;
import com.springboot.pruebaproductos.services.ArticuloService;
import com.springboot.pruebaproductos.suscribes.BitacoraSubscription;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/articulos")
public class ArticulosController {
    @Autowired
    private ArticuloService service;

    @GetMapping
    public Mono<ResponseEntity<Flux<Articulos>>> listArticulos() {
        return Mono.just(
                ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(service.findAll()));
    }

    @GetMapping("/bitacora")
    public ResponseEntity<Flux<BitacoraSubscription>> getBitacoraArticulos() {
        Flux<BitacoraSubscription> bits = service.getBitacora();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(bits);
    }

    @PostMapping("/bitacora")
    public Mono<ResponseEntity<Map<String, Object>>> saveBitacoraController(
            @RequestBody Mono<BitacoraSubscription> registerMono) {
        Map<String, Object> response = new HashMap<>();
        return registerMono.flatMap(register -> {
            return service.saveBitacora(register).map(re -> {
                response.put("bitacora", re);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            });
        }).onErrorResume(error -> {
            return Mono.just(error).cast(WebClientResponseException.class)
                    .flatMap(e -> {
                        response.put("error", e.getResponseBodyAsString());
                        return Mono.just(ResponseEntity.badRequest().body(response));
                    });
        });
    }

    @GetMapping("/{clave}")
    public Mono<ResponseEntity<Articulos>> showArticulo(@PathVariable String clave) {
        return service.findByClave(clave).map(ar -> ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ar))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> saveArticulo(@Valid @RequestBody Mono<Articulos> aMono) {
        // Retornaremos un mono con un mapa
        Map<String, Object> response = new HashMap<>();
        BitacoraSubscription bitacora = new BitacoraSubscription();
        // flatMap me da varias salidas de una entrada
        return aMono.flatMap(articulo -> {
            // Una vez se guarde el articulo, transformamos la respuesta
            return service.save(articulo).map(ar -> {
                // Esto seria la body de las response
                response.put("articulo", ar);
                response.put("mensaje", "Articulo Guardado");
                response.put("timestamp", new Date());
                return ar;
                // Al final mandamos la body
                // return
                // ResponseEntity.created(URI.create("/api/articulos/".concat(ar.getClave())))
                // .contentType(MediaType.APPLICATION_JSON)
                // .body(response);
            }).flatMap(ar -> {
                bitacora.setAccion("CREATED");
                bitacora.setTimestamp(new Date().toString());
                bitacora.setBody("Se Cre?? un articulo con clave = " + ar.getClave());
                return saveBitacoraController(Mono.just(bitacora)).map(res -> {
                    if (res.getBody().get("error") != null) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                                .body(res.getBody());
                    } else {
                        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
                    }
                });
            });
        }).onErrorResume(error -> {
            // En caso de que halla un error
            // cachamos el error
            return Mono.just(error).cast(WebExchangeBindException.class)
                    // Obtenemos los errores de los campos
                    .flatMap(e -> Mono.just(e.getFieldErrors()))
                    .flatMapMany(Flux::fromIterable)
                    // Iteramos para ir realizando los mensajes de los campos erroneos
                    .map(fieldError -> "El campo " + fieldError.getField() + " " + fieldError.getDefaultMessage())
                    .collectList()
                    .flatMap(list -> {
                        // realizamos el body de la response
                        response.put("errors", list);
                        response.put("timestamp", new Date());
                        response.put("status", HttpStatus.BAD_REQUEST.value());
                        // Retornamos la respuesta
                        return Mono.just(ResponseEntity.badRequest().body(response));
                    });
        });
    }

    @PutMapping("/{clave}")
    public Mono<ResponseEntity<Map<String, Object>>> updateArticulo(@Valid @RequestBody Articulos articulo,
            @PathVariable String clave) {
        Map<String, Object> response = new HashMap<>();
        // Buscamos el Articulo por clave
        return service.findByClave(clave).flatMap(art -> {
            // Cuando lo encontramos actualizamos el articulo encontrado con el que
            // recibimos
            art.setNombre(articulo.getNombre());
            art.setPrecio(articulo.getPrecio());
            art.setUm(articulo.getUm());
            // Guardamos los cambios
            return service.save(art);
            // Creamos nuestra response
        }).flatMap(art -> {
            BitacoraSubscription bSubscription = new BitacoraSubscription();
            bSubscription.setAccion("UPDATED");
            bSubscription.setTimestamp(new Date().toString());
            bSubscription.setBody("Se actualiz?? un articulo con clave = " + art.getClave());
            response.put("articulo", art);
            return saveBitacoraController(Mono.just(bSubscription)).map(res -> {
                response.put("bitacora", res.getBody());
                response.put("mensaje", "Actualizado Correctamente");
                response.put("timestamp", new Date().toString());
                if (res.getBody().get("error") != null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                            .body(res.getBody());
                } else {
                    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
                }
            });
        }).onErrorResume(error -> {
            // En caso de que halla un error
            // cachamos el error
            return Mono.just(error).cast(WebExchangeBindException.class)
                    // Obtenemos los errores de los campos
                    .flatMap(e -> Mono.just(e.getFieldErrors()))
                    .flatMapMany(Flux::fromIterable)
                    // Iteramos para ir realizando los mensajes de los campos erroneos
                    .map(fieldError -> "El campo " + fieldError.getField() + " " + fieldError.getDefaultMessage())
                    .collectList()
                    .flatMap(list -> {
                        // realizamos el body de la response
                        response.put("errors", list);
                        response.put("timestamp", new Date());
                        response.put("status", HttpStatus.BAD_REQUEST.value());
                        // Retornamos la respuesta
                        return Mono.just(ResponseEntity.badRequest().body(response));
                    });
        });
    }

    @DeleteMapping("/{clave}")
    public Mono<ResponseEntity<Void>> deleteArticulo(@PathVariable String clave) {
        BitacoraSubscription bSubscription = new BitacoraSubscription();
        bSubscription.setAccion("DELETED");
        bSubscription.setBody("Se elimin?? un articulo con clave = " + clave);
        bSubscription.setTimestamp(new Date().toString());
        // Buscamos el articulo por clave
        return service.findByClave(clave).flatMap(ar -> {
            // Si lo encontramos llamamos la funcion delete y le mandamos nuestro document
            return service.delete(ar).then(
                    // UUna vez borrado realizamos la respuesta
                    Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
            // Si no encontramos el articulo por la clave hacemos esta respuesta
        }).defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/add")
    public Flux<Object> add(@Valid @RequestBody Mono<Articulos> monoArticulos) {
        Map<String, Object> response = new HashMap<>();
        BitacoraSubscription bit = new BitacoraSubscription();
        bit.setAccion("CREATED");
        bit.setBody("Se registr?? un nuevo Articulo");
        bit.setTimestamp(new Date().toString());
        return Flux.zip(saveArticulo(monoArticulos), saveBitacoraController(Mono.just(bit))).flatMap(tuples -> {
            response.put("Articulo", tuples.getT1().getBody().get("mensaje"));
            if (tuples.getT2().getBody().get("error") != null) {
                response.put("Bitacora", tuples.getT2().getBody().get("error"));
            } else {
                response.put("Bitacora", "Todo Ok");
            }
            return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response).getBody());
        });

    }

    @DeleteMapping("/delete/{clave}")
    public Flux<Object> delete(@PathVariable String clave) {
        Map<String, Object> response = new HashMap<>();
        BitacoraSubscription bit = new BitacoraSubscription();
        bit.setAccion("DELETED");
        bit.setBody("Se elimin?? un articulo");
        bit.setTimestamp(new Date().toString());
        return Flux.zip(deleteArticulo(clave), saveBitacoraController(Mono.just(bit))).flatMap(
                tuples -> {
                    response.put("status code articulo", tuples.getT1().getStatusCode());
                    response.put("status code bitacora", tuples.getT2().getStatusCode());
                    if (tuples.getT2().getBody().get("error") != null) {
                        response.put("bitacora", tuples.getT2().getBody().get("error"));
                    } else {
                        response.put("articulo", "Todo OK");
                        response.put("bitacora", "Todo OK");
                    }
                    return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response));
                });

    }

    @PutMapping("/update/{clave}")
    public Flux<Object> update(@Valid @RequestBody Articulos articulo, @PathVariable String clave) {
        Map<String, Object> response = new HashMap<>();
        BitacoraSubscription bit = new BitacoraSubscription();
        bit.setAccion("UPDATED");
        bit.setBody("Se actualiz?? un Articulo");
        bit.setTimestamp(new Date().toString());
        return Flux.zip(updateArticulo(articulo, clave), saveBitacoraController(Mono.just(bit))).flatMap(
                tuples -> {
                    if (tuples.getT2().getBody().get("error") != null) {
                        response.put("bitacora", tuples.getT2().getBody().get("error"));
                    } else {
                        response.put("articulo", "Todo OK");
                        response.put("bitacora", "Todo OK");
                    }
                    return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response));
                });
    }
}
