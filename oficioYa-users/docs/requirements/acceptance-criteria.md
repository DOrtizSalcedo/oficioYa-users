# ✅ Criterios de aceptación del microservicio de usuarios

Cada requerimiento funcional tiene sus criterios en formato **Dado / Cuando / Entonces**. Todos están automatizados en
[`UserAcceptanceTest`](../../src/test/java/edu/eci/dosw/oficioya_users/acceptance/UserAcceptanceTest.java), que levanta la
aplicación completa (seguridad JWT, controlador, servicio y base de datos H2) y llama a los endpoints como lo haría el front.

Para ejecutarlas:

```bash
mvn test -Dtest=UserAcceptanceTest
```

## Evidencias

Además de las pruebas automatizadas, la [colección de Postman](../oficioya-users.postman_collection.json) recorre los mismos
requerimientos con 20 peticiones. Ejecutada completa (*Run collection*), pasan las 40 pruebas:

![Ejecución de la colección de Postman](../evidencias/postman-run.png)

Documentación de los endpoints en Swagger (`/swagger-ui.html`):

![Swagger del microservicio de usuarios](../evidencias/swagger.png)

## RF-01 Registrar cuenta (`POST /api/v1/users`)

| # | Dado | Cuando | Entonces |
|---|------|--------|----------|
| 1 | Un correo que no está registrado | Me registro con nombre, un teléfono principal y el rol CONTRATANTE | Recibo 201 y la cuenta queda Activa con ese rol y el correo enmascarado |
| 2 | Un correo que no está registrado | Me registro como TRABAJADOR y CONTRATANTE | La cuenta tiene ambos roles |
| 3 | Un correo ya registrado (sin importar mayúsculas) | Intento registrarme con él | Recibo 409 |
| 4 | — | Intento registrarme con el rol ADMIN | Recibo 400 |
| 5 | — | Me registro con dos teléfonos principales | Recibo 400 |
| 6 | — | Me registro con más de tres teléfonos o con datos inválidos | Recibo 400 |

## RF-02 Agregar rol (`POST /api/v1/users/me/roles/{role}`)

| # | Dado | Cuando | Entonces |
|---|------|--------|----------|
| 1 | Un contratante activo | Agrega el rol TRABAJADOR | Su cuenta tiene ambos roles, sin crear otra cuenta |
| 2 | Un usuario que ya tiene el rol | Lo vuelve a agregar | Recibe 409 |
| 3 | Un usuario activo | Intenta agregarse el rol ADMIN | Recibe 400 |
| 4 | El administrador | Intenta agregarse un rol | Recibe 403 |

## RF-03 Consultar perfil propio (`GET /api/v1/users/me`)

| # | Dado | Cuando | Entonces |
|---|------|--------|----------|
| 1 | Un usuario activo | Consulta su perfil | Ve sus datos con correo (`*****@mail.com`) y teléfono (`******4567`) enmascarados |
| 2 | — | Consulta el perfil sin token | Recibe 401 |

## RF-04 Editar perfil propio (`PATCH /api/v1/users/me`)

| # | Dado | Cuando | Entonces |
|---|------|--------|----------|
| 1 | Un usuario activo | Cambia su nombre y sus teléfonos | Quedan guardados el nombre nuevo y solo los teléfonos nuevos; el correo no cambia |
| 2 | Un usuario activo | Envía teléfonos sin uno principal | Recibe 400 |

## RF-05 Consultar perfil público (`GET /api/v1/users/{id}`)

| # | Dado | Cuando | Entonces |
|---|------|--------|----------|
| 1 | Otro usuario activo | Consulto su perfil | Solo veo id, nombre y roles (sin correo ni teléfonos) |
| 2 | Un usuario desactivado | Consulto su perfil público | Recibo 404 |
| 3 | Que mi propia cuenta está desactivada | Consulto un perfil público | Recibo 403 |

## RF-06 Desactivar cuenta (`PATCH /api/v1/admin/users/{id}/deactivate`)

| # | Dado | Cuando | Entonces |
|---|------|--------|----------|
| 1 | Un usuario activo | El admin lo desactiva con motivo | La cuenta queda Inactiva y el usuario recibe 403 al usar su perfil |
| 2 | Un usuario activo | El admin lo desactiva sin motivo | Recibe 400 |
| 3 | Una cuenta ya inactiva | El admin la vuelve a desactivar | Recibe 409 |
| 4 | El administrador | Intenta desactivar su propia cuenta | Recibe 403 |
| 5 | Un usuario que no es admin | Intenta desactivar una cuenta | Recibe 403 |

## RF-07 Reactivar cuenta (`PATCH /api/v1/admin/users/{id}/activate`)

| # | Dado | Cuando | Entonces |
|---|------|--------|----------|
| 1 | Un usuario inactivo | El admin lo reactiva con motivo | La cuenta queda Activa y el usuario vuelve a usar su perfil |
| 2 | Una cuenta ya activa | El admin la reactiva | Recibe 409 |

## RF-08 Consultar información completa (`GET /api/v1/admin/users`, `GET /api/v1/admin/users/{id}`)

| # | Dado | Cuando | Entonces |
|---|------|--------|----------|
| 1 | Un usuario registrado | El admin lo consulta | Ve correo, teléfonos, estado y fechas sin enmascarar |
| 2 | Cuentas registradas | El admin lista los usuarios | Aparecen las cuentas registradas |
| 3 | Un id que no existe | El admin lo consulta | Recibe 404 |
| 4 | Un usuario que no es admin | Consulta la información completa | Recibe 403 |
