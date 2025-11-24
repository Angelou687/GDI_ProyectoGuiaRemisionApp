@echo off
REM Ejecuta el diálogo de bootstrap directamente
java -Dfile.encoding=UTF-8 -cp "bin;lib/*" util.BootstrapRunner
if %ERRORLEVEL% neq 0 (
  echo EJECUCION FALLIDA
  exit /b %ERRORLEVEL%
)
exit /b 0
