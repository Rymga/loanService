package com.libreriaSanSebastian.loanService.service;

import com.libreriaSanSebastian.loanService.modelo.Prestamo;
import com.libreriaSanSebastian.loanService.repository.PrestamoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Optional;
//
@Service
public class PrestamoService {

    @Autowired
    private PrestamoRepository prestamoRepository;

    private RestTemplate restTemplate = new RestTemplate();

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
        // Verificar que el usuario existe
        try {
            String userUrl = "http://localhost:8082/api/v1/usuarios/" + prestamo.getUsuarioId();
            restTemplate.getForObject(userUrl, Object.class);
        } catch (Exception e) {
            throw new RuntimeException("Usuario no encontrado");
        }

        // Verificar que el libro existe y decrementar stock
        try {
            String bookUrl = "http://localhost:8081/api/v1/libros/" + prestamo.getLibroId();
            restTemplate.getForObject(bookUrl, Object.class);

            String decrementUrl = "http://localhost:8081/api/v1/libros/decrementar-stock/" + prestamo.getLibroId();
            restTemplate.patchForObject(decrementUrl, null, Void.class);
        } catch (Exception e) {
            throw new RuntimeException("Libro no disponible o no encontrado");
        }

        return prestamoRepository.save(prestamo);
    }
}