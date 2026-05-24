package com.spa.bookingapp;

import com.spa.bookingapp.model.SpaService;
import com.spa.bookingapp.model.Role;
import com.spa.bookingapp.model.User;
import com.spa.bookingapp.repository.AppointmentRepository;
import com.spa.bookingapp.repository.SpaServiceRepository;
import com.spa.bookingapp.repository.RoleRepository;
import com.spa.bookingapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    private final SpaServiceRepository spaServiceRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppointmentRepository appointmentRepository;

    public DataSeeder(SpaServiceRepository spaServiceRepository, RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder, AppointmentRepository appointmentRepository) {
        this.spaServiceRepository = spaServiceRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Limpiar citas corruptas (sin hora de fin) que bloquean el sistema
        appointmentRepository.findAll().stream()
            .filter(a -> a.getAppointmentEndTime() == null)
            .forEach(a -> appointmentRepository.delete(a));

        // Seed Roles if they don't exist
        if (!roleRepository.findByName("ROLE_CLIENT").isPresent()) {
            roleRepository.save(new Role("ROLE_CLIENT"));
        }
        if (!roleRepository.findByName("ROLE_THERAPIST").isPresent()) {
            roleRepository.save(new Role("ROLE_THERAPIST"));
        }
        if (!roleRepository.findByName("ROLE_ADMIN").isPresent()) {
            roleRepository.save(new Role("ROLE_ADMIN"));
        }

        // Seed Default Therapist
        if (!userRepository.existsByUsername("terapeuta1")) {
            User therapist = new User("Juan Perez", "terapeuta1", "juan@spa.com", passwordEncoder.encode("123456"));
            Set<Role> roles = new HashSet<>();
            Role userRole = roleRepository.findByName("ROLE_THERAPIST").orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
            therapist.setRoles(roles);
            userRepository.save(therapist);
        }

        if (spaServiceRepository.count() == 0) {
            System.out.println("Seeding database with default services...");
            
            // Masajes Relajantes
            spaServiceRepository.save(new SpaService("Masaje Tradicional", "Movimientos suaves con aceites aromáticos para eliminar el estrés y mejorar la circulación general.", new BigDecimal("80.00"), 60, "Masajes Relajantes", "img/service_masaje_tradicional.png"));
            spaServiceRepository.save(new SpaService("Terapia de Piedras Volcánicas Calientes", "Piedras lisas de origen volcánico dispuestas en puntos clave del cuerpo para relajar los músculos más profundos mediante calor terapéutico.", new BigDecimal("120.00"), 90, "Masajes Relajantes", "img/service_piedras.png"));
            spaServiceRepository.save(new SpaService("Aromaterapia de Lavanda y Manzanilla", "Masaje manual ultrasuave combinado con la difusión e inhalación de aceites esenciales puros para calmar el sistema nervioso.", new BigDecimal("95.00"), 60, "Masajes Relajantes", null));
            spaServiceRepository.save(new SpaService("Masaje Descontracturante Profundo", "Enfocado en liberar nudos y tensiones acumuladas en la espalda, cuello y hombros debido a las malas posturas o la rutina diaria.", new BigDecimal("110.00"), 60, "Masajes Relajantes", null));
            
            // Tratamientos Faciales
            spaServiceRepository.save(new SpaService("Limpieza Facial Profunda con Ozono", "Extracción de impurezas, exfoliación y aplicación de vapor de ozono para purificar los poros y oxigenar la piel.", new BigDecimal("60.00"), 45, "Tratamientos Faciales", "img/service_limpieza.png"));
            spaServiceRepository.save(new SpaService("Hidratación Extrema con Ácido Hialurónico", "Tratamiento intensivo para pieles deshidratadas o cansadas, devolviendo la elasticidad y un brillo natural.", new BigDecimal("90.00"), 60, "Tratamientos Faciales", "img/service_hidratacion.png"));
            spaServiceRepository.save(new SpaService("Facial Antioxidante de Vitamina C", "Ideal para unificar el tono de la piel, combatir los radicales libres y aportar un efecto de luminosidad y juventud inmediato.", new BigDecimal("85.00"), 60, "Tratamientos Faciales", null));
            spaServiceRepository.save(new SpaService("Mascarilla Botánica Calmante", "Especial para pieles sensibles, utilizando extractos de aloe vera, caléndula y té verde para desinflamar y refrescar.", new BigDecimal("55.00"), 40, "Tratamientos Faciales", null));
            
            // Terapias Corporales
            spaServiceRepository.save(new SpaService("Exfoliación Corporal de Coco y Café", "Eliminación de células muertas mediante un liquido natural que activa la circulación y deja la piel increíblemente suave y renovada.", new BigDecimal("70.00"), 45, "Terapias Corporales", "img/service_exfoliacion.png"));
            spaServiceRepository.save(new SpaService("Envoltura Nutritiva de Fango Mineral", "Aplicación de arcillas ricas en minerales por todo el cuerpo, seguida de una manta térmica para desintoxicar los tejidos y suavizar la piel.", new BigDecimal("100.00"), 60, "Terapias Corporales", "img/service_fango.png"));
            spaServiceRepository.save(new SpaService("Chocolaterapia Hidratante", "Envoltura a base de cacao puro que estimula la producción de endorfinas (las hormonas de la felicidad), mientras hidrata y reafirma profundamente.", new BigDecimal("110.00"), 60, "Terapias Corporales", null));
            spaServiceRepository.save(new SpaService("Baño de Hidromasaje Herbal", "Inmersión en jacuzzi con espuma y extractos de plantas medicinales para una relajación muscular total antes o después de cualquier masaje.", new BigDecimal("80.00"), 45, "Terapias Corporales", null));
            
            System.out.println("Default services seeded successfully.");
        }
    }
}
