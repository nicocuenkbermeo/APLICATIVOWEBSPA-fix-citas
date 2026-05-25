package com.spa.bookingapp.controller;

import com.spa.bookingapp.model.Role;
import com.spa.bookingapp.model.User;
import com.spa.bookingapp.payload.response.MessageResponse;
import com.spa.bookingapp.repository.RoleRepository;
import com.spa.bookingapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @GetMapping("/therapists")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public List<Map<String, Object>> getTherapists() {
        List<User> therapists = userRepository.findByRoles_Name("ROLE_THERAPIST");
        return therapists.stream().map(t -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", t.getId());
            map.put("name", t.getName());
            map.put("username", t.getUsername());
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * Lista todos los usuarios del sistema con sus roles actuales.
     * Solo accesible para ADMIN — usado por el dashboard admin para gestion de roles.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String, Object>> getAllUsers() {
        return userRepository.findAll().stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("name", u.getName());
            map.put("username", u.getUsername());
            map.put("email", u.getEmail());
            map.put("roles", u.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList()));
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * Cambia el conjunto de roles de un usuario. Solo ADMIN puede invocar este endpoint.
     *
     * Body esperado: { "roles": ["ROLE_CLIENT", "ROLE_THERAPIST"] }
     * o equivalentes cortos: { "roles": ["client", "therapist"] }
     *
     * Reglas de negocio aplicadas:
     *  - El usuario destino debe existir.
     *  - Todos los roles enviados deben existir en la tabla roles.
     *  - Se reemplaza el set completo de roles (no se hace merge), comportamiento
     *    explicito para que el admin tenga control total y predecible.
     *  - Un ADMIN no puede quitarse a si mismo el rol ADMIN — proteccion para no
     *    quedar el sistema sin administradores accidentalmente.
     */
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRoles(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            org.springframework.security.core.Authentication auth) {

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404)
                    .body(new MessageResponse("Usuario no encontrado."));
        }

        Object rawRoles = body.get("roles");
        if (!(rawRoles instanceof List)) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("El campo 'roles' debe ser un arreglo de nombres de rol."));
        }

        @SuppressWarnings("unchecked")
        List<String> requestedRoles = (List<String>) rawRoles;
        if (requestedRoles.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Debe asignarse al menos un rol al usuario."));
        }

        Set<Role> newRoles = new HashSet<>();
        for (String roleName : requestedRoles) {
            String normalized = normalizeRoleName(roleName);
            Role role = roleRepository.findByName(normalized).orElse(null);
            if (role == null) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Rol no valido: " + roleName +
                                ". Roles validos: ROLE_CLIENT, ROLE_THERAPIST, ROLE_ADMIN."));
            }
            newRoles.add(role);
        }

        // Proteccion: el admin no puede quitarse a si mismo el rol ADMIN
        boolean isSelf = auth != null && auth.getName().equals(user.getUsername());
        boolean keepsAdmin = newRoles.stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName()));
        if (isSelf && !keepsAdmin) {
            return ResponseEntity.status(403)
                    .body(new MessageResponse("Por seguridad, no podes quitarte el rol ADMIN a vos mismo."));
        }

        user.setRoles(newRoles);
        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Roles actualizados correctamente.");
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        response.put("newRoles", newRoles.stream().map(Role::getName).collect(Collectors.toList()));
        return ResponseEntity.ok(response);
    }

    /**
     * Normaliza nombres de rol para aceptar tanto formato corto ("admin") como
     * formato canonico Spring Security ("ROLE_ADMIN"). Esto simplifica el contrato
     * del API frontend sin sacrificar consistencia interna.
     */
    private String normalizeRoleName(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim().toUpperCase();
        if (trimmed.startsWith("ROLE_")) return trimmed;
        return "ROLE_" + trimmed;
    }
}
