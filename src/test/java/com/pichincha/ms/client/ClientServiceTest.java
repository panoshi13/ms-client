package com.pichincha.ms.client;

import com.pichincha.ms.client.dto.ClientDTO;
import com.pichincha.ms.client.model.Client;
import com.pichincha.ms.client.repository.ClientRepository;
import com.pichincha.ms.client.service.ClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {
    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    @Mock
    private WebClient webClient;

    @Test
    void testRegisterClient_EmailAlreadyRegistered() {
        // Datos de prueba
        ClientDTO clientDTO = new ClientDTO(1L,"John Doe", "john@example.com", "Male",null);

        // Simular que el correo ya está registrado en el repositorio
        when(clientRepository.findByEmail(clientDTO.getEmail()))
                .thenReturn(Mono.just(new Client(1L,"John Doe", "john@example.com", "Male","active")));

        // Llamar al método
        Mono<ClientDTO> result = clientService.registerClient(clientDTO);

        // Verificar que el resultado es un error de tipo EmailAlreadyRegisteredException
        StepVerifier.create(result)
                .expectError(ClientService.EmailAlreadyRegisteredException.class)
                .verify();

        // Verificar que el repositorio fue consultado correctamente
        verify(clientRepository, times(1)).findByEmail(clientDTO.getEmail());
        verify(clientRepository, times(0)).save(any(Client.class)); // No debe guardar si el correo ya está registrado
    }

    @Test
    void testUpdateClient() {
        // Datos de prueba
        Long id = 1L;
        ClientDTO updatedClientDTO = new ClientDTO(1L,"Updated Name", "updated@example.com", "Male","active");

        // Crear un cliente simulado que se va a encontrar en el repositorio
        Client existingClient = new Client(1L,"Old Name", "old@example.com", "Female","active");
        existingClient.setId(id);

        // Simular que el cliente se encuentra en el repositorio
        when(clientRepository.findById(id)).thenReturn(Mono.just(existingClient));

        // Simular que después de actualizar el cliente se guarda en el repositorio
        when(clientRepository.save(any(Client.class))).thenReturn(Mono.just(existingClient));

        // Llamar al método que estamos probando
        Mono<ClientDTO> result = clientService.updateClient(id, updatedClientDTO);

        // Verificar el comportamiento con StepVerifier
        StepVerifier.create(result)
                .expectNextMatches(clientDTO ->
                        clientDTO.getName().equals("Updated Name") &&
                                clientDTO.getEmail().equals("updated@example.com") &&
                                clientDTO.getGender().equals("Male"))
                .verifyComplete();

        // Verificar que el repositorio fue llamado correctamente
        verify(clientRepository, times(1)).findById(id);
        verify(clientRepository, times(1)).save(any(Client.class));
    }


    @Test
    void testSearchClients() {
        // Datos de prueba
        String name = "John";
        String email = "john@example.com";

        // Crear un Flux de clientes simulados
        Client client1 = new Client(1L,"John Doe", "john@example.com","male","active");
        Client client2 = new Client(1L,"Jane Doe", "jane@example.com","male","active");

        // Simular el comportamiento del repositorio
        when(clientRepository.findAllByNameContainingOrEmailContaining(name, email))
                .thenReturn(Flux.just(client1, client2));

        // Llamar al método que estamos probando
        Flux<ClientDTO> result = clientService.searchClients(name, email);

        // Verificar el comportamiento con StepVerifier
        StepVerifier.create(result)
                .expectNextMatches(clientDTO -> clientDTO.getName().equals("John Doe") && clientDTO.getEmail().equals("john@example.com"))
                .expectNextMatches(clientDTO -> clientDTO.getName().equals("Jane Doe") && clientDTO.getEmail().equals("jane@example.com"))
                .verifyComplete();

        // Verificar que el repositorio fue llamado correctamente
        verify(clientRepository, times(1)).findAllByNameContainingOrEmailContaining(name, email);
    }

    @Test
    void deleteClient_WhenClientExists_ShouldDeleteSuccessfully() {
        // Arrange
        Long clientId = 1L;
        Client mockClient = new Client();
        mockClient.setId(clientId);

        when(clientRepository.findById(clientId)).thenReturn(Mono.just(mockClient));
        when(clientRepository.delete(mockClient)).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(clientService.deleteClient(clientId))
                .expectSubscription()
                .verifyComplete();

        // Assert
        verify(clientRepository).findById(clientId);
        verify(clientRepository).delete(mockClient);
    }

    @Test
    void deleteClient_WhenClientDoesNotExist_ShouldThrowException() {
        // Arrange
        Long clientId = 2L;

        when(clientRepository.findById(clientId))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(clientService.deleteClient(clientId))
                .expectErrorMatches(throwable -> throwable instanceof DataIntegrityViolationException
                        && throwable.getMessage().equals("Client no encontrado con id: " + clientId))
                .verify();

        verify(clientRepository).findById(clientId);
        verify(clientRepository, Mockito.never()).delete(Mockito.any());
    }
}
