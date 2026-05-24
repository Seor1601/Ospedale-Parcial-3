# Ospedale
# Parcial 3 - Ospedale

## Integrantes

- Samuel Elias Ortega Rodriguez - NRC: 2039
- Sofia Lopez De La Hoz - NRC: 2039

## Descripción

Refactorización del proyecto Ospedale a arquitectura MVC aplicando principios SOLID. El sistema permite gestionar usuarios, pacientes, doctores, citas, hospitalizaciones y tratamientos desde una interfaz gráfica Java Swing.

## Ejecución

Abrir el proyecto en Apache NetBeans con JDK 21 y ejecutar la clase `packagee.Main`.

## Estructura general

- `controller`: controladores para login, pacientes, doctores, citas, hospitalizaciones y tablas.
- `controller/response`: sistema de respuestas con códigos de estado.
- `storage`: almacenamiento simulado y carga de datos JSON.
- `validator`: validaciones de reglas de negocio.
- `dto`: serialización de datos para enviar información a las vistas.
- `json`: archivos con datos iniciales del sistema.
