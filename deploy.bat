@echo off

mkdir deploy\build\
xcopy /s/z E:\proj\SunTailoring\proj\3rdParty deploy\3rdParty\
xcopy /s/z E:\proj\SunTailoring\proj\src deploy\src\
xcopy /s/z E:\proj\SunTailoring\proj\test deploy\test\
xcopy /s/z E:\proj\SunTailoring\proj\build deploy\build\
xcopy /s/z E:\proj\SunTailoring\proj\build.xml deploy\build.xml*
xcopy /s/z E:\proj\SunTailoring\proj\run.bat deploy\run.bat*
