package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("dictated") // Esta anotación asocia explícitamente el modelo 'Dictated' con la tabla
                 // 'dictated' en la DB.
public class Dictated extends Model {

}