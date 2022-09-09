package com.springboot.pruebaproductos.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.springboot.pruebaproductos.documents.Articulos;

public interface ArticulosDAO extends ReactiveMongoRepository<Articulos,String> {
    
}
