@echo off
PUSHD .\3rdParty
set THIRD_PARTY_DIR=%CD%

PUSHD ..\
set ANT_HOME=%THIRD_PARTY_DIR%\ant\apache-ant-1.7.0
if exist "%ProgramFiles%\Java\jdk1.8.0_40" (
    set JAVA_HOME=%ProgramFiles%\Java\jdk1.8.0_40
) else (
    echo Error: Can't find Java JDK Installation
)
set PATH=%PATH%;%ANT_HOME%\bin;%JAVA_HOME%\bin