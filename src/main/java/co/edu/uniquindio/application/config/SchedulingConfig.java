package co.edu.uniquindio.application.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Esta configuración habilita las tareas programadas (@Scheduled)
}