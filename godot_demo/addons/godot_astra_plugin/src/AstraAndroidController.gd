extends Node

signal new_body_frame(joints)
signal new_color_frame(byte_buffer)

var module = null

func _ready():
    if Engine.has_singleton("AstraAndroidModule"):
        module = Engine.get_singleton("AstraAndroidModule")
        module.set_instance_id(get_instance_id())
        module.getData()

func _new_body_frame(astra_joints):
    var joints = Dictionary()
    for joint_type in astra_joints:
        var aj = astra_joints[joint_type]
        joints[int(joint_type)] = Vector3(aj["x"], aj["y"], aj["z"])
    emit_signal("new_body_frame", joints)

func _new_color_frame(byte_buffer):
    print(str(byte_buffer))
    get_node("../Label").text = "new byte buffer"
    get_node("../Text").text = str(byte_buffer)
