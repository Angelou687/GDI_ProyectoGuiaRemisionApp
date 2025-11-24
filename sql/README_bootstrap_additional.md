Ejecutar múltiples archivos y re-ejecución segura

- El script `bootstrap-db.ps1` soporta ejecutar un único `schema.sql` o todos los `*.sql` dentro de la carpeta `sql/` en orden alfabético.
- Para permitir re-ejecuciones limpias, hemos añadido `sql/00_drop_objects.sql` que elimina objetos previos si los hay. Recomendación de uso:

  1) Si quieres limpiar y aplicar todo desde cero (Peligro: borrará datos): ejecuta primero `00_drop_objects.sql` y luego el esquema:

```powershell
cd 'C:\ruta\a\tu\repo\Guia_remision_app_gdi'
# ejecutar solo el drop
psql -h localhost -p 5432 -U postgres -d tu_bd -f .\sql\00_drop_objects.sql
# luego ejecutar el schema completo
psql -h localhost -p 5432 -U postgres -d tu_bd -f .\sql\schema.sql
```

  2) Alternativamente, deja que el `bootstrap-db.ps1` haga el trabajo: cuando te pregunte por la ruta al script, indícale la carpeta `sql` (no un archivo). El script ejecutará todos los .sql en la carpeta en orden alfabético.

Notas:
- Asegúrate de tener copia de seguridad antes de ejecutar `00_drop_objects.sql` en entornos con datos.
- Si prefieres que divida `schema.sql` en archivos numerados (`01_tables.sql`, `02_functions.sql`, etc.), lo hago y actualizo `bootstrap-db.ps1` para ejecutarlos en orden.
