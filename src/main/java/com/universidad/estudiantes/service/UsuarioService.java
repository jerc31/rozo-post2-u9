package com.universidad.estudiantes.service;

import com.universidad.estudiantes.model.Usuario;
import com.universidad.estudiantes.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Transactional
    public void registrar(Usuario usuario) {
        if (repo.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El correo ya esta registrado");
        }
        usuario.setContrasenia(encoder.encode(usuario.getContrasenia()));
        usuario.setRol("ROLE_USER");
        repo.save(usuario);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Usuario> listarTodos() {
        return repo.findAll();
    }

    @PreAuthorize("hasRole('ADMIN') or #email == authentication.name")
    public Optional<Usuario> buscarPorEmail(String email) {
        return repo.findByEmail(email);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @Transactional
    public void cambiarRol(Long id, String nuevoRol) {
        Usuario u = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        u.setRol(nuevoRol);
    }

    @PreAuthorize("#usuario.email == authentication.name or hasRole('ADMIN')")
    @Transactional
    public void actualizarNombre(Usuario usuario) {
        Usuario existente = repo.findById(usuario.getId())
            .orElseThrow(() -> new RuntimeException("No encontrado"));
        existente.setNombre(usuario.getNombre());
    }
}
