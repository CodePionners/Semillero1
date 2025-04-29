# Modelo de Apoyo al Diagnóstico de Cáncer de Melanoma mediante Deep Learning
## Descripción

El proyecto, desarrollado por el semillero de investigación de la Universidad CUN,
tiene como objetivo principal el diseño y desarrollo de una herramienta de apoyo al
diagnóstico médico, orientada al análisis y reconocimiento del cáncer de
melanoma a partir de imágenes reales de lesiones cutáneas.
Esta herramienta contará con un módulo de extradición de características que
permiten analizar detalladamente las propiedades visuales de lesiones en la piel,
con el fin de identificar patrones que puedan seleccionar o indicar la presencia de
patologías, ya sean benignas o malignas, como el cáncer de piel tipo melanoma,
para este proyecto. Se emplearán técnicas de Deep Learning que posibilitan el
procesamiento y la interpretación automatizada de imágenes.

## Requisitos Implementados (Fase Inicial)

1.  Ingreso y almacenamiento de imágenes de lesiones en la piel.
2.  Filtrado y selección básica de atributos visuales (placeholder o implementación inicial con OpenCV).
3.  Clasificación diagnóstica inicial simplificada o placeholder (la implementación avanzada con ML se considera una fase futura).
4.  Permitir eliminar imágenes cargadas.
5.  Mostrar al usuario un historial de imágenes analizadas.

## Tecnologías Principales

*   **Lenguaje:** Java <Versión 21>
*   **Framework Backend:** Spring Boot <Versión de Spring Boot>
*   **Build Tool:** Maven
*   **Base de Datos (Desarrollo):** H2 Database (en archivo)
*   **Base de Datos (Posible Producción):** MySQL
*   **Persistencia:** Spring Data JPA / Hibernate
*   **Frontend (Inicial):** Thymeleaf, HTML, CSS, JavaScript
*   **Procesamiento de Imágenes:** OpenCV para Java (se integrará)
*   **Control de Versiones:** Git
*   **Plataforma Git:** GitHub

## Configuración del Entorno de Desarrollo

1.  Asegúrate de tener instalado el JDK de Java (<La misma versión que elegiste>).
2.  Instala un IDE compatible con Spring Boot y Maven (IntelliJ IDEA con extensiones Java).
3.  Si usas Maven fuera del IDE, asegúrate de tenerlo instalado.

## Primeros Pasos

1.  Clona el repositorio desde GitHub:
    ```bash
    git clone <https://github.com/CodePionners/Semillero/blob/main/README.md>
    ```
2.  Navega a la rama de desarrollo principal:
    ```bash
    cd <Nombre de la carpeta del proyecto, ej: salc>
    git checkout develop
    ```
3.  Importa el proyecto en tu IDE (IntelliJ IDEA). El IDE debería detectar automáticamente que es un proyecto Maven y descargar las dependencias. Si no, busca la opción "Import Maven Project" o "Reload Project".
4.  Verifica el archivo `src/main/resources/application.properties` para ajustar la configuración de la base de datos H2 y el directorio de subida de imágenes (`app.upload.dir`) si es necesario.
5.  Verifica que el directorio de subida de imágenes definido en `app.upload.dir` esté configurado para ser creado al iniciar la aplicación (ver `SalcApplication.java` o clase de configuración).

## Compilación y Ejecución

Para compilar el proyecto usando Maven:
```bash
mvn clean install