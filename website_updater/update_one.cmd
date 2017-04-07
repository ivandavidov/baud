@echo off

cd ..

if "%1" == "" goto missing_param
if "%2" == "" goto missing_param

if exist weekly.js del weekly.js
call run %1
if not exist weekly.js goto missing_js
copy /y weekly.js website\%2
echo The file '%2' has been processed.
goto exit

:missing_js
echo The file 'weekly.js' is missing.
goto exit

:missing_param
echo There are missing parameters.
goto exit

:exit
cd website_updater