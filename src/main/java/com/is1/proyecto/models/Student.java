package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("student") 
public class Student extends Model {

    public Integer getID() { return getInteger("id"); }
    
    public Integer getUserId() { return getInteger("user_id"); }
    public void setUserId(Integer userId) { set("user_id", userId); }

    public String getName() { return getString("name"); }
    public void setName(String name) { set("name", name); }

    public String getSurname() { return getString("surname"); }
    public void setSurname(String surname) { set("surname", surname); }

    public String getEmail() { return getString("email"); }
    public void setEmail(String email) { set("email", email); }

    public String getDni() { return getString("dni"); }
    public void setDni(String dni) { set("dni", dni); }

    public String getNombreCompleto() {
        return getName() + " " + getSurname(); 
    }
}
