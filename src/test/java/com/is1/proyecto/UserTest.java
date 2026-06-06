package com.is1.proyecto;

import com.is1.proyecto.config.DBConfigSingleton;
import com.is1.proyecto.models.User;
import org.javalite.activejdbc.Base;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @BeforeEach
    void setUp() {
        DBConfigSingleton.getInstance().openConnection();
        Base.exec("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL UNIQUE, " +
                "password TEXT NOT NULL, " +
                "role TEXT NOT NULL CHECK(role IN ('ADMIN', 'PROFESSOR', 'STUDENT'))" +
                ")");
    }

    @AfterEach
    void tearDown() {
        Base.exec("DROP TABLE IF EXISTS users");
        DBConfigSingleton.getInstance().closeConnection();
    }

    @Test /* Verifica que todos los campos se guarden correctamente */
    void testCrearUsuarioValido() {
        User user = new User();
        user.setName("agustin");
        user.setPassword("1234");
        user.setRol("STUDENT");
        user.saveIt();

        User encontrado = User.findFirst("name = ?", "agustin");

        assertNotNull(encontrado);
        assertEquals("agustin", encontrado.getName());
        assertEquals("1234", encontrado.getPassword());
        assertEquals("STUDENT", encontrado.getRol());
    }

    @Test /* Verifica que la búsqueda por nombre funciona y devuelve algo. */
    void testRecuperarUsuarioPorNombre() {
        User user = new User();
        user.setName("maria");
        user.setPassword("abcd");
        user.setRol("PROFESSOR");
        user.saveIt();

        User encontrado = User.findFirst("name = ?", "maria");

        assertNotNull(encontrado);
        assertEquals("maria", encontrado.getName());
    }

    @Test /* Verifica que cuando intentás guardar un usuario con rol inválido, el sistema lanza una excepción */
    void testRolInvalido() {
        User user = new User();
        user.setName("carlos");
        user.setPassword("1234");
        user.setRol("INVALIDO");

        assertThrows(Exception.class, () -> user.saveIt());
    }

    @Test /* Verifica que no haya dos usuarios con el mismo nombre */
    void testNameUnico() {
        User user1 = new User();
        user1.setName("pedro");
        user1.setPassword("1234");
        user1.setRol("STUDENT");
        user1.saveIt();

        User user2 = new User();
        user2.setName("pedro");
        user2.setPassword("5678");
        user2.setRol("ADMIN");

        assertThrows(Exception.class, () -> user2.saveIt());
    }

    @Test /* Verifico que no haya campos NULL */
    void testCamposNotNull() {
        User user = new User();
        user.setName(null);
        user.setPassword(null);
        user.setRol(null);

        assertThrows(Exception.class, () -> user.saveIt());
    }

}