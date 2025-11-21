package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("course") // Esta anotación asocia explícitamente el modelo 'Course' con la tabla
                 // 'course' en la DB.
public class Course extends Model {

    // ActiveJDBC mapea automáticamente las columnas de la tabla 'course'
    // (como 'id', 'name', 'courseLoad'.) a los atributos de esta clase.
    // No necesitas declarar los campos (id, name, courseLoad) aquí como variables de
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

    public int getCourseLoad() {
        return getInteger("courseLoad"); // Obtiene el valor de la columna 'courseLoad'
    }

    public void setCourseLoad(int courseLoad) {
        set("courseLoad", courseLoad); // Establece el valor de la columna 'courseLoad'
    }

    /**
     * Obtiene el nombre completo de la materia "<name>(<id>)"
     */
    @Override
    public String toString() {
        return getName() + " (" + getID()+ ")"; // Combina el nombre y el id ej Ingenieria
    }                                           // Ej: "Ingenieria de Software (3305)"
}