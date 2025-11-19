-- Elimina la tabla 'users' si ya existe para asegurar un inicio limpio
DROP TABLE IF EXISTS users;

-- Crea la tabla 'users' con los campos originales, adaptados para SQLite
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT, -- Clave primaria autoincremental para SQLite
    name TEXT NOT NULL UNIQUE, -- Nombre de usuario (TEXT es el tipo de cadena recomendado para SQLite), con restricción UNIQUE
    password TEXT NOT NULL -- Contraseña hasheada (TEXT es el tipo de cadena recomendado para SQLite)
);

-- Elimina la tabla 'professor' si ya existe para asegurar un inicio limpio
DROP TABLE IF EXISTS professor;

-- Crea la tabla 'users' con los campos originales, adaptados para SQLite
CREATE TABLE professor (
    id INTEGER PRIMARY KEY AUTOINCREMENT, -- Clave primaria autoincremental para SQLite
    email TEXT NOT NULL UNIQUE, -- email de profesor (TEXT es el tipo de cadena recomendado para SQLite), con restricción UNIQUE
    name TEXT NOT NULL, -- Nombre del profesor     
    surname TEXT NOT NULL, -- Apellido del profesor
    dni VARCHAR(8) NOT NULL UNIQUE -- DNI del profesor 
);