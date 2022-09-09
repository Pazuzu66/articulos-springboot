package com.springboot.pruebaproductos.services;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.springboot.pruebaproductos.documents.Articulos;
import com.springboot.pruebaproductos.repositories.ArticulosDAO;
import com.springboot.pruebaproductos.suscribes.BitacoraSubscription;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ArticuloServiceImpl implements ArticuloService {
    private static final String URL = "http://localhost:8080/api/bitacora";
    WebClient client = WebClient.create(URL);

    @Autowired
    private ArticulosDAO articulosDAO;

    @Override
    public Flux<Articulos> findAll() {
        return articulosDAO.findAll();
    }

    @Override
    public Mono<Articulos> findByClave(String clave) {
        return articulosDAO.findById(clave);
    }

    @Override
    public Mono<Articulos> save(Articulos articulo) {
        return articulosDAO.save(articulo);
    }

    @Override
    public Mono<Void> delete(Articulos articulo) {
        return articulosDAO.delete(articulo);
    }

    @Override
    public Flux<BitacoraSubscription> getBitacora() {
        Flux<BitacoraSubscription> bits = client.get().uri(URL).retrieve().bodyToFlux(BitacoraSubscription.class);
        return bits;
    }

    @Override
    public Mono<BitacoraSubscription> saveBitacora(BitacoraSubscription register) {
        return  client.post().uri(URL)
        .body(Mono.just(register), BitacoraSubscription.class)
        .retrieve()
        .bodyToMono(BitacoraSubscription.class);
    }

}
