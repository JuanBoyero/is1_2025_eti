-- Elimina la tabla 'users' si ya existe para asegurar un inicio limpio
DROP TABLE IF EXISTS users;

-- Crea la tabla 'users' con los campos originales, adaptados para SQLite
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT, -- Clave primaria autoincremental para SQLite
    name TEXT NOT NULL UNIQUE,          -- Nombre de usuario (TEXT es el tipo de cadena recomendado para SQLite), con restricción UNIQUE
    password TEXT NOT NULL           -- Contraseña hasheada (TEXT es el tipo de cadena recomendado para SQLite)
);

-- Elimina la tabla 'users' si ya existe para asegurar un inicio limpio
DROP TABLE IF EXISTS professor;

-- Crea la tabla 'users' con los campos originales, adaptados para SQLite
CREATE TABLE professor (
    id INTEGER PRIMARY KEY AUTOINCREMENT, -- Clave primaria autoincremental para SQLite
    name TEXT NOT NULL UNIQUE,          -- Nombre de usuario (TEXT es el tipo de cadena recomendado para SQLite), con restricción UNIQUE
    password TEXT NOT NULL           -- Contraseña hasheada (TEXT es el tipo de cadena recomendado para SQLite)
    nombre TEXT NOT NULL,
    apellido TEXT NOT NULL, 
    correo TEXT NOT NULL,
    dni TEXT NOT NULL,

    CONSTRAINT fk_id_professor FOREIGN KEY (id) REFERENCES users(id),
    CONSTRAINT fk_name_professor FOREIGN KEY (name) REFERENCES users(name),
    CONSTRAINT fk_password_professor FOREIGN KEY (password) REFERENCES users(password)
    );