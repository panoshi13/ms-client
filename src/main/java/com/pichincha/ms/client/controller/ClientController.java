package com.pichincha.ms.client.controller;

import com.pichincha.ms.client.dto.ClientDTO;
import com.pichincha.ms.client.service.ClientService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    public Mono<ResponseEntity<ClientDTO>> registerClient(@RequestBody @Valid ClientDTO client) {
        return clientService.registerClient(client)
                .map(savedClient -> ResponseEntity.status(HttpStatus.CREATED).body(savedClient));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<ClientDTO>> updateClient(@PathVariable Long id, @RequestBody ClientDTO client) {
        return clientService.updateClient(id, client)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    public Flux<ClientDTO> searchClients(@NotBlank @RequestParam(required = false) String name,
                                      @NotBlank @RequestParam(required = false) String email) {
        return clientService.searchClients(name, email);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteClient(@PathVariable Long id) {
        return clientService.deleteClient(id)
                .map(v -> ResponseEntity.noContent().build());
    }
}
