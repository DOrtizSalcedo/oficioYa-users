# 📄 Planeación del Sistema

## Desglose de trabajo: Épicas, Historias de Usuario y Tareas

La implementación de los requerimientos identificados de OficioYa se desglosa de la siguiente manera:

### 1. Épica:

| Campo | Descripción                                                                                                                      |
|------|----------------------------------------------------------------------------------------------------------------------------------|
| **ID** | EP-01                                                                                                                            |
| **Título** | Creación del perfil del usuario y su información general                                                                         |
| **Descripción** | Permitir que el usuario (trabajador, contratante o ambos roles) pueda gestionar su información y cuenta dentro de la plataforma. |
| **Stakeholder** | Trabajadores y Contratantes                                                                                                      |

### 2. Historias de usuario:

| Campo | Descripción                                                                                                                                                             |
|------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **ID** | HU-01                                                                                                                                                                   |
| **Título** | Registro de usuarios                                                                                                                                                    |
| **Descripción** | COMO usuario (trabajador o contratante) que quiere usar la plataforma web <br/>QUIERO poder registrarme con una cuenta <br/>PARA acceder a los servicios que la plataforma ofrece |
| **Prioridad** | Alta                                                                                                                                                                    |
| **Justificación** | Es el prerequisito para que la plataforma pueda ser funcional, prácticamente son los que dan la interacción dentro de la plataforma                                     |
| **Estimación** | 5                                                                                                                                                                       |

| Campo | Descripción                                                                                                                                  |
|------|----------------------------------------------------------------------------------------------------------------------------------------------|
| **ID** | HU-02                                                                                                                                        |
| **Título** | Consultar su propio perfil                                                                                                                   |
| **Descripción** | COMO usuario registrado (trabajador o contratante) <br/>QUIERO visualizar mi perfil <br/>PARA verificar que la información esté correcta y actualizada |
| **Prioridad** | Media                                                                                                                                        |
| **Justificación** | Es la base para que un usuario pueda ser identificado dentro de la plataforma                                                                |
| **Estimación** | 3                                                                                                                                            |

| Campo | Descripción                                                                                                                                               |
|------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| **ID** | HU-03                                                                                                                                                     |
| **Título** | Editar perfil                                                                                                                                             |
| **Descripción** | COMO usuario registrado (trabajador o contratante) <br/>QUIERO editar la información de mi perfil <br/>PARA mostrar mi información actualizada a los demás usuarios |
| **Prioridad** | Baja                                                                                                                                                      |
| **Justificación** | Permite que el usuario pueda actualizar, eliminar o añadir información                                                                                    |
| **Estimación** | 3                                                                                                                                                         |

| Campo | Descripción                                                                                                                                                     |
|------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **ID** | HU-04                                                                                                                                                           |
| **Título** | Consultar perfil de otro usuario                                                                                                                                |
| **Descripción** | COMO usuario de la plataforma (trabajador o contratante) QUIERO ver el perfil de otro usuario PARA conocer su información básica sin acceder a datos delicados* |
| **Prioridad** | Media                                                                                                                                                           |
| **Justificación** | Posibilita que los usuarios puedan ver la información de otros sin tener que ver el correo y teléfono del que estén visualizando                                |
| **Estimación** | 9.7                                                                                                                                                             |

| Campo | Descripción                                                                                                                                                              |
|------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **ID** | HU-05                                                                                                                                                                    |
| **Título** | Gestión de usuarios por parte del administrador                                                                                                                          |
| **Descripción** | COMO administrador de la plataforma <br/>QUIERO gestionar las cuentas de los usuarios <br/>PARA moderar perfiles, reportar y verificar identidades por si sucede alguna disputa    |
| **Prioridad** | Alta                                                                                                                                                                     |
| **Justificación** | Es el usuario que debe velar que los usuarios de la plataforma tengan una experiencia agradable para evitar posibles conflictos de identidad y mantener la transparencia |
| **Estimación** | 9.7                                                                                                                                                                      |

### 3. Tareas:

| Campo | Descripción |
|------|--|
| **ID** | TR-01 |
| **Título** | Definir atributos de los registros |
| **ID de la Historia de Uso asociada** | HU-01 |
| **Descripción** | Especificar los atributos cuando un usuario hace un registro en la plataforma según su tipo |
| **Tareas requisito** | Ninguna |

| Campo | Descripción                                                                                                      |
|------|------------------------------------------------------------------------------------------------------------------|
| **ID** | TR-02                                                                                                            |
| **Título** | Documentar el doble rol                                                                                          |
| **ID de la Historia de Uso asociada** | HU-01                                                                                                            |
| **Descripción** | Verificar qué implicaciones tiene un usuario si es trabajador y contratante sin que tenga que crear otra cuenta. |
| **Tareas requisito** | TR-01                                                                                                            |

| Campo | Descripción                                                                                                                                 |
|------|---------------------------------------------------------------------------------------------------------------------------------------------|
| **ID** | TR-03                                                                                                                                       |
| **Título** | Definir lo visible y oculto en un perfil                                                                                                    |
| **ID de la Historia de Uso asociada** | HU-02                                                                                                                                       |
| **Descripción** | Establecer los campos que ve el usuario al consultar su propio perfil y cuáles se muestran como ocultos para la administración del sistema. |
| **Tareas requisito** | Ninguna                                                                                                                                     |

| Campo | Descripción                                                                       |
|------|-----------------------------------------------------------------------------------|
| **ID** | TR-04                                                                             |
| **Título** | Especificar lo que se puede editar de un perfil                                   |
| **ID de la Historia de Uso asociada** | HU-03                                                                             |
| **Descripción** | Determinar qué campos puede editar el usuario y cuáles no se les permite cambiar. |
| **Tareas requisito** | TR-03                                                                             |

| Campo | Descripción                                                                                                                                       |
|------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| **ID** | TR-05                                                                                                                                             |
| **Título** | Definir campos visibles en perfiles de otros usuarios                                                                                             |
| **ID de la Historia de Uso asociada** | HU-04                                                                                                                                             |
| **Descripción** | Establecer la información visible al consultar el perfil de otro usuario y verificar que la información sensible para el sistema no se pueda ver. |
| **Tareas requisito** | TR-03 y TR-04                                                                                                                                     |

| Campo | Descripción                                                                                            |
|------|--------------------------------------------------------------------------------------------------------|
| **ID** | TR-06                                                                                                  |
| **Título** | Especificar CRUD del usuario administrador                                                             |
| **ID de la Historia de Uso asociada** | HU-05                                                                                                  |
| **Descripción** | Declarar qué operaciones CRUD realiza sobre las cuentas y cuando determinar que la identidad es falsa. |
| **Tareas requisito** | Ninguna                                                                                                |
