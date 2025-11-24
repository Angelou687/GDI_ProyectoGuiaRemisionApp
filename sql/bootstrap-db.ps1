<#
Bootstrap DB PowerShell script
- Pregunta los datos al usuario (host, puerto, admin user, admin password, db name, app user, app password)
- Crea la base de datos si no existe
- Ejecuta un archivo schema SQL (por defecto sql\schema.sql) usando psql
- Crea/actualiza el usuario de aplicación y otorga permisos

Requisitos:
- Cliente psql en PATH, o indicar la ruta completa a psql.exe cuando se solicite
- Ejecutar desde PowerShell con permisos de usuario capaz de comunicarse con el servidor Postgres
#>

function Read-SecureStringAsPlainText([string]$prompt) {
    $s = Read-Host -AsSecureString -Prompt $prompt
    $bstr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($s)
    try {
        return [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($bstr)
    } finally {
        [System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
    }
}

Write-Host "=== Bootstrap PostgreSQL database (psql-based) ===" -ForegroundColor Cyan

# Encuentra psql
$psqlCmd = "psql"
try {
    $which = (Get-Command $psqlCmd -ErrorAction SilentlyContinue)
    if (-not $which) {
        Write-Host "No se encontró 'psql' en PATH." -ForegroundColor Yellow
        $psqlCmd = Read-Host "Ruta completa a psql.exe (por ejemplo C:\\Program Files\\PostgreSQL\\13\\bin\\psql.exe)"
    }
} catch {
    $psqlCmd = Read-Host "Ruta completa a psql.exe"
}

$host = Read-Host "Host de Postgres (default: localhost)"
if ([string]::IsNullOrWhiteSpace($host)) { $host = 'localhost' }
$port = Read-Host "Puerto de Postgres (default: 5432)"
if ([string]::IsNullOrWhiteSpace($port)) { $port = '5432' }
$adminUser = Read-Host "Usuario admin de Postgres (ej: postgres)"
$adminPass = Read-SecureStringAsPlainText "Contraseña del usuario admin"
$dbName = Read-Host "Nombre de la base de datos a crear (ej: mi_app_db)"
if ([string]::IsNullOrWhiteSpace($dbName)) { Write-Host "Debe indicar un nombre de base de datos"; exit 1 }
$appUser = Read-Host "Usuario de aplicación a crear (ej: app_user)"
$appPass = Read-SecureStringAsPlainText "Contraseña para el usuario de aplicación"

# Ubicación por defecto del script SQL (en la carpeta sql/schema.sql relativa al script)
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$defaultSchema = Join-Path $scriptDir "schema.sql"
$schemaFile = Read-Host "Ruta al archivo schema SQL (default: $defaultSchema)"
if ([string]::IsNullOrWhiteSpace($schemaFile)) { $schemaFile = $defaultSchema }
if (-not (Test-Path $schemaFile)) {
    Write-Host "No se encontró el archivo SQL en: $schemaFile" -ForegroundColor Red
    $ok = Read-Host "¿Deseas continuar sin ejecutar schema.sql? (s/n)"
    if ($ok -ne 's' -and $ok -ne 'S') { exit 1 }
}

# Función auxiliar para ejecutar psql y devolver código
function Run-Psql($db, $sqlArgs) {
    $env:PGPASSWORD = $adminPass
    $args = @('-h', $host, '-p', $port, '-U', $adminUser, '-d', $db) + $sqlArgs
    $proc = Start-Process -FilePath $psqlCmd -ArgumentList $args -NoNewWindow -Wait -PassThru -RedirectStandardOutput ($scriptDir + "\psql_out.txt") -RedirectStandardError ($scriptDir + "\psql_err.txt")
    $rc = $proc.ExitCode
    Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
    return $rc
}

# Comprobar si la BD existe
Write-Host "Comprobando si la base de datos existe..."
$checkSql = "-tAc" , "SELECT 1 FROM pg_database WHERE datname = '$dbName';"
$env:PGPASSWORD = $adminPass
$proc = Start-Process -FilePath $psqlCmd -ArgumentList @('-h', $host, '-p', $port, '-U', $adminUser, '-d', 'postgres', '-tAc', "SELECT 1 FROM pg_database WHERE datname = '$dbName';") -NoNewWindow -Wait -PassThru -RedirectStandardOutput ($scriptDir + "\psql_check_out.txt") -RedirectStandardError ($scriptDir + "\psql_check_err.txt")
$existsOut = Get-Content ($scriptDir + "\psql_check_out.txt") -ErrorAction SilentlyContinue | Out-String
Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
if ($existsOut -match '1') {
    Write-Host "La base de datos '$dbName' ya existe." -ForegroundColor Green
} else {
    Write-Host "Creando la base de datos '$dbName'..."
    $rc = Run-Psql 'postgres' @('-c', "CREATE DATABASE \"$dbName\";")
    if ($rc -ne 0) { Write-Host "Error creando la base de datos. Revisa psql salida en psql_err.txt" -ForegroundColor Red; exit $rc }
    Write-Host "Base de datos creada." -ForegroundColor Green
}

# Ejecutar script(s) SQL
if (Test-Path $schemaFile) {
    if ((Get-Item $schemaFile).PSIsContainer) {
        Write-Host "Se encontró un directorio. Ejecutando todos los archivos .sql en: $schemaFile ..." -ForegroundColor Cyan
        $sqlFiles = Get-ChildItem -Path $schemaFile -Filter '*.sql' | Sort-Object Name
        foreach ($f in $sqlFiles) {
            Write-Host "Ejecutando: $($f.FullName) ..." -ForegroundColor Gray
            $rcf = Run-Psql $dbName @('-f', $f.FullName)
            if ($rcf -ne 0) { Write-Host "Error ejecutando $($f.Name). Revisa psql_err.txt" -ForegroundColor Red; exit $rcf }
        }
        Write-Host "Todos los scripts en el directorio fueron ejecutados correctamente." -ForegroundColor Green
    }
    else {
        Write-Host "Ejecutando script de esquema: $schemaFile ..."
        $rc2 = Run-Psql $dbName @('-f', $schemaFile)
        if ($rc2 -ne 0) { Write-Host "Error ejecutando schema.sql. Revisa psql_err.txt" -ForegroundColor Red; exit $rc2 }
        Write-Host "Script ejecutado correctamente." -ForegroundColor Green
    }
}

# Crear o actualizar usuario de aplicación y otorgar permisos mínimos
Write-Host "Creando/actualizando usuario de aplicación y otorgando permisos..."
$env:PGPASSWORD = $adminPass
$checkUserProc = Start-Process -FilePath $psqlCmd -ArgumentList @('-h', $host, '-p', $port, '-U', $adminUser, '-d', $dbName, '-tAc', "SELECT 1 FROM pg_roles WHERE rolname = '$appUser';") -NoNewWindow -Wait -PassThru -RedirectStandardOutput ($scriptDir + "\psql_user_check_out.txt") -RedirectStandardError ($scriptDir + "\psql_user_check_err.txt")
$userExistsOut = Get-Content ($scriptDir + "\psql_user_check_out.txt") -ErrorAction SilentlyContinue | Out-String
Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue

if ($userExistsOut -match '1') {
    Write-Host "Usuario $appUser ya existe — actualizando contraseña..."
    $rc3 = Run-Psql $dbName @('-c', "ALTER USER \"$appUser\" WITH PASSWORD '$appPass';")
    if ($rc3 -ne 0) { Write-Host "Error al actualizar contraseña del usuario." -ForegroundColor Red; exit $rc3 }
} else {
    Write-Host "Creando usuario $appUser..."
    $rc4 = Run-Psql $dbName @('-c', "CREATE USER \"$appUser\" WITH PASSWORD '$appPass';")
    if ($rc4 -ne 0) { Write-Host "Error creando usuario." -ForegroundColor Red; exit $rc4 }
}

# Otorgar permisos mínimos al usuario de aplicación
$grantSqls = @( 
    "GRANT CONNECT ON DATABASE \"$dbName\" TO \"$appUser\";",
    "GRANT USAGE ON SCHEMA public TO \"$appUser\";",
    "GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO \"$appUser\";",
    "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO \"$appUser\";"
)
foreach ($g in $grantSqls) {
    $rcg = Run-Psql $dbName @('-c', $g)
    if ($rcg -ne 0) { Write-Host "Error ejecutando: $g" -ForegroundColor Red; exit $rcg }
}

Write-Host "Usuario y permisos configurados correctamente." -ForegroundColor Green
Write-Host "Bootstrap finalizado. Puedes guardar las credenciales de aplicación en un archivo config.properties para que la app las use." -ForegroundColor Cyan

# Recomendar borrar archivos temporales y variables
Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
Write-Host "Archivos de salida psql guardados en: $scriptDir (psql_out.txt, psql_err.txt)" -ForegroundColor DarkGray

# Fin
