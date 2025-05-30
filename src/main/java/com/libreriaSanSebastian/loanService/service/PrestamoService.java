package com.libreriaSanSebastian.loanService.service;

import com.libreriaSanSebastian.loanService.modelo.Prestamo;
import com.libreriaSanSebastian.loanService.repository.PrestamoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Optional;

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
}