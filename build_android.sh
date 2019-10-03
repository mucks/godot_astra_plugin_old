source ~/.profile
armv7a-linux-androideabi29-clang -fPIC \
    -c src/*.cpp \
    -g -O3 -std=c++14 \
    -Igodot-cpp/include -Igodot-cpp/include/core \
    -Igodot-cpp/include/gen \
    -Igodot-cpp/godot_headers \
    -Idependencies/linux/astra_sdk/include


ASTRA_PATH=$PWD/dependencies/android/jni/armeabi-v7a

armv7a-linux-androideabi29-clang -o bin/libtest.so \
    -shared astra_controller.o \
    -shared godot_astra.o \
    -l$PWD/godot-cpp/bin/libgodot-cpp.android.debug.armv7.a \
    -l$ASTRA_PATH/libastra_android_bridge.so \
    -l$ASTRA_PATH/libastra_core_api.so \
    -l$ASTRA_PATH/libastra_core.so \
    -l$ASTRA_PATH/libastra_jni.so \
    -l$ASTRA_PATH/libastra.so
