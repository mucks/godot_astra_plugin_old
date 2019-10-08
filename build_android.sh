source ~/.profile
cp -r AstraAndroidModule ~/godot/modules/
#cp -r AstraAndroidModule/libs/*.ar ~/godot/platform/android/java/libs/
cd ~/godot/platform/android/java
./gradlew build