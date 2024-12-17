package com.pichincha.ms.client.controller;

import com.pichincha.ms.client.dto.ClientDTO;
import com.pichincha.ms.client.service.ClientService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



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
    public Mono<ResponseEntity<ClientDTO>> updateClient(@PathVariable Long id, @RequestBody @Valid ClientDTO client) {
        return clientService.updateClient(id, client)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    public Flux<ClientDTO> searchClients(@RequestParam(required = false) @NotBlank(message = "El nombre no puede estar vacío.")  String name,
                                         @RequestParam(required = false)
                                         @NotBlank(message = "El email no puede estar vacío.")
                                         @Email(message = "El correo electrónico debe tener un formato válido.")
                                         String email) {
        return clientService.searchClients(name, email);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteClient(@PathVariable Long id) {
        return clientService.deleteClient(id)
                .map(v -> ResponseEntity.noContent().build());
    }
}
