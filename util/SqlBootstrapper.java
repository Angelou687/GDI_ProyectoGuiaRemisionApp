/*
 * SqlBootstrapper.java
 * Utilidad para ejecutar el bootstrap de la base de datos usando `psql`.
 * Contiene helpers para ejecutar procesos `psql`, crear la base de datos,
 * ejecutar scripts SQL (ordenados cuando se pasa un directorio) y crear/alterar
 * el usuario de aplicación. Los métodos devuelven un objeto `Result` con
 * información amigable para mostrar en la UI.
 */
package util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class SqlBootstrapper {

    public static class Result {
        public final boolean success;
        public final String output;
        public final int exitCode;

        public Result(boolean success, String output, int exitCode) {
            this.success = success;
            this.output = output;
            this.exitCode = exitCode;
        }
    }

    // runPsql(): intenta ejecutar `psql` con los argumentos proporcionados.
    // - `psqlPath`: ruta a psql.exe (o null/empty para usar "psql" del PATH)
    // - `host`, `port`, `user`, `pass`, `db`: parámetros de conexión
    // - `extraArgs`: argumentos adicionales como -f file.sql o -c "sql"
    // Retorna un `Result` con `success==false` y exitCode -2 cuando no se
    // pudo iniciar el proceso (p. ej. psql no encontrado).
    private static Result runPsql(String psqlPath, String host, String port, String user, String pass, String db, List<String> extraArgs) throws IOException, InterruptedException {
        List<String> cmd = new ArrayList<>();
        cmd.add(psqlPath == null || psqlPath.isEmpty() ? "psql" : psqlPath);
        if (host != null && !host.isEmpty()) { cmd.addAll(Arrays.asList("-h", host)); }
        if (port != null && !port.isEmpty()) { cmd.addAll(Arrays.asList("-p", port)); }
        if (user != null && !user.isEmpty()) { cmd.addAll(Arrays.asList("-U", user)); }
        if (db != null && !db.isEmpty())   { cmd.addAll(Arrays.asList("-d", db)); }
        if (extraArgs != null) cmd.addAll(extraArgs);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        // set PGPASSWORD env var for non-interactive password passing
        if (pass != null) pb.environment().put("PGPASSWORD", pass);
        pb.redirectErrorStream(true);
        Process p;
        try {
            p = pb.start();
        } catch (IOException ioex) {
            // Provide a friendly message when psql is not found
            String attempted = !cmd.isEmpty() ? cmd.get(0) : "psql";
            String msg = "No se pudo ejecutar 'psql'. Asegúrate que psql está instalado o proporciona la ruta completa a psql.exe en el formulario. Intentado: " + attempted + "\n" + ioex.getMessage();
            return new Result(false, msg, -2);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream is = p.getInputStream()) {
            byte[] buf = new byte[4096];
            int r;
            while ((r = is.read(buf)) != -1) baos.write(buf, 0, r);
        }

        int rc = p.waitFor();
        String output = baos.toString(StandardCharsets.UTF_8.name());
        return new Result(rc == 0, output, rc);
    }

    // createDatabaseAndRunSchema(): flujo principal del bootstrap.
    // 1) (opcional) ejecuta 00_drop_objects.sql si `runDropFirst` true.
    // 2) crea la base de datos si no existe (conexión a 'postgres').
    // 3) ejecuta los archivos SQL (o un único archivo) en orden por nombre.
    // 4) crea o modifica el usuario de aplicación y le otorga privilegios.
    public static Result createDatabaseAndRunSchema(String psqlPath, String host, String port, String adminUser, String adminPass, String dbName, File schemaFileOrDir, boolean runDropFirst, String appUser, String appPass) {
        try {
            // Optionally run drop script first
            if (runDropFirst) {
                File drop = new File(schemaFileOrDir.getParentFile(), "00_drop_objects.sql");
                if (drop.exists()) {
                    Result rdrop = runPsql(psqlPath, host, port, adminUser, adminPass, dbName, Arrays.asList("-f", drop.getAbsolutePath()));
                    if (!rdrop.success) return new Result(false, "DROP failed:\n" + rdrop.output, rdrop.exitCode);
                }
            }

            // Create DB if not exists (connect to postgres)
            String createCmd = String.format("SELECT 1 FROM pg_database WHERE datname = '%s';", dbName);
            Result rcCheck = runPsql(psqlPath, host, port, adminUser, adminPass, "postgres", Arrays.asList("-tAc", createCmd));
            if (!rcCheck.success) return new Result(false, "Error checking DB existence:\n" + rcCheck.output, rcCheck.exitCode);
            if (!rcCheck.output.contains("1")) {
                Result rcreate = runPsql(psqlPath, host, port, adminUser, adminPass, "postgres", Arrays.asList("-c", String.format("CREATE DATABASE \"%s\";", dbName)));
                if (!rcreate.success) return new Result(false, "Error creating DB:\n" + rcreate.output, rcreate.exitCode);
            }

            // Execute schema file(s)
            if (schemaFileOrDir != null && schemaFileOrDir.exists()) {
                if (schemaFileOrDir.isDirectory()) {
                    File[] files = schemaFileOrDir.listFiles((d, name) -> name.toLowerCase().endsWith(".sql"));
                    if (files != null) {
                        Arrays.sort(files, Comparator.comparing(File::getName));
                        for (File f : files) {
                            Result rf = runPsql(psqlPath, host, port, adminUser, adminPass, dbName, Arrays.asList("-f", f.getAbsolutePath()));
                            if (!rf.success) return new Result(false, "Error executing " + f.getName() + ":\n" + rf.output, rf.exitCode);
                        }
                    }
                } else {
                    Result rf = runPsql(psqlPath, host, port, adminUser, adminPass, dbName, Arrays.asList("-f", schemaFileOrDir.getAbsolutePath()));
                    if (!rf.success) return new Result(false, "Error executing schema file:\n" + rf.output, rf.exitCode);
                }
            }

            // Create/alter application user
            if (appUser != null && !appUser.isEmpty()) {
                // check if role exists
                Result ruserCheck = runPsql(psqlPath, host, port, adminUser, adminPass, dbName, Arrays.asList("-tAc", String.format("SELECT 1 FROM pg_roles WHERE rolname = '%s';", appUser)));
                if (!ruserCheck.success) return new Result(false, "Error checking app user:\n" + ruserCheck.output, ruserCheck.exitCode);
                if (ruserCheck.output.contains("1")) {
                    Result ralt = runPsql(psqlPath, host, port, adminUser, adminPass, dbName, Arrays.asList("-c", String.format("ALTER USER \"%s\" WITH PASSWORD '%s';", appUser, appPass)));
                    if (!ralt.success) return new Result(false, "Error altering app user:\n" + ralt.output, ralt.exitCode);
                } else {
                    Result rcreate = runPsql(psqlPath, host, port, adminUser, adminPass, dbName, Arrays.asList("-c", String.format("CREATE USER \"%s\" WITH PASSWORD '%s';", appUser, appPass)));
                    if (!rcreate.success) return new Result(false, "Error creating app user:\n" + rcreate.output, rcreate.exitCode);
                }

                // grant privileges
                List<String> grants = Arrays.asList(
                        String.format("GRANT CONNECT ON DATABASE \"%s\" TO \"%s\";", dbName, appUser),
                        String.format("GRANT USAGE ON SCHEMA public TO \"%s\";", appUser),
                        String.format("GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO \"%s\";", appUser),
                        String.format("ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO \"%s\";", appUser)
                );
                for (String g : grants) {
                    Result rg = runPsql(psqlPath, host, port, adminUser, adminPass, dbName, Arrays.asList("-c", g));
                    if (!rg.success) return new Result(false, "Error granting privileges:\n" + rg.output, rg.exitCode);
                }
            }

            // Si todo pasó, retornamos success
            return new Result(true, "Bootstrap completed successfully", 0);
        } catch (IOException | InterruptedException ex) {
            StringWriter sw = new StringWriter(); ex.printStackTrace(new PrintWriter(sw));
            return new Result(false, sw.toString(), -1);
        }
    }

    public static boolean writeDbProperties(String host, String port, String dbName, String appUser, String appPass) {
        // writeDbProperties(): escribe un fichero `db.properties` simple
        // con `db.url`, `db.user` y `db.pass` para que `db.Conexion` pueda usarlo.
        try (FileOutputStream fos = new FileOutputStream("db.properties")) {
            String url = String.format("jdbc:postgresql://%s:%s/%s", host == null || host.isEmpty() ? "localhost" : host, port == null || port.isEmpty() ? "5432" : port, dbName);
            String content = String.format("db.url=%s\n db.user=%s\n db.pass=%s\n", url, appUser, appPass);
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
