package com.springboot.pruebaproductos.services;


import com.springboot.pruebaproductos.documents.Articulos;
import com.springboot.pruebaproductos.suscribes.BitacoraSubscription;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ArticuloService {
    public Flux<Articulos> findAll();

    public Mono<Articulos> findByClave(String clave);

    public Mono<Articulos> save(Articulos articulo);

    public Mono<Void> delete(Articulos clave);

    public Flux<BitacoraSubscription> getBitacora();

    public Mono<BitacoraSubscription> saveBitacora(BitacoraSubscription register);
}
