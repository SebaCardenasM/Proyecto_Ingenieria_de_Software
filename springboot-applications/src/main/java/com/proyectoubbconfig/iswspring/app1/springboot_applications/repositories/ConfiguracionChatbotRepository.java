package com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.ConfiguracionChatbot;

@Repository
public interface ConfiguracionChatbotRepository extends JpaRepository<ConfiguracionChatbot, String> {
}
