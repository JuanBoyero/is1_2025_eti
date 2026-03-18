![](unrc_logo.png)

**Departamento de Computación**  
**Asignatura: Ingeniería de Software II (Cód. 3387)**  
**Año 2026**

# Proyecto integrador: Especificación, Gestión y planificación

## 1. (Requirements) Describir su proyecto:

- Problema que se quiere resolver
- Usuarios del sistema
- Funcionalidades principales
- Restricciones técnicas
- Tamaño del equipo
- Tecnologías elegidas y justificación
- Plazo estimado
- Cambios de alcance ocurridos
- Problemas encontrados
- Forma de organización del equipo

Generar un documento en markdown con esta información. Este documento va a servir como base para la gestión del proyecto.

---

## 2. (Auditoría) Análisis de riesgos con IA

a) Pedir al LLM que identifique riesgos.
b) El equipo debe identificar riesgos manualmente.
c) Comparar ambos análisis:

- Riesgos que encontró la IA y el equipo no
- Riesgos que encontró el equipo y la IA no
- Calidad del análisis

```
Actúa como un auditor de proyectos de software.
A partir de la siguiente descripción, identifica: - Riesgos técnicos -
Riesgos organizacionales - Riesgos de planificación - Riesgos humanos
Clasifícalos por probabilidad e impacto.
```

Objetivo: Estructurar información de gestión para que una IA pueda analizarla. Iterar sobre el documento generato en 1.

| Tipo de Riesgo     | Descripción                                         | Probabilidad | Impacto | Identificado por |
| :----------------- | :-------------------------------------------------- | :----------- | :------ | :--------------- |
| **Técnico**        | Curva de aprendizaje en nuevas librerías.           | Media        | Alto    | -                |
| **Organizacional** | Desalineación en la prioridad de tareas.            | Baja         | Medio   | -                |
| **Planificación**  | Desviación en la estimación de historias complejas. | Alta         | Crítico | -                |
| **Humano**         | Falta de disponibilidad de un rol clave.            | Baja         | Crítico | -                |

---

## 3. (Design) Generar los Diagramas de Arquitectura del sistema y diagrama de diseño.

Estos diagramas deben incluir los componentes principales del sistema, sus responsabilidades y las interacciones entre ellos.

---

## 4. Crear un backlog en github projects.

Este backlog va a tener que mantenerse actualizado durante el desarrollo del proyecto.

a. Los issues deben incluir:

- Tipo de tarea (feature / bug / investigación / gestión)
- Descripción
- Estimación
- Responsable
- Prioridad

---

b. (SQA) Definir criterios de aceptación para cada tarea del backlog. Estos criterios deben ser claros y medibles, para que el equipo pueda saber cuándo una tarea está completa. Registrar estos criterios de aceptación en el backlog.
Ejemplo:

```
Una tarea se considera terminada cuando:
  - el código compila ?
  - tiene tests ???
  - pasa CI ?
  - fue revisado por otro miembro del equipo ?
```

---

## 5. (Estimation) Para cada tarea del backlog, estimar el esfuerzo necesario para completarla.

Pueden usar cualquier técnica de estimación que conozcan (T-Shirts, dogs, puntos de historia, horas ideales, etc.). Registrar estas estimaciones en el backlog.

---

## 6. (SCM) Crear un Roadmap en github projects con los hitos del proyecto.

Este roadmap va a servir para planificar el desarrollo del proyecto, se tiene que mantener actualizado durante el desarrollo del proyecto.

---

## 7. (SCM) Durante el desarrollo del proyecto, registrar el tiempo real que se tarda en completar cada tarea del backlog.

Comparar el tiempo real con las estimaciones y analizar las desviaciones. Burndown Chart.
