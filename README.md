## Getting Started

Welcome to the VS Code Java world. Here is a guideline to help you get started to write Java code in Visual Studio Code.

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.

## Dependency Management

The `JAVA PROJECTS` view allows you to manage your dependencies. More details can be found [here](https://github.com/microsoft/vscode-java-dependency#manage-dependencies).

## Compilar y ejecutar con codificación UTF-8 (tildes/acentos)

Si al ejecutar la aplicación los caracteres con tildes (á, é, í, ó, ú, ñ) se ven mal, asegúrate de compilar y ejecutar forzando UTF-8:

- Compilar (Windows PowerShell / cmd):

javac -encoding UTF-8 -d bin -cp "lib\mysql-connector-j-9.5.0.jar" src\\**\\*.java

- Ejecutar (forzar encoding en la JVM):

java -Dfile.encoding=UTF-8 -cp "bin;lib\mysql-connector-j-9.5.0.jar" MainApp

Nota: también confirma que los archivos fuente están guardados en UTF-8 en tu editor (VS Code muestra la codificación en la barra de estado).
