@echo off
set DIRNAME=%~dp0
if "%JAVA_HOME%"=="" set JAVACMD=java
if not "%JAVA_HOME%"=="" set JAVACMD=%JAVA_HOME%\bin\java
"%JAVACMD%" -jar "%DIRNAME%gradle\wrapper\gradle-wrapper.jar" %*
