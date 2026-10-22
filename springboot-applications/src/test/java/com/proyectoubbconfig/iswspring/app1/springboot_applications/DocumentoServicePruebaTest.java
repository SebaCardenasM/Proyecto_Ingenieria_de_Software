package com.proyectoubbconfig.iswspring.app1.springboot_applications;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.DocumentoService;

@SpringBootTest
class DocumentoServicePruebaTest {

    @Autowired
    private DocumentoService documentoService;

    @Test
    void testServiceNotNull() {
        assertNotNull(documentoService, "El servicio real DocumentoService debe inyectarse correctamente");
    }
}