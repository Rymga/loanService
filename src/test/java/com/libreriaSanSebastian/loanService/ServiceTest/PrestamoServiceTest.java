package com.libreriaSanSebastian.loanService.ServiceTest;

import com.libreriaSanSebastian.loanService.service.PrestamoService;
import com.libreriaSanSebastian.loanService.modelo.Prestamo;
import com.libreriaSanSebastian.loanService.repository.PrestamoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrestamoServiceTest {

    @Mock
    private PrestamoRepository prestamoRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PrestamoService prestamoService;

    private Prestamo prestamo;

    @BeforeEach
    void setUp() {
        prestamo = new Prestamo();
        prestamo.setId(1L);
        prestamo.setUsuarioId(1L);
        prestamo.setLibroId(1L);
        prestamo.setFechaPrestamo(new Date());
        prestamo.setEstado("ACTIVO");
    }

    @Test
    void testListarTodos() {
        // Given
        List<Prestamo> prestamos = Arrays.asList(prestamo);
        when(prestamoRepository.findAll()).thenReturn(prestamos);

        // When
        List<Prestamo> resultado = prestamoService.listarTodos();

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(prestamo.getId(), resultado.get(0).getId());
        verify(prestamoRepository, times(1)).findAll();
    }

    @Test
    void testBuscarPorId_Existente() {
        // Given
        when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));

        // When
        Optional<Prestamo> resultado = prestamoService.buscarPorId(1L);

        // Then
        assertTrue(resultado.isPresent());
        assertEquals(prestamo.getId(), resultado.get().getId());
        verify(prestamoRepository, times(1)).findById(1L);
    }

    @Test
    void testBuscarPorId_NoExistente() {
        // Given
        when(prestamoRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<Prestamo> resultado = prestamoService.buscarPorId(1L);

        // Then
        assertFalse(resultado.isPresent());
        verify(prestamoRepository, times(1)).findById(1L);
    }

    @Test
    void testBuscarPorUsuario() {
        // Given
        List<Prestamo> prestamos = Arrays.asList(prestamo);
        when(prestamoRepository.findByUsuarioId(1L)).thenReturn(prestamos);

        // When
        List<Prestamo> resultado = prestamoService.buscarPorUsuario(1L);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(prestamo.getUsuarioId(), resultado.get(0).getUsuarioId());
        verify(prestamoRepository, times(1)).findByUsuarioId(1L);
    }

    @Test
    void testCrearPrestamo_Exitoso() {
        // Given
        Prestamo nuevoPrestamo = new Prestamo();
        nuevoPrestamo.setUsuarioId(1L);
        nuevoPrestamo.setLibroId(1L);

        // Mock para verificar usuario existe
        when(restTemplate.getForEntity(anyString(), eq(Object.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // Mock para decrementar stock
        when(restTemplate.exchange(anyString(), any(), any(), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        when(prestamoRepository.save(any(Prestamo.class))).thenReturn(prestamo);

        // When
        Prestamo resultado = prestamoService.crearPrestamo(nuevoPrestamo);

        // Then
        assertNotNull(resultado);
        assertEquals(prestamo.getId(), resultado.getId());
        verify(prestamoRepository, times(1)).save(any(Prestamo.class));
    }

    @Test
    void testCrearPrestamo_SinUsuarioId() {
        // Given
        Prestamo nuevoPrestamo = new Prestamo();
        nuevoPrestamo.setLibroId(1L);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> prestamoService.crearPrestamo(nuevoPrestamo));
        
        assertEquals("Usuario ID y Libro ID son requeridos", exception.getMessage());
        verify(prestamoRepository, never()).save(any());
    }

    @Test
    void testCrearPrestamo_SinLibroId() {
        // Given
        Prestamo nuevoPrestamo = new Prestamo();
        nuevoPrestamo.setUsuarioId(1L);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> prestamoService.crearPrestamo(nuevoPrestamo));
        
        assertEquals("Usuario ID y Libro ID son requeridos", exception.getMessage());
        verify(prestamoRepository, never()).save(any());
    }

    @Test
    void testDevolverLibro_Exitoso() {
        // Given
        when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));
        when(prestamoRepository.save(any(Prestamo.class))).thenReturn(prestamo);

        // When
        Prestamo resultado = prestamoService.devolverLibro(1L);

        // Then
        assertNotNull(resultado);
        assertEquals("DEVUELTO", resultado.getEstado());
        assertNotNull(resultado.getFechaDevolucion());
        verify(prestamoRepository, times(1)).save(prestamo);
    }

    @Test
    void testDevolverLibro_YaDevuelto() {
        // Given
        prestamo.setEstado("DEVUELTO");
        when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> prestamoService.devolverLibro(1L));
        
        assertEquals("El libro ya fue devuelto", exception.getMessage());
        verify(prestamoRepository, never()).save(any());
    }

    @Test
    void testDevolverLibro_NoEncontrado() {
        // Given
        when(prestamoRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> prestamoService.devolverLibro(1L));
        
        assertEquals("Préstamo no encontrado", exception.getMessage());
        verify(prestamoRepository, never()).save(any());
    }

    @Test
    void testEliminar() {
        // Given
        doNothing().when(prestamoRepository).deleteById(1L);

        // When
        prestamoService.eliminar(1L);

        // Then
        verify(prestamoRepository, times(1)).deleteById(1L);
    }
}