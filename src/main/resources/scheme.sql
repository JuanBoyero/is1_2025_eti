-- Elimina la tabla 'users' si ya existe para asegurar un inicio limpio
DROP TABLE IF EXISTS users;

-- Crea la tabla 'users' con los campos originales, adaptados para SQLite
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT, -- Clave primaria autoincremental para SQLite
    name TEXT NOT NULL UNIQUE, -- Nombre de usuario (TEXT es el tipo de cadena recomendado para SQLite), con restricción UNIQUE
    password TEXT NOT NULL, -- Contraseña hasheada (TEXT es el tipo de cadena recomendado para SQLite)
    role TEXT NOT NULL CHECK(role IN ('ADMIN', 'PROFESSOR', 'STUDENT'))
);

-- Elimina la tabla 'professor' si ya existe para asegurar un inicio limpio
DROP TABLE IF EXISTS professor;

-- Crea la tabla 'professor' con los campos originales, adaptados para SQLite
CREATE TABLE professor (
    id INTEGER PRIMARY KEY AUTOINCREMENT, 
    user_id INTEGER UNIQUE, -- NUEVO: Clave foránea para saber cuál es su cuenta de login
    email TEXT NOT NULL UNIQUE, 
    name TEXT NOT NULL,      
    surname TEXT NOT NULL, 
    dni VARCHAR(8) NOT NULL UNIQUE, 
    FOREIGN KEY (user_id) REFERENCES users(id) -- Conexión lógica
);

-- Elimina la tabla 'student' si ya existe para asegurar un inicio limpio
DROP TABLE IF EXISTS student;

-- Crea la tabla 'student'
CREATE TABLE student (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER UNIQUE, -- NUEVO: Clave foránea para saber cuál es su cuenta de login
    email TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    surname TEXT NOT NULL,
    dni VARCHAR(8) NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Elimina la tabla 'course' si ya existe para asegurar un inicio limpio
DROP TABLE IF EXISTS course;

-- Crea la tabla 'course' con los campos originales, adaptados para SQLite
CREATE TABLE course (
    id INTEGER PRIMARY KEY AUTOINCREMENT, -- Codigo de la materia Clave primaria autoincremental para SQLite
    name TEXT NOT NULL, -- nombre de la materia (TEXT es el tipo de cadena recomendado para SQLite)
    code TEXT NOT NULL UNIQUE, -- Codigo identificador de la materia (ej: IS1, MAT101)
    year INTEGER, -- Año/Nivel de la materia (ej: 1, 2, 3, 4, 5)
    courseLoad INTEGER -- Carga Horaria
);

-- Elimina la tabla 'course' si ya existe para asegurar un inicio limpio
DROP TABLE IF EXISTS dictated;

-- Crea la tabla 'dictated' con los campos originales, adaptados para SQLite
CREATE TABLE dictated (
    idCourse INTEGER NOT NULL,    -- id de la materia
    idProfessor INTEGER NOT NULL, -- id del profesor
    
    --Clave Primaria Compuesta
    PRIMARY KEY (idCourse, idProfessor),
    
    --Claves Foráneas
    FOREIGN KEY (idCourse) REFERENCES course(id), -- Fk a course
    FOREIGN KEY (idProfessor) REFERENCES professor(id) -- Fk a professor
);

-- Tabla intermedia para inscripciones (Materia cursada por Alumno)
DROP TABLE IF EXISTS enrollment;
CREATE TABLE enrollment (
    idCourse INTEGER NOT NULL,
    idStudent INTEGER NOT NULL,
    PRIMARY KEY (idCourse, idStudent),
    FOREIGN KEY (idCourse) REFERENCES course(id),
    FOREIGN KEY (idStudent) REFERENCES student(id)
);
