package com.pichincha.ms.client.repository;

import com.pichincha.ms.client.model.Client;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ClientRepository extends R2dbcRepository<Client,Long> {
    Mono<Client> findByNameAndEmail(String name, String email);
    Flux<Client> findAllByNameContainingOrEmailContaining(String name, String email);
}
