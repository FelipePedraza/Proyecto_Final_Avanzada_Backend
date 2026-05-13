package co.edu.uniquindio.application.config;

import co.edu.uniquindio.application.repositories.AlojamientoRepositorio;
import co.edu.uniquindio.application.repositories.UsuarioRepositorio;
import co.edu.uniquindio.application.models.enums.Estado;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomMetricsConfig {

    @Bean
    public Counter reservasCreadasCounter(MeterRegistry registry) {
        return Counter.builder("reservas.creadas").description("Total de reservas creadas").register(registry);
    }

    @Bean
    public Counter reservasAceptadasCounter(MeterRegistry registry) {
        return Counter.builder("reservas.aceptadas").description("Total de reservas aceptadas").register(registry);
    }

    @Bean
    public Counter reservasRechazadasCounter(MeterRegistry registry) {
        return Counter.builder("reservas.rechazadas").description("Total de reservas rechazadas").register(registry);
    }

    @Bean
    public Counter reservasCanceladasCounter(MeterRegistry registry) {
        return Counter.builder("reservas.canceladas").description("Total de reservas canceladas").register(registry);
    }

    @Bean
    public Counter alojamientosCreadosCounter(MeterRegistry registry) {
        return Counter.builder("alojamientos.creados").description("Total de alojamientos creados").register(registry);
    }

    @Bean
    public Counter alojamientosEliminadosCounter(MeterRegistry registry) {
        return Counter.builder("alojamientos.eliminados").description("Total de alojamientos eliminados").register(registry);
    }

    @Bean
    public Counter pagosExitososCounter(MeterRegistry registry) {
        return Counter.builder("pagos.exitosos").description("Total de pagos exitosos").register(registry);
    }

    @Bean
    public Counter pagosFallidosCounter(MeterRegistry registry) {
        return Counter.builder("pagos.fallidos").description("Total de pagos fallidos").register(registry);
    }

    @Bean
    public Counter authLoginExitosoCounter(MeterRegistry registry) {
        return Counter.builder("auth.login.exitoso").description("Total de logins exitosos").register(registry);
    }

    @Bean
    public Counter authLoginFallidoCounter(MeterRegistry registry) {
        return Counter.builder("auth.login.fallido").description("Total de logins fallidos").register(registry);
    }

    @Bean
    public Counter authRegistroExitosoCounter(MeterRegistry registry) {
        return Counter.builder("auth.registro.exitoso").description("Total de registros exitosos").register(registry);
    }

    @Bean
    public Counter authRecuperacionSolicitadaCounter(MeterRegistry registry) {
        return Counter.builder("auth.recuperacion.solicitada").description("Total de solicitudes de recuperacion de contrasena").register(registry);
    }

    @Bean
    public Counter mensajesChatEnviadosCounter(MeterRegistry registry) {
        return Counter.builder("chat.mensajes.enviados").description("Total de mensajes de chat enviados").register(registry);
    }

    @Bean
    public Gauge alojamientosActivosGauge(MeterRegistry registry, AlojamientoRepositorio repo) {
        return Gauge.builder("alojamientos.activos", () -> repo.countByEstado(Estado.ACTIVO))
                .description("Cantidad de alojamientos activos").register(registry);
    }

    @Bean
    public Gauge usuariosRegistradosGauge(MeterRegistry registry, UsuarioRepositorio repo) {
        return Gauge.builder("usuarios.registrados", () -> repo.countByEstado(Estado.ACTIVO))
                .description("Cantidad de usuarios registrados activos").register(registry);
    }
}
