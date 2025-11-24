Bootstrap DB (psql) — Instrucciones

Este repositorio contiene un script PowerShell `bootstrap-db.ps1` que: 
- crea la base de datos solicitada (si no existe)
- ejecuta el archivo SQL de esquema (`schema.sql`) usando `psql`
- crea/actualiza un usuario de aplicación y otorga permisos mínimos

Requisitos
- PostgreSQL cliente (`psql`) instalado y en `PATH`, o indicar la ruta completa a `psql.exe` cuando se solicite.
- Acceso a un servidor PostgreSQL (host, puerto) y credenciales de un usuario con permisos para crear bases (ej: `postgres`).
- El archivo `schema.sql` con el DDL del proyecto debe estar en `sql/schema.sql` (o indicar otra ruta durante la ejecución).

Cómo usar
1. Abrir PowerShell como el usuario que pueda contactar al servidor.
2. Ir a la carpeta del proyecto donde está `sql\bootstrap-db.ps1`.
3. Ejecutar:

   .\sql\bootstrap-db.ps1

4. Seguir las indicaciones: ingresar host, puerto, usuario admin y contraseña, nombre de BD, usuario app, contraseña app y ruta del `schema.sql` (puedes dejar la ruta por defecto si el archivo está en `sql/schema.sql`).

Notas de seguridad
- No incluyas credenciales admin en el ejecutable. Introduce las credenciales cuando se soliciten.
- El script no guarda la contraseña admin; solo la usa en memoria para ejecutar `psql`.
- Para la aplicación, guarda solo las credenciales del `app_user` en un archivo de configuración (ej: `config.properties`) y, de preferencia, cifra la contraseña o usa el almacén de credenciales del sistema.

Problemas comunes
- "psql: not found": instala PostgreSQL o añade la ruta a `psql` al `PATH`.
- Errores al ejecutar `schema.sql`: abre `sql/psql_err.txt` o `sql/psql_out.txt` para ver la salida generada por psql (el script escribe archivos temporales en la carpeta `sql`).

Si quieres que automati ce este bootstrap desde el propio `.exe`, puedo añadir una clase Java que invoque este PowerShell desde `MainApp` o que llame directamente a `psql` mediante `ProcessBuilder`. Si prefieres, también puedo añadir una UI inicial para pedir las credenciales y ejecutar el bootstrap automáticamente.
