# normalize-encoding.ps1
# Normaliza todos los archivos .java a UTF-8 (sin BOM) y a Unicode NFC
# Uso: Ejecutar desde la raíz del repo: powershell -ExecutionPolicy Bypass -File .\normalize-encoding.ps1

$normForm = [System.Text.NormalizationForm]::FormC
Get-ChildItem -Recurse -Filter *.java | ForEach-Object {
    $path = $_.FullName
    Write-Host "Normalizando: $path"
    try {
        $s = [System.IO.File]::ReadAllText($path)
        if ($s -ne $null) {
            $s2 = $s.Normalize($normForm)
            # Escribir en UTF8 sin BOM
            $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
            [System.IO.File]::WriteAllText($path, $s2, $utf8NoBom)
        }
    } catch {
        Write-Warning "Error procesando $path : $_"
    }
}
Write-Host "Normalización completada."
