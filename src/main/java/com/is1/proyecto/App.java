package com.is1.proyecto; // Define el paquete de la aplicación, debe coincidir con la estructura de carpetas.

// Importaciones necesarias para la aplicación Spark
import java.util.ArrayList;
import java.util.HashMap; // Utilidad para serializar/deserializar objetos Java a/desde JSON.
import java.util.List;
import java.util.Map; // Importa los métodos estáticos principales de Spark (get, post, before, after, etc.).
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javalite.activejdbc.Base;
import org.mindrot.jbcrypt.BCrypt;

import com.fasterxml.jackson.databind.ObjectMapper; // Representa un modelo de datos y el nombre de la vista a renderizar.
import com.is1.proyecto.config.DBConfigSingleton; // Motor de plantillas Mustache para Spark.
import com.is1.proyecto.models.Course;
import com.is1.proyecto.models.Dictated;
import com.is1.proyecto.models.Enrollment;
import com.is1.proyecto.models.Professor; // Modelo de ActiveJDBC que representa la tabla 'Professor'. 
import com.is1.proyecto.models.Student;
import com.is1.proyecto.models.User; // Para crear mapas de datos (modelos para las plantillas).

import spark.ModelAndView; // Modelo de ActiveJDBC que representa la tabla 'Professor'.
import static spark.Spark.after; // Para crear mapas de datos (modelos para las plantillas).
import static spark.Spark.before; // Interfaz Map, utilizada para Map.of() o HashMap.
import static spark.Spark.exception; // Clase Singleton para la configuración de la base de datos.
import static spark.Spark.get; // Modelo de ActiveJDBC que representa la tabla 'users'.
import static spark.Spark.halt;
import static spark.Spark.internalServerError;
import static spark.Spark.notFound;
import static spark.Spark.port;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;



/**
 * Clase principal de la aplicación Spark.
 * Configura las rutas, filtros y el inicio del servidor web.
 */
public class App {

