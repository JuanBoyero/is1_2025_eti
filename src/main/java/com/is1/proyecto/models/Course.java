package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("course")
public class Course extends Model {

    public Integer getID() {
        return getInteger("id");
    }

    public String getName() {
        return getString("name");
    }

    public void setName(String name) {
        set("name", name);
    }

    public String getCode() {
        return getString("code");
    }

    public void setCode(String code) {
        set("code", code);
    }

    public Integer getYear() {
        return getInteger("year");
    }

    public void setYear(Integer year) {
        set("year", year);
    }

    public int getCourseLoad() {
        return getInteger("courseLoad");
    }

    public void setCourseLoad(int courseLoad) {
        set("courseLoad", courseLoad);
    }

    @Override
    public String toString() {
        return getName() + " (" + getID() + ")";
    }
}