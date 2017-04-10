@echo off

cd ..

if "%1" == "" goto missing_param
if "%2" == "" goto missing_param

if exist daily.csv del daily.csv
if exist weekly.csv del weekly.csv
if exist weekly.js del weekly.js

call run %1

if not exist daily.csv goto missing_daily_csv
if not exist weekly.csv goto missing_weekly_csv
if not exist weekly.js goto missing_weekly_js

copy /y daily.csv website\data\%2-dnevni-danni.csv
echo The file 'website\data\%2-dnevni-danni.csv' has been processed.

copy /y weekly.csv website\data\%2-sedmichni-danni.csv
echo The file 'website\data\%2-sedmichni-danni.csv' has been processed.

copy /y weekly.js website\data\%2.js
echo The file 'website\data\%2.js' has been processed.

goto exit

:missing_weekly_js
echo The file 'weekly.js' is missing.
goto exit

:missing_daily_csv
echo The file 'daily.csv' is missing.
goto exit

:missing_weekly_csv
echo The file 'weekly.csv' is missing.
goto exit

:missing_param
echo There are missing parameters.
goto exit

:exit
cd website_updater
