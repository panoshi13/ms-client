package com.pichincha.ms.client.service;

import com.pichincha.ms.client.dto.ClientDTO;
import com.pichincha.ms.client.model.Client;
import com.pichincha.ms.client.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final WebClient webClient;

    public ClientService(ClientRepository clientRepository, WebClient.Builder webClientBuilder) {
        this.clientRepository = clientRepository;
        this.webClient = webClientBuilder.baseUrl("https://gorest.co.in/public/v2/users").build();
    }

    public Mono<ClientDTO> registerClient(ClientDTO clientDTO) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("name", clientDTO.getName())
                        .queryParam("email", clientDTO.getEmail())
                        .build())
                .retrieve()
                .bodyToFlux(Client.class)
                .next() // Obtiene el primer cliente encontrado
                .flatMap(existingClient -> {
                    // Si el cliente existe, actualiza el estado
                    var client = new Client();
                    client.setName(existingClient.getName());
                    client.setEmail(existingClient.getEmail());
                    client.setStatus("exists");
                    return clientRepository.save(client);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // Si el cliente no existe, crea un cliente nuevo con estado "active"
                    var client = new Client();
                    client.setName(clientDTO.getName());
                    client.setEmail(clientDTO.getEmail());
                    client.setGender(clientDTO.getGender());
                    client.setStatus("active");
                    return clientRepository.save(client);
                }))
                // Mapea el resultado (Client) a ClientDTO
                .map(this::toClientDTO);
    }

    public ClientDTO toClientDTO(Client client){
        return new ClientDTO(client.getName(),client.getEmail(),client.getGender(), client.getStatus());
    }

    public Mono<ClientDTO> updateClient(Long id, ClientDTO updatedClient) {
        return clientRepository.findById(id)
                .flatMap(client -> {
                    client.setName(updatedClient.getName());
                    client.setEmail(updatedClient.getEmail());
                    client.setGender(updatedClient.getGender());
                    return clientRepository.save(client);
                })
                .map(this::toClientDTO);
    }

    public Flux<ClientDTO> searchClients(String name, String email) {
        return clientRepository.findAllByNameContainingOrEmailContaining(name, email)
                .map(this::toClientDTO);
    }

    public Mono<Void> deleteClient(Long id) {
        return clientRepository.findById(id)
                .flatMap(clientRepository::delete);
    }
}