    // Instancia estática y final de ObjectMapper para la
    // serialización/deserialización JSON.
    // Se inicializa una sola vez para ser reutilizada en toda la aplicación.
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Método principal que se ejecuta al iniciar la aplicación.
     * Aquí se configuran todas las rutas y filtros de Spark.
     */
    public static void main(String[] args) {
        port(8080); // Configura el puerto en el que la aplicación Spark escuchará las peticiones
                    // (por defecto es 4567).
        
        // --- MANEJO GLOBAL DE ERRORES ---
        exception(Exception.class, (e, req, res) -> {
            System.err.println("ERROR GLOBAL NO CONTROLADO: " + e.getMessage());
            e.printStackTrace();

            //Si hay una conexion vieja se cierra para evitar inconvenientes
            try {
                if (Base.hasConnection()) {
                    Base.close();
                }
            } catch (Exception ex) {
                System.err.println("Error cerrando conexion en handler global: " + ex.getMessage());
            }

            res.status(500);

            // No pisar respuestas existentes
            if (res.body() != null && !res.body().isEmpty()) {
                return;
            }

            res.type("text/plain");
            res.body("Error interno del servidor");
            });

        // Manejo de rutas inexistentes (404)
        notFound((req, res) -> {
            res.type("text/plain");
            return "404 - Pagina no encontrada";
        });

        // Manejo de error interno (500)
        internalServerError((req, res) -> {
            res.type("text/plain");
            return "500 - Error interno del servidor";
        });
        

        // Obtener la instancia única del singleton de configuración de la base de
        // datos.
        DBConfigSingleton dbConfig = DBConfigSingleton.getInstance();

        // --- Filtro 'before' para gestionar la conexión a la base de datos ---
        // Este filtro se ejecuta antes de cada solicitud HTTP.
        before((req, res) -> {
            //Si hay una conexion vieja se cierra para evitar inconvenientes
            try {
                if (Base.hasConnection()) {
                Base.close();
            }

                // Abre una conexión a la base de datos utilizando las credenciales del
                // singleton.
                Base.open(dbConfig.getDriver(), dbConfig.getDbUrl(), dbConfig.getUser(), dbConfig.getPass());
                System.out.println(req.url());

            } catch (Exception e) {
                // Si ocurre un error al abrir la conexión, se registra y se detiene la
                // solicitud
                // con un código de estado 500 (Internal Server Error) y un mensaje JSON.
                System.err.println("Error al abrir conexión con ActiveJDBC: " + e.getMessage());
                halt(500, "{\"error\": \"Error interno del servidor: Fallo al conectar a la base de datos.\"}"
                        + e.getMessage());
            }
        });

        // --- FILTROS DE SEGURIDAD RBAC ---

        before("/admin/*", (req, res) -> {
            String rol = req.session().attribute("user_role");
            if (rol == null || !rol.equals("ADMIN")) {
                System.out.println("DEBUG: Intento de acceso denegado a ruta ADMIN");
                res.redirect("/login?error=Acceso denegado. Permisos de Administrador requeridos.");
                halt(401);
            }
        });

        before("/profesor/*", (req, res) -> {
            String rol = req.session().attribute("user_role");
            if (rol == null || !rol.equals("PROFESSOR")) {
                res.redirect("/login?error=Acceso denegado. Área exclusiva para profesores.");
                halt(401);
            }
        });

        before("/alumno/*", (req, res) -> {
            String rol = req.session().attribute("user_role");
            if (rol == null || !rol.equals("STUDENT")) {
                res.redirect("/login?error=Acceso denegado. Área exclusiva para alumnos.");
                halt(401);
            }
        });

        // --- Filtro 'after' para cerrar la conexión a la base de datos ---
        // Este filtro se ejecuta después de que cada solicitud HTTP ha sido procesada.
        after((req, res) -> {
            try {
                // Cierra la conexión a la base de datos para liberar recursos.
                Base.close();
                
            } catch (Exception e) {
                // Si ocurre un error al cerrar la conexión, se registra.
                System.err.println("Error al cerrar conexión con ActiveJDBC: " + e.getMessage());
            }
        });

        // --- Rutas GET para renderizar formularios y páginas HTML ---

        // GET: Muestra el formulario de creación de cuenta.
        // Soporta la visualización de mensajes de éxito o error pasados como query
        // parameters.
        get("/user/create", (req, res) -> {
            Map<String, Object> model = new HashMap<>(); // Crea un mapa para pasar datos a la plantilla.

            // Obtener y añadir mensaje de éxito de los query parameters (ej.
            // ?message=Cuenta creada!)
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }

            // Obtener y añadir mensaje de error de los query parameters (ej. ?error=Campos
            // vacíos)
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }

            // Renderiza la plantilla 'user_form.mustache' con los datos del modelo.
            return new ModelAndView(model, "user_form.mustache");
        }, new MustacheTemplateEngine()); // Especifica el motor de plantillas para esta ruta.

        //Ruta de prueba----------------------------------------
        get("/test-error", (req, res) -> {
            throw new RuntimeException("Error de prueba");
        });
        //--------------------------------------------------

        // GET: Ruta para mostrar el dashboard (panel de control) del usuario.
        // Requiere que el usuario esté autenticado.

        // Dashboard ADMIN
        get("/admin/dashboard", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("username", req.session().attribute("currentUserUsername"));

            // Estadisticas del sistema
            model.put("totalUsers", User.count());
            model.put("totalProfessors", Professor.count());
            model.put("totalStudents", Student.count());
            model.put("totalCourses", Course.count());

            return new ModelAndView(model, "dashboard_admin.mustache");
        }, new MustacheTemplateEngine());

        // Dashboard PROFESOR
        get("/profesor/dashboard", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("username", req.session().attribute("currentUserUsername"));

            Long userId = req.session().attribute("userId");
            Professor prof = Professor.findFirst("user_id = ?", userId);
            if (prof != null) {
                model.put("professorName", prof.get("name"));
                model.put("professorSurname", prof.get("surname"));
                model.put("professorEmail", prof.get("email"));
                model.put("professorDni", prof.get("dni"));

                List<Dictated> dictatedList = Dictated.where("idProfessor = ?", prof.getLongId());
                if (dictatedList != null && !dictatedList.isEmpty()) {
                    List<Map<String, Object>> courses = new ArrayList<>();
                    for (Dictated d : dictatedList) {
                        Course course = Course.findById(d.getLong("idCourse"));
                        if (course != null) {
                            Map<String, Object> courseMap = new HashMap<>();
                            courseMap.put("name", course.get("name"));
                            courseMap.put("courseLoad", course.get("courseLoad"));
                            courses.add(courseMap);
                        }
                    }
                    model.put("courses", courses);
                }
            }

            return new ModelAndView(model, "dashboard_profesor.mustache");
        }, new MustacheTemplateEngine());

        // Dashboard ALUMNO
        get("/alumno/dashboard", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("username", req.session().attribute("currentUserUsername"));

            Long userId = req.session().attribute("userId");
            Student student = Student.findFirst("user_id = ?", userId);
            if (student != null) {
                model.put("studentName", student.get("name"));
                model.put("studentSurname", student.get("surname"));
                model.put("studentEmail", student.get("email"));
                model.put("studentDni", student.get("dni"));

                List<Enrollment> enrollmentList = Enrollment.where("idStudent = ?", student.getLongId());
                if (enrollmentList != null && !enrollmentList.isEmpty()) {
                    List<Map<String, Object>> courses = new ArrayList<>();
                    for (Enrollment e : enrollmentList) {
                        Course course = Course.findById(e.getLong("idCourse"));
                        if (course != null) {
                            Map<String, Object> courseMap = new HashMap<>();
                            courseMap.put("name", course.get("name"));
                            courseMap.put("courseLoad", course.get("courseLoad"));
                            courses.add(courseMap);
                        }
                    }
                    model.put("courses", courses);
                }
            }

            return new ModelAndView(model, "dashboard_alumno.mustache");
        }, new MustacheTemplateEngine());

        // GET: Ruta para cerrar la sesión del usuario.
        get("/logout", (req, res) -> {
            // Invalida completamente la sesión del usuario.
            // Esto elimina todos los atributos guardados en la sesión y la marca como
            // inválida.
            // La cookie JSESSIONID en el navegador también será gestionada para
            // invalidarse.
            req.session().invalidate();

            System.out.println("DEBUG: Sesión cerrada. Redirigiendo a /login.");

            // Redirige al usuario a la página de login con un mensaje de éxito.
            res.redirect("/");

            return null; // Importante retornar null después de una redirección.
        });

        // GET: Muestra el formulario de inicio de sesión (login).
        // Nota: Esta ruta debería ser capaz de leer también mensajes de error/éxito de
        // los query params
        // si se la usa como destino de redirecciones. (Tu código de /user/create ya lo
        // hace, aplicar similar).
        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            return new ModelAndView(model, "login.mustache");
        }, new MustacheTemplateEngine()); // Especifica el motor de plantillas para esta ruta.

        // GET: Ruta de alias para el formulario de creación de cuenta.
        // En una aplicación real, probablemente querrías unificar con '/user/create'
        // para evitar duplicidad.
        get("/user/new", (req, res) -> {
            return new ModelAndView(new HashMap<>(), "user_form.mustache"); // No pasa un modelo específico, solo el
                                                                            // formulario.
        }, new MustacheTemplateEngine()); // Especifica el motor de plantillas para esta ruta.

        // --- Rutas POST para manejar envíos de formularios y APIs ---

        // POST: Maneja el envío del formulario de creación de nueva cuenta.
        post("/user/new", (req, res) -> {
            String name = req.queryParams("name");
            String password = req.queryParams("password");
            String role = req.queryParams("role"); // NUEVO: Capturamos el rol elegido en el desplegable
        
            // Validaciones básicas actualizadas para incluir el rol
            if (name == null || name.isEmpty() || password == null || password.isEmpty() || role == null || role.isEmpty()) {
                res.status(400); 
                res.redirect("/user/create?error=Nombre, contraseña y rol son requeridos.");
                return ""; 
            }

            try {
                User ac = new User(); 
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            
                ac.set("name", name); 
                ac.set("password", hashedPassword); 
                ac.set("role", role); // NUEVO: Guardamos el rol en la base de datos
                ac.saveIt(); 
            
                res.status(201); 
                res.redirect("/user/create?message=Cuenta de " + role + " creada exitosamente para " + name + "!");
                return ""; 
            
            } catch (Exception e) {
                System.err.println("Error al registrar la cuenta: " + e.getMessage());
                e.printStackTrace(); 
                res.status(500); 
                res.redirect("/user/create?error=Error interno al crear la cuenta. Intente de nuevo.");
                return ""; 
            }
        });

        // POST: Maneja el envío del formulario de inicio de sesión.
        post("/login", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
        
            String username = req.queryParams("username");
            String plainTextPassword = req.queryParams("password");
        
            // Validaciones básicas
            if (username == null || username.isEmpty() || plainTextPassword == null || plainTextPassword.isEmpty()) {
                res.status(400); 
                model.put("errorMessage", "El nombre de usuario y la contraseña son requeridos.");
                return new ModelAndView(model, "login.mustache"); 
            }
        
            // Busca la cuenta
            User ac = User.findFirst("name = ?", username);
        
            if (ac == null) {
                res.status(401); 
                model.put("errorMessage", "Usuario o contraseña incorrectos."); 
                return new ModelAndView(model, "login.mustache"); 
            }
        
            String storedHashedPassword = ac.getString("password");
        
            // Validar Contraseña
            if (BCrypt.checkpw(plainTextPassword, storedHashedPassword)) {
                res.status(200); 
            
                // --- Gestión de Sesión ---
                req.session(true).attribute("currentUserUsername", username);
                req.session().attribute("userId", ac.getLong("id")); 
                req.session().attribute("loggedIn", true);

                // ¡NUEVO! Guardamos el rol en la sesión
                String rol = ac.getString("role");
                req.session().attribute("user_role", rol);
            
                System.out.println("DEBUG: Login exitoso. Usuario: " + username + " | Rol: " + rol);
            
                // --- Enrutamiento basado en Rol ---
                if ("ADMIN".equals(rol)) {
                    res.redirect("/admin/dashboard");
                } else if ("PROFESSOR".equals(rol)) {
                    res.redirect("/profesor/dashboard");
                } else if ("STUDENT".equals(rol)) {
                    res.redirect("/alumno/dashboard");
                } else {
                    // Failsafe por si el rol está nulo o corrupto
                    req.session().invalidate();
                    res.redirect("/login?error=Error de permisos de usuario.");
                }
                return null;
            
            } else {
                // Contraseña incorrecta
                res.status(401); 
                model.put("errorMessage", "Usuario o contraseña incorrectos."); 
                return new ModelAndView(model, "login.mustache"); 
            }
        }, new MustacheTemplateEngine());

        // POST: Endpoint para añadir usuarios (API que devuelve JSON, no HTML).
        // Advertencia: Esta ruta tiene un propósito diferente a las de formulario HTML.
        post("/add_users", (req, res) -> {
            res.type("application/json"); // Establece el tipo de contenido de la respuesta a JSON.

            // Obtiene los parámetros 'name' y 'password' de la solicitud.
            String name = req.queryParams("name");
            String password = req.queryParams("password");

            // --- Validaciones básicas ---
            if (name == null || name.isEmpty() || password == null || password.isEmpty()) {
                res.status(400); // Bad Request.
                return objectMapper.writeValueAsString(Map.of("error", "Nombre y contraseña son requeridos."));
            }

            try {
                // --- Creación y guardado del usuario usando el modelo ActiveJDBC ---
                User newUser = new User(); // Crea una nueva instancia de tu modelo User.
                // ¡ADVERTENCIA DE SEGURIDAD CRÍTICA!
                // En una aplicación real, las contraseñas DEBEN ser hasheadas (ej. con BCrypt)
                // ANTES de guardarse en la base de datos, NUNCA en texto plano.
                // (Nota: El código original tenía la contraseña en texto plano aquí.
                // Se recomienda usar `BCrypt.hashpw(password, BCrypt.gensalt())` como en la
                // ruta '/user/new').
                newUser.set("name", name); // Asigna el nombre al campo 'name'.
                newUser.set("password", password); // Asigna la contraseña al campo 'password'.
                newUser.saveIt(); // Guarda el nuevo usuario en la tabla 'users'.

                res.status(201); // Created.
                // Devuelve una respuesta JSON con el mensaje y el ID del nuevo usuario.
                return objectMapper.writeValueAsString(
                        Map.of("message", "Usuario '" + name + "' registrado con éxito.", "id", newUser.getId()));

            } catch (Exception e) {
                // Si ocurre cualquier error durante la operación de DB, se captura aquí.
                System.err.println("Error al registrar usuario: " + e.getMessage());
                e.printStackTrace(); // Imprime el stack trace para depuración.
                res.status(500); // Internal Server Error.
                return objectMapper
                        .writeValueAsString(Map.of("error", "Error interno al registrar usuario: " + e.getMessage()));
            }
        });

        // GET: Muestra el formulario de registro de profesor
        get("/professor/create", (req, res) -> {
            
            Map<String, Object> model = new HashMap<>();

            // Verificar que el usuario esté autenticado
            Boolean loggedIn = req.session().attribute("loggedIn");
            if (loggedIn == null || !loggedIn) {
                res.redirect("/login?error=Debes iniciar sesión para acceder a esta página.");
                return null;
            }

            //Listado de cursos
            List<Course> courses = Course.findAll().orderBy("name ASC");
            if(courses!=null && !courses.isEmpty()){
                model.put("courses", courses); //Verifico que haya cursos cargados
            }

            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }

            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }

            return new ModelAndView(model, "profesor_form.mustache");
        }, new MustacheTemplateEngine());

        // POST: Maneja el envío del formulario de registro de profesor
        post("/professor/new", (req, res) -> {
            
            String name = req.queryParams("name");
            String email = req.queryParams("email");
            String surname = req.queryParams("surname");
            String dni = req.queryParams("dni");

            // Validación: Campos obligatorios LLENOS
            //EMAIL
            if (email == null || email.trim().isEmpty()) {
                res.status(400);
                res.redirect("/professor/create?error=Campo Email es OBLIGATORIO");
                return "";
            }
            //NOMBRE
            if (name == null || name.trim().isEmpty()) {
                res.status(400);
                res.redirect("/professor/create?error=Campo Nombre es OBLIGATORIO");
                return "";
            }
            //APELLIDO
            if (surname == null || surname.trim().isEmpty()) {
                res.status(400);
                res.redirect("/professor/create?error=Campo Apellido es OBLIGATORIO");
                return "";
            }
            //DNI
            if (dni == null || dni.trim().isEmpty()) {
                res.status(400);
                res.redirect("/professor/create?error=Campo DNI es OBLIGATORIO");
                return "";
            }



            try {
                // Validación: DNI debe tener 7 u 8 dígitos
                if (!dni.matches("[0-9]{7,8}")) {
                    res.status(400);
                    res.redirect("/professor/create?error=El DNI debe contener 7 u 8 dígitos numéricos.");
                    return "";
                }

                // Validación: email  duplicado
                Professor existingByEmail = Professor.findFirst("email = ?", email);
                if (existingByEmail != null) {
                    res.status(400);
                    res.redirect("/professor/create?error=El el email ya está registrado en el sistema.");
                    return "";
                }
                
                //Validacion: email valido
                //Repo utilizado : https://gist.github.com/donpandix/68dc90a2cde27106c4b960074dce3c17
                String validEmail = email;
                Pattern pattern = Pattern.compile("^([0-9a-zA-Z]+[-._+&])*[0-9a-zA-Z]+@([-0-9a-zA-Z]+[.])+[a-zA-Z]{2,6}$");
		        Matcher matcher = pattern.matcher(validEmail);
		        Boolean valid = matcher.matches();
                if (!valid) {
                    res.status(400);
                    res.redirect("/professor/create?error=El email tiene un formato invalido.");
                    return "";
                }
                
                // Validación: DNI duplicado
                Professor existingByDni = Professor.findFirst("dni = ?", dni);
                if (existingByDni != null) {
                    res.status(400);
                    res.redirect("/professor/create?error=El DNI ya está registrado en el sistema.");
                    return "";
                }

                // Crear y guardar el profesor
                Professor professor = new Professor();
                professor.set("name", name.trim());
                professor.set("email", email.trim());
                professor.set("surname", surname.trim());
                professor.set("dni", dni.trim());
                professor.saveIt(); //Guardo el profesor

                //Aca voy a seguir con las materias

                Long newProfessorID = professor.getLongId();
                //Guardo todos los id de las materias seleccionadas del menu
                String[] courseIds = req.queryParamsValues("course_ids");

                if(courseIds != null)
                    for (String courseID_Str : courseIds) {
                        //Lo paso de string a integer
                        Long courseID = Long.parseLong(courseID_Str);

                        //Creo el modelo para cargarlo a la bd
                        Dictated asignatura = new Dictated();
                        asignatura.set("idProfessor", newProfessorID); //Cargo en "idProfessor" el valor newProfessorÎD
                        asignatura.set("idCourse",courseID); //Cargo en "idCourse" EL VALOR courseID 
                        //Lo guardo
                        asignatura.saveIt();
                    }

                res.status(201);
                res.redirect(
                        "/professor/create?message=Profesor " + name + " " + surname + " registrado exitosamente.");
                return "";

            } catch (Exception e) {
                System.err.println("Error al registrar profesor: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                res.redirect("/professor/create?error=Error interno al registrar el profesor. Intente de nuevo.");
                return "";
            }
        });
        
        // GET: Muestra el formulario para crear una nueva materia.
        get("/coursed/create", (req, res) -> {
            
            Map<String, Object> model = new HashMap<>();

            Boolean loggedIn = req.session().attribute("loggedIn");
            if (loggedIn == null || !loggedIn) {
                res.redirect("/login?error=Debes iniciar sesión para acceder a esta página.");
                return null;
            }

            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }

            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }

            return new ModelAndView(model, "coursed_form.mustache");
        }, new MustacheTemplateEngine());

        // POST: Maneja el envío del formulario para crear una nueva materia.
        post("/coursed/new", (req, res) -> {

            String name = req.queryParams("name");
            String code = req.queryParams("code");
            String yearStr = req.queryParams("year");
            String courseLoadStr = req.queryParams("courseLoad");

            if (name == null || name.trim().isEmpty()) {
                res.status(400);
                res.redirect("/coursed/create?error=El campo Nombre de la Materia es OBLIGATORIO.");
                return "";
            }

            if (code == null || code.trim().isEmpty()) {
                res.status(400);
                res.redirect("/coursed/create?error=El campo Código es OBLIGATORIO.");
                return "";
            }

            if (yearStr == null || yearStr.trim().isEmpty()) {
                res.status(400);
                res.redirect("/coursed/create?error=El campo Año/Nivel es OBLIGATORIO.");
                return "";
            }

            if (courseLoadStr == null || courseLoadStr.trim().isEmpty()) {
                res.status(400);
                res.redirect("/coursed/create?error=El campo Carga Horaria es OBLIGATORIO.");
                return "";
            }

            int courseLoad;
            try {
                courseLoad = Integer.parseInt(courseLoadStr.trim());
                if (courseLoad <= 0) {
                    res.status(400);
                    res.redirect("/coursed/create?error=La Carga Horaria debe ser un número entero positivo.");
                    return "";
                }
            } catch (NumberFormatException e) {
                res.status(400);
                res.redirect("/coursed/create?error=La Carga Horaria debe ser un número entero válido.");
                return "";
            }

            int year;
            try {
                year = Integer.parseInt(yearStr.trim());
                if (year < 1 || year > 5) {
                    res.status(400);
                    res.redirect("/coursed/create?error=El Año/Nivel debe estar entre 1 y 5.");
                    return "";
                }
            } catch (NumberFormatException e) {
                res.status(400);
                res.redirect("/coursed/create?error=El Año/Nivel debe ser un número entero válido.");
                return "";
            }

            try {
                Course existingByName = Course.findFirst("name = ?", name.trim());
                if (existingByName != null) {
                    res.status(400);
                    res.redirect("/coursed/create?error=El nombre de la materia ya existe en el sistema.");
                    return "";
                }

                Course existingByCode = Course.findFirst("code = ?", code.trim().toUpperCase());
                if (existingByCode != null) {
                    res.status(400);
                    res.redirect("/coursed/create?error=El código de la materia ya existe en el sistema.");
                    return "";
                }

                Course newCourse = new Course();
                newCourse.set("name", name.trim());
                newCourse.set("code", code.trim().toUpperCase());
                newCourse.set("year", year);
                newCourse.set("courseLoad", courseLoad);
                newCourse.saveIt();

                res.status(201);
                res.redirect(
                        "/coursed/create?message=Materia '" + name.trim() + "' (" + code.trim().toUpperCase() + ") registrada exitosamente.");
                return "";
            } catch (Exception e) {
                System.err.println("Error al registrar materia: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                res.redirect("/coursed/create?error=Error interno al registrar la materia. Intente de nuevo.");
                return "";
            }
        });
        // --- RUTAS ABM DE USUARIOS (SOLO ADMIN) ---

        // GET: Listado de usuarios
        get("/admin/users", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            List<User> users = User.findAll().orderBy("name ASC");
            model.put("users", users);

            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }

            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }

            return new ModelAndView(model, "users_list.mustache");
        }, new MustacheTemplateEngine());

        // GET: Formulario para crear usuario (admin)
        get("/admin/users/create", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }

            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }

            return new ModelAndView(model, "user_form.mustache");
        }, new MustacheTemplateEngine());

        // POST: Crear usuario (admin)
        post("/admin/users/create", (req, res) -> {
            String name = req.queryParams("name");
            String password = req.queryParams("password");
            String role = req.queryParams("role");

            if (name == null || name.trim().isEmpty() || password == null || password.trim().isEmpty() || role == null || role.trim().isEmpty()) {
                res.status(400);
                res.redirect("/admin/users/create?error=Nombre, contraseña y rol son requeridos.");
                return "";
            }

            try {
                User existing = User.findFirst("name = ?", name.trim());
                if (existing != null) {
                    res.status(400);
                    res.redirect("/admin/users/create?error=El nombre de usuario ya existe.");
                    return "";
                }

                User user = new User();
                user.set("name", name.trim());
                user.set("password", BCrypt.hashpw(password, BCrypt.gensalt()));
                user.set("role", role);
                user.saveIt();

                res.status(201);
                res.redirect("/admin/users?message=Usuario " + name.trim() + " creado exitosamente.");
                return "";
            } catch (Exception e) {
                System.err.println("Error al crear usuario: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                res.redirect("/admin/users/create?error=Error interno al crear el usuario.");
                return "";
            }
        });

        // GET: Formulario para editar usuario
        get("/admin/users/:id/edit", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            String idStr = req.params(":id");
            if (idStr == null || idStr.isEmpty()) {
                res.redirect("/admin/users?error=ID de usuario no especificado.");
                return null;
            }

            try {
                Long id = Long.parseLong(idStr);
                User user = User.findById(id);
                if (user == null) {
                    res.redirect("/admin/users?error=Usuario no encontrado.");
                    return null;
                }

                model.put("user", user);

                String userRole = user.getString("role");
                model.put("selected" + userRole, true);

                String successMessage = req.queryParams("message");
                if (successMessage != null && !successMessage.isEmpty()) {
                    model.put("successMessage", successMessage);
                }

                String errorMessage = req.queryParams("error");
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    model.put("errorMessage", errorMessage);
                }

                return new ModelAndView(model, "user_edit_form.mustache");
            } catch (NumberFormatException e) {
                res.redirect("/admin/users?error=ID de usuario inválido.");
                return null;
            }
        }, new MustacheTemplateEngine());

        // POST: Actualizar usuario
        post("/admin/users/:id/edit", (req, res) -> {
            String idStr = req.params(":id");

            if (idStr == null || idStr.isEmpty()) {
                res.status(400);
                res.redirect("/admin/users?error=ID de usuario no especificado.");
                return "";
            }

            try {
                Long id = Long.parseLong(idStr);
                User user = User.findById(id);
                if (user == null) {
                    res.status(404);
                    res.redirect("/admin/users?error=Usuario no encontrado.");
                    return "";
                }

                String name = req.queryParams("name");
                String password = req.queryParams("password");
                String role = req.queryParams("role");

                if (name == null || name.trim().isEmpty()) {
                    res.status(400);
                    res.redirect("/admin/users/" + id + "/edit?error=El nombre de usuario es requerido.");
                    return "";
                }

                if (role == null || role.trim().isEmpty()) {
                    res.status(400);
                    res.redirect("/admin/users/" + id + "/edit?error=El rol es requerido.");
                    return "";
                }

                User existing = User.findFirst("name = ? AND id != ?", name.trim(), id);
                if (existing != null) {
                    res.status(400);
                    res.redirect("/admin/users/" + id + "/edit?error=El nombre de usuario ya está en uso.");
                    return "";
                }

                user.set("name", name.trim());
                user.set("role", role);

                if (password != null && !password.trim().isEmpty()) {
                    user.set("password", BCrypt.hashpw(password, BCrypt.gensalt()));
                }

                user.saveIt();

                res.status(200);
                res.redirect("/admin/users?message=Usuario " + name.trim() + " actualizado exitosamente.");
                return "";
            } catch (NumberFormatException e) {
                res.status(400);
                res.redirect("/admin/users?error=ID de usuario inválido.");
                return "";
            } catch (Exception e) {
                System.err.println("Error al actualizar usuario: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                res.redirect("/admin/users/" + idStr + "/edit?error=Error interno al actualizar el usuario.");
                return "";
            }
        });

        // POST: Eliminar usuario
        post("/admin/users/:id/delete", (req, res) -> {
            String idStr = req.params(":id");

            if (idStr == null || idStr.isEmpty()) {
                res.status(400);
                res.redirect("/admin/users?error=ID de usuario no especificado.");
                return "";
            }

            try {
                Long id = Long.parseLong(idStr);
                User user = User.findById(id);
                if (user == null) {
                    res.status(404);
                    res.redirect("/admin/users?error=Usuario no encontrado.");
                    return "";
                }

                String username = user.getString("name");
                user.delete();

                res.status(200);
                res.redirect("/admin/users?message=Usuario " + username + " eliminado exitosamente.");
                return "";
            } catch (NumberFormatException e) {
                res.status(400);
                res.redirect("/admin/users?error=ID de usuario inválido.");
                return "";
            } catch (Exception e) {
                System.err.println("Error al eliminar usuario: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                res.redirect("/admin/users?error=Error interno al eliminar el usuario. Verifique que no tenga registros asociados.");
                return "";
            }
        });
    } // Fin del método main
} // Fin de la clase App
