package com.libreriaSanSebastian.loanService.service;

import com.libreriaSanSebastian.loanService.modelo.Prestamo;
import com.libreriaSanSebastian.loanService.repository.PrestamoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PrestamoService {

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private RestTemplate restTemplate;

    // URLs de los microservicios
    private static final String USER_SERVICE_URL = "http://localhost:8081";
    private static final String BOOK_SERVICE_URL = "http://localhost:8080";

    public List<Prestamo> listarTodos() {
        return prestamoRepository.findAll();
    }

    public Optional<Prestamo> buscarPorId(Long id) {
        return prestamoRepository.findById(id);
    }

    public List<Prestamo> buscarPorUsuario(Long usuarioId) {
        return prestamoRepository.findByUsuarioId(usuarioId);
    }

    public Prestamo crearPrestamo(Prestamo prestamo) {
        // Validar entrada
        if (prestamo.getUsuarioId() == null || prestamo.getLibroId() == null) {
            throw new RuntimeException("Usuario ID y Libro ID son requeridos");
        }
        // Verificar que el usuario existe
        if (!verificarUsuarioExiste(prestamo.getUsuarioId())) {
            throw new RuntimeException("Usuario no encontrado con ID: " + prestamo.getUsuarioId());
        }
        // Verificar que el libro existe y tiene stock disponible
        if (!verificarLibroDisponible(prestamo.getLibroId())) {
            throw new RuntimeException("Libro no disponible o no encontrado con ID: " + prestamo.getLibroId());
        }
        // Decrementar stock del libro
        if (!decrementarStockLibro(prestamo.getLibroId())) {
            throw new RuntimeException("No se pudo decrementar el stock del libro");
        }
        // Crear el préstamo
        return prestamoRepository.save(prestamo);
    }

    private boolean verificarUsuarioExiste(Long usuarioId) {
        try {
            String userUrl = USER_SERVICE_URL + "/api/v1/usuarios/" + usuarioId;
            ResponseEntity<Object> response = restTemplate.getForEntity(userUrl, Object.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (RestClientException e) {
            System.err.println("Error al verificar usuario: " + e.getMessage());
            return false;
        }
    }

    private boolean verificarLibroDisponible(Long libroId) {
        try {
            String bookUrl = BOOK_SERVICE_URL + "/api/v1/libros/" + libroId;
            ResponseEntity<Object> response = restTemplate.getForEntity(bookUrl, Object.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (RestClientException e) {
            System.err.println("Error al verificar libro: " + e.getMessage());
            return false;
        }
    }

    private boolean decrementarStockLibro(Long libroId) {
        try {
            String decrementUrl = BOOK_SERVICE_URL + "/api/v1/libros/decrementar-stock/" + libroId;

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Void> response = restTemplate.exchange(
                    decrementUrl,
                    HttpMethod.PUT,
                    entity,
                    Void.class
            );

            return response.getStatusCode() == HttpStatus.OK;
        } catch (RestClientException e) {
            System.err.println("Error al decrementar stock: " + e.getMessage());
            return false;
        }
    }

    public Prestamo devolverLibro(Long id) {
        Optional<Prestamo> prestamoOpt = prestamoRepository.findById(id);
        if (prestamoOpt.isPresent()) {
            Prestamo prestamo = prestamoOpt.get();
            if ("DEVUELTO".equals(prestamo.getEstado())) {
                throw new RuntimeException("El libro ya fue devuelto");
            }
            prestamo.setEstado("DEVUELTO");
            prestamo.setFechaDevolucion(new Date());
            return prestamoRepository.save(prestamo);
        }
        throw new RuntimeException("Préstamo no encontrado");
    }

    public void eliminar(Long id) {
        prestamoRepository.deleteById(id);
    }
}