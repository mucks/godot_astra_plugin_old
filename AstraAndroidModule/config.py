def can_build(env, plat):
    return plat=="android" #ensures that this module is built for android only

def configure(env):
    if env['platform'] == 'android':
        env.android_add_java_dir("src")
        env.android_add_dependency("implementation fileTree(include: ['*.aar'], dir: 'libs')")