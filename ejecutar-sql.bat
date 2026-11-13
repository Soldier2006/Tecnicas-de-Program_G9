@echo off
cd /d "%~dp0"
java -Djava.library.path=lib -cp "dist\Bodexa.jar;lib\*" bodexa.Main --sql
pause
