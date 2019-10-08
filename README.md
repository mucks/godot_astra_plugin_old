# will update README.md shortly


### setup instructions
#### linux
* install astra sdk and put it in your path <Br> 
(cp -r dependencies/linux/astra_sdk/libs/* /usr/lib/)
* cd this repo dir
* git submodule update --init --recursive
* cd godot-cpp
* scons platform=x11 generate_bindings=yes
* cd ..
* scons platform=x11
* you can use the compiled binary in your projects
* when exporting make sure the astra sdk libs are inside the project dir and in the path <BR> (see "godot_demo/ld_library_path.sh")

#### android export
* install android sdk and ndk and put them both in your path
* cd ~/
* git clone https://github.com/godotengine/godot
* git checkout 3.1
* cd godot
* scons -j8 platform=x11/windows
* scons -j8 platform=android target=release_debug
* cd this repo dir
* ./build_android.sh
* add android_debug.apk and android_release.apk in ~/godot/bin to the export in the godot editor
* add module "org/godotengine/godot/AstraAndroidModule" in ProjectSettings->Android


### some notes
* for arch linux or manjaro use ubuntu 14.04 astra sdk version and install libpng12
* unplug and replug the sensor when its flickering
* on windows make sure to compile godot-cpp with the argument bits=64
* keep in mind that the included astra_sdks have their own license in their respective folders
* windows wasn't tested so its currently wip