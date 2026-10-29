# Biblioteca26 - Proyecto Spring Boot MVC

Proyecto CRUD completo con Spring Boot, JPA/Hibernate y MySQL, siguiendo la arquitectura MVC.

## Estructura del Proyecto

```
src/main/java/com/biblioteca26/
├── Biblioteca26Application.java       # Clase principal (SpringBootApplication)
├── controladores/                     # Controladores MVC para cada entidad
│   ├── InicioController.java
│   ├── AutorController.java
│   ├── LibroController.java
│   ├── PremioController.java
│   ├── PaisController.java
│   ├── SerieController.java
│   ├── AutorLibroController.java
│   ├── AutorPremioController.java
│   └── LibroPremioController.java
├── modelos/                           # Entidades JPA
│   ├── Autor.java
│   ├── Libro.java
│   ├── Premio.java
│   ├── Pais.java
│   ├── Serie.java
│   ├── AutorLibro.java
│   ├── AutorPremio.java
│   └── LibroPremio.java
├── repositorios/                      # Repositorios Spring Data JPA
│   ├── AutorRepository.java
│   ├── LibroRepository.java
│   ├── PremioRepository.java
│   ├── PaisRepository.java
│   ├── SerieRepository.java
│   ├── AutorLibroRepository.java
│   ├── AutorPremioRepository.java
│   └── LibroPremioRepository.java
└── vistas/                            # (Paquete preparado para controladores MVC)

src/main/resources/
├── templates/                         # Plantillas Thymeleaf
│   ├── index.html
│   ├── autores/
│   ├── libros/
│   ├── premios/
│   ├── paises/
│   ├── series/
│   ├── autor-libros/
│   ├── autor-premios/
│   └── libro-premios/
├── application.properties             # Configuración de DB y JPA

pom.xml                                # Configuración Maven
```

## Requisitos

- **Java 17+**
- **Maven 3.6+**
- **MySQL Server**

## Configuración de la Base de Datos

1. **Crear la BD en MySQL:**
   ```sql
   CREATE DATABASE libros;
   ```

2. **Ejecutar el script SQL de esquema:**
   ```bash
   mysql -u root -p libros < libros.sql
   ```

3. **Editar `application.properties`:**
   Ajusta los valores de conexión en `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/libros?useSSL=false&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=TU_CONTRASEÑA
   spring.jpa.hibernate.ddl-auto=none
   ```
   - Cambiar `TU_CONTRASEÑA` por tu contraseña MySQL.
   - `ddl-auto=none` evita que Hibernate altere el esquema existente.

## Compilación

```bash
cd biblioteca26
mvn clean package -DskipTests
```

## Ejecución

### Opción 1: Maven
```bash
mvn spring-boot:run
```

### Opción 2: JAR Ejecutable
```bash
java -jar target/biblioteca26-0.0.1-SNAPSHOT.jar
```

La aplicación arrancará en `http://localhost:8080`.

## URLs y Rutas CRUD

| Entidad | Listado | Crear | Editar | Eliminar |
|---------|---------|-------|--------|----------|
| Paises | `/paises` | `/paises/nuevo` | `/paises/editar/{id}` | `/paises/eliminar/{id}` |
| Series | `/series` | `/series/nuevo` | `/series/editar/{id}` | `/series/eliminar/{id}` |
| Autores | `/autores` | `/autores/nuevo` | `/autores/editar/{id}` | `/autores/eliminar/{id}` |
| Premios | `/premios` | `/premios/nuevo` | `/premios/editar/{id}` | `/premios/eliminar/{id}` |
| Libros | `/libros` | `/libros/nuevo` | `/libros/editar/{id}` | `/libros/eliminar/{id}` |
| Autor Libros | `/autor-libros` | `/autor-libros/nuevo` | `/autor-libros/editar/{id}` | `/autor-libros/eliminar/{id}` |
| Autor Premios | `/autor-premios` | `/autor-premios/nuevo` | `/autor-premios/editar/{id}` | `/autor-premios/eliminar/{id}` |
| Libro Premios | `/libro-premios` | `/libro-premios/nuevo` | `/libro-premios/editar/{id}` | `/libro-premios/eliminar/{id}` |

## Inicio

Accede a `http://localhost:8080/` para ver el menú principal con enlaces a todos los CRUD.

## Tecnologías

- **Spring Boot 3.3.4** - Framework web/MVC
- **Spring Data JPA** - Persistencia con Hibernate
- **Thymeleaf** - Motor de plantillas
- **MySQL Connector/J** - Driver JDBC para MySQL
- **Maven** - Gestor de dependencias

## Notas

- El proyecto está configurado con `spring.jpa.show-sql=true` en `application.properties` para ver las sentencias SQL en consola (útil para debug).
- Cambiar `spring.thymeleaf.cache=false` para desarrollo sin necesidad de reiniciar.
- Para agregar validaciones, usar `@Valid`, `@NotBlank`, etc. en controladores.
- Para paginación/búsqueda, extender `PagingAndSortingRepository` en los repositorios.
