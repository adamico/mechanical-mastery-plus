#!/bin/sh
# This script is used to configure and run CatDownloader!
# You can add few additional arguments to change the behaviour of the app.
# Check out the github repo of this app for more details https://github.com/Kanzaji/Cat-Downloader-Legacy
export PS4="\$LINENO: "
set -xv

ARGUMENTS_SETUP="-SettingsPath:automation -LogsPath:automation/CDL-Logs -DefaultSettings:false -Mode:CF-Instance"
ARGUMENTS_LAUNCH="-SettingsPath:automation"

cd ..
echo "Setup git hooks"

echo "#!/bin/sh" > .git/hooks/post-merge
echo "java -jar automation/Cat-Downloader-Legacy.jar" $ARGUMENTS_LAUNCH >> .git/hooks/post-merge
chmod +x .git/hooks/post-merge

echo "Done setting up hooks"

echo "Running Cat-Downloader-Legacy"

if [ ! -e "automation/Cat-Downloader-Legacy-Settings.json5" ]
then
  java -jar automation/Cat-Downloader-Legacy.jar $ARGUMENTS_LAUNCH
else
  java -jar automation/Cat-Downloader-Legacy.jar" $ARGUMENTS_SETUP
fi
