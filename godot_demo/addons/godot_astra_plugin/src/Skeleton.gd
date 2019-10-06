extends Spatial

var show_legs = true
var joint_objects: Dictionary

var joint_scene = preload("res://addons/godot_astra_plugin/scenes/Joint.tscn")

enum JointType { HEAD, SHOULDER_SPINE, LEFT_SHOULDER, LEFT_ELBOW, LEFT_HAND, \
    RIGHT_SHOULDER, RIGHT_ELBOW, RIGHT_HAND, MID_SPINE, BASE_SPINE, LEFT_HIP, \
    LEFT_KNEE, LEFT_FOOT, RIGHT_HIP, RIGHT_KNEE, RIGHT_FOOT, LEFT_WRIST, \
    RIGHT_WRIST, NECK, UNKNOWN = 255, PARSE_ERROR = -1}

func is_leg(joint_type):
    match joint_type:
        JointType.RIGHT_FOOT, JointType.RIGHT_KNEE, JointType.RIGHT_HIP, \
        JointType.LEFT_FOOT, JointType.LEFT_KNEE, JointType.LEFT_HIP:
            return true
        _:
            return false

func _on_AstraController_new_body_frame(joints):
    for joint_type in joints:
        if joint_type == JointType.UNKNOWN:
            continue
        if !show_legs && is_leg(joint_type):
            continue
        
        #var joint_name = JointType.keys()[joint_type]
        var translation = joints[joint_type] / 150
        translation.x *= -1

        if not joint_objects.has(joint_type):
            joint_objects[joint_type] = joint_scene.instance()
            joint_objects[joint_type].translation = translation
            add_child(joint_objects[joint_type])
        else:
            joint_objects[joint_type].translation = translation

    