package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("professor") // Esta anotación asocia explícitamente el modelo 'Professor' con la tabla
                    // 'Professor' en la DB.
public class Professor extends Model {

    // ActiveJDBC mapea automáticamente las columnas de la tabla 'users'
    // (como 'id', 'name', 'password', etc.) a los atributos de esta clase.
    // No necesitas declarar los campos (id, name, password) aquí como variables de
    // instancia,
    // ya que la clase Model base se encarga de la interacción con la base de datos.

    // Opcional: Puedes agregar métodos getters y setters si prefieres un acceso más
    // tipado,
    // aunque los métodos genéricos de Model (getString(), set(), getInteger(),
    // etc.) ya funcionan.
    public Integer getID() {
        return getInteger("id"); // Obtiene el valor de la columna id
    }

    public String getName() {
        return getString("name"); // Obtiene el valor de la columna 'name'
    }

    public void setName(String name) {
        set("name", name); // Establece el valor de la columna 'name'
    }

    public String getEmail() {
        return getString("email"); // Obtiene el valor de la columna 'email'
    }

    public void setEmail(String email) {
        set("email", email); // Establece el valor de la columna 'email'
    }

    public String getSurname() {
        return getString("surname"); // Obtiene el valor de la columna 'surname'
    }

    public void setSurname(String surname) {
        set("surname", surname); // Establece el valor de la columna 'surname'
    }

    public String getDni() {
        return getString("dni"); // Obtiene el valor de la columna 'dni'
    }

    public void setDni(String dni) {
        set("dni", dni); // Establece el valor de la columna 'dni'
    }

    /**
     * Obtiene el nombre completo del profesor
     */
    public String getNombreCompleto() {
        return getName() + " " + getSurname(); // Combina el nombre real y el apellido
    }
}