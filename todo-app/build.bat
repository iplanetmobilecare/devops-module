@echo off
setlocal enabledelayedexpansion

set SCRIPT_DIR=%~dp0
cd /d "%SCRIPT_DIR%"

set SRC_DIR=src\main\java
set RES_DIR=src\main\resources
set WEB_DIR=src\main\webapp
set LIB_DIR=lib
set TARGET_DIR=target
set CLASSES_DIR=%TARGET_DIR%\classes
set STAGING_DIR=%TARGET_DIR%\war-staging
set WAR_FILE=%TARGET_DIR%\todo-app.war

if exist "%CLASSES_DIR%" rmdir /s /q "%CLASSES_DIR%"
if exist "%STAGING_DIR%" rmdir /s /q "%STAGING_DIR%"
if exist "%WAR_FILE%" del /f /q "%WAR_FILE%"

mkdir "%CLASSES_DIR%"
mkdir "%STAGING_DIR%\WEB-INF\classes"
mkdir "%STAGING_DIR%\WEB-INF\lib"

set CLASSPATH=%LIB_DIR%\jakarta.servlet-api-5.0.0.jar;%LIB_DIR%\gson-2.10.1.jar;%LIB_DIR%\mysql-connector-j-8.3.0.jar

javac -encoding UTF-8 -cp "%CLASSPATH%" -d "%CLASSES_DIR%" ^
    %SRC_DIR%\com\lovely\todo\model\*.java ^
    %SRC_DIR%\com\lovely\todo\dao\*.java ^
    %SRC_DIR%\com\lovely\todo\controller\*.java

if %ERRORLEVEL% NEQ 0 (
    exit /b %ERRORLEVEL%
)

if exist "%RES_DIR%" (
    xcopy /e /y /q "%RES_DIR%\*" "%CLASSES_DIR%\"
)
xcopy /e /y /q "%CLASSES_DIR%\*" "%STAGING_DIR%\WEB-INF\classes\"

xcopy /e /y /q "%WEB_DIR%\*" "%STAGING_DIR%\"
copy /y "%LIB_DIR%\gson-2.10.1.jar" "%STAGING_DIR%\WEB-INF\lib\"
copy /y "%LIB_DIR%\mysql-connector-j-8.3.0.jar" "%STAGING_DIR%\WEB-INF\lib\"

cd "%STAGING_DIR%"
jar -cvf "..\%~nx0\..\todo-app.war" . > nul
cd /d "%SCRIPT_DIR%"
move /y "todo-app.war" "%WAR_FILE%" > nul 2>&1

echo Build finished: %WAR_FILE%
