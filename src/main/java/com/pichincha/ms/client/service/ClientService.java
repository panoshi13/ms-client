package com.pichincha.ms.client.service;

import com.pichincha.ms.client.dto.ClientDTO;
import com.pichincha.ms.client.model.Client;
import com.pichincha.ms.client.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final WebClient webClient;


    public Mono<ClientDTO> registerClient(ClientDTO clientDTO) {
        // Verifica si el correo electrónico ya está registrado en la base de datos
        return clientRepository.findByEmail(clientDTO.getEmail())
                .flatMap(existingClient -> Mono.error(new EmailAlreadyRegisteredException("El correo electrónico ya está registrado.")))
                .switchIfEmpty(Mono.defer(() -> checkClientInExternalService(clientDTO)
                        .flatMap(existingClient -> {
                            // Si el cliente existe en el servicio externo, actualiza el estado
                            var client = new Client();
                            client.setName(existingClient.getName());
                            client.setEmail(existingClient.getEmail());
                            client.setStatus("exists");
                            return clientRepository.save(client);
                        })
                        .switchIfEmpty(createNewClient(clientDTO)) // Si no existe, crea un nuevo cliente
                ))
                .map(this::toClientDTO);
    }

    // Método para verificar cliente en servicio externo
    private Mono<Client> checkClientInExternalService(ClientDTO clientDTO) {
        return webClient.get()
                .uri("https://gorest.co.in/public/v2/users", uriBuilder -> uriBuilder
                        .queryParam("name", clientDTO.getName())
                        .queryParam("email", clientDTO.getEmail())
                        .build())
                .retrieve()
                .bodyToFlux(Client.class)
                .next(); // Toma el primer cliente que coincida
    }

    // Método para crear un nuevo cliente en caso de que no exista
    private Mono<Client> createNewClient(ClientDTO clientDTO) {
        var client = new Client();
        client.setName(clientDTO.getName());
        client.setEmail(clientDTO.getEmail());
        client.setGender(clientDTO.getGender());
        client.setStatus("active");
        return clientRepository.save(client);
    }

    // Excepción personalizada
    public static class EmailAlreadyRegisteredException extends RuntimeException {
        public EmailAlreadyRegisteredException(String message) {
            super(message);
        }
    }


    public ClientDTO toClientDTO(Object client) {
        if (client instanceof Client clientObj) {
            return new ClientDTO(
                    clientObj.getId(),
                    clientObj.getName(),
                    clientObj.getEmail(),
                    clientObj.getGender(),
                    clientObj.getStatus()
            );
        } else {
            throw new IllegalArgumentException("El objeto proporcionado no es una instancia de Client");
        }
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
                .switchIfEmpty(Mono.error(new DataIntegrityViolationException("Client no encontrado con id: " + id)))
                .flatMap(clientRepository::delete);
    }
}
