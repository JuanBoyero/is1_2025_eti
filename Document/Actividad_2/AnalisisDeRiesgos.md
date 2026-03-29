# **Analisis De Riesgo Del Proyecto Integrador**

## **Análisis de riesgo hecho por ChatGPT Pro**

| Tipo de Riesgo   | Descripción                                                                 | Probabilidad | Impacto  | Identificado por |
|:---|:---|:---|:---|:---|
| Técnico          | Uso de patrón Singleton para la base de datos sin control de concurrencia, lo que puede generar problemas de acceso simultáneo. | Media        | Alto     | Auditor          |
| Técnico          | Ausencia de validaciones robustas en la capa de modelos/controladores que puede derivar en errores en tiempo de ejecución. | Alta         | Alto     | Auditor          |
| Técnico          | Dependencia de templates (Mustache) sin separación clara de lógica y presentación. | Media        | Medio    | Auditor          |
| Organizacional   | Falta de definición clara de roles (todos con misma responsabilidad general). | Alta         | Medio    | Auditor          |
| Organizacional   | Centralización excesiva en el líder del grupo para completar el proyecto.   | Media        | Alto     | Auditor          |
| Planificación    | Plazo de 3 semanas insuficiente para cubrir análisis, diseño, implementación y pruebas. | Alta         | Crítico  | Auditor          |
| Planificación    | Ausencia de evidencia de estimación formal (historias, puntos, etc.).        | Alta         | Alto     | Auditor          |
| Planificación    | Falta de planificación explícita de testing y validación del sistema.       | Alta         | Crítico  | Auditor          |
| Humano           | Equipo con experiencia limitada (estudiantes), lo que aumenta errores técnicos. | Alta         | Alto     | Auditor          |
| Humano           | Dependencia de pocos integrantes para completar todo el sistema.            | Media        | Crítico  | Auditor          |
| Humano           | Posible sobrecarga de trabajo debido al corto plazo.                        | Alta         | Alto     | Auditor          |

## **Análisis de riesgo hecho por el Equipo de trabajo**

| Tipo de Riesgo   | Descripción                                                                 | Probabilidad | Impacto  | Identificado por |
|:---|:---|:---|:---|:---|
| Técnico  | Falta de criterio de unificación de Usuarios en el sistema | Media | Bajo | Equipo de trabajo |
| Técnico | Falta de documentación de el equipo anterior respecto al trabajo hecho| Alta | Critico | Equipo de Trabajo|
| Organizacional   | Inexperiencia a la hora de dividir roles y tareas | Alta         | Alta    | Equipo de Trabajo         |
| Organizacional   | Demasiada dependencia del líder para terminar el proyecto  | Alta        | Alto     | Equipo de Trabajo       |
|Planificación | Falta de criterio en la Especificación de los tiempos de entregas | Bajo | Critico | Equipo de Trabajo |
|Humano | Falta de compromiso de equipo de trabajo  | Media | Alto | Equipo De Trabajo|
|Humano| Falta de comunicación del Equipo de trabajo | Media | Critico | Equipo De Trabajo|

## **Comparacion de Resultados**

Ambos análisis de riesgos demostraron resultados similares en cuanto a los riesgos identificados, tanto técnicos como de planificación y humanos.

Esto permitió evidenciar algunos puntos críticos a la hora de desarrollar proyectos de este tipo, brindándonos una mejor comprensión del enfoque que debemos adoptar al momento de resolver problemas de software.

En la parte técnica, se observa que ambos análisis coinciden en los aspectos tecnológicos que deben considerarse durante el desarrollo del sistema.

Por otro lado, en las dimensiones organizacional, de planificación y humana, se identificaron problemáticas como la falta de experiencia, la escasa comunicación y la ausencia de documentación adecuada por parte de los integrantes del equipo. Asimismo, se evidenció una falta de comprensión de los plazos establecidos, lo que generó dificultades durante el desarrollo del software.

Este análisis de riesgos, junto con su correspondiente documentación, contribuye a que el desarrollo de un proyecto de software sea más estable, fluido y con menor cantidad de inconvenientes.