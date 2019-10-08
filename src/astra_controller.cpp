#include "astra_controller.h"

using namespace godot;

void AstraController::_register_methods()
{
  register_signal<AstraController>("new_color_frame", "width", GODOT_VARIANT_TYPE_INT,
                                   "height", GODOT_VARIANT_TYPE_INT, "colors",
                                   GODOT_VARIANT_TYPE_POOL_BYTE_ARRAY);
  register_signal<AstraController>("new_body_frame", "joints", GODOT_VARIANT_TYPE_DICTIONARY);
}

AstraController::AstraController() {}

AstraController::~AstraController()
{
  astra::terminate();
}

void convert_image(int width, int height, AstraController *ex, const astra::RgbPixel *colorPtr)
{
  int byteLength = width * height * 4;

  if (ex->poolByteArray.size() != byteLength)
  {
    ex->poolByteArray.resize(byteLength);

    for (auto i = 0; i < byteLength; i++)
    {
      ex->poolByteArray.set(0, 0);
    }
  }

  for (auto i = 0; i < width * height; i++)
  {
    int rgbaOffset = i * 4;

    ex->poolByteArray.set(rgbaOffset, colorPtr[i].r);
    ex->poolByteArray.set(rgbaOffset + 1, colorPtr[i].g);
    ex->poolByteArray.set(rgbaOffset + 2, colorPtr[i].b);
    ex->poolByteArray.set(rgbaOffset + 3, 255);
  }
}

int jointTypeToInt(astra::JointType joint_type)
{
  switch (joint_type)
  {
  case astra::JointType::Head:
    return 0;
  case astra::JointType::ShoulderSpine:
    return 1; /*!< Shoulder spine */
  case astra::JointType::LeftShoulder:
    return 2; /*!< Left Shoulder */
  case astra::JointType::LeftElbow:
    return 3; /*!< Left Elbow */
  case astra::JointType::LeftHand:
    return 4; /*!< Left hand */
  case astra::JointType::RightShoulder:
    return 5; /*!< Right Shoulder */
  case astra::JointType::RightElbow:
    return 6; /*!< Right Elbow */
  case astra::JointType::RightHand:
    return 7; /*!< Right Hand */
  case astra::JointType::MidSpine:
    return 8; /*!< Mid spine */
  case astra::JointType::BaseSpine:
    return 9; /*!< Base spine */
  case astra::JointType::LeftHip:
    return 10; /*!< Left Hip */
  case astra::JointType::LeftKnee:
    return 11; /*!< Left Knee */
  case astra::JointType::LeftFoot:
    return 12; /*!< Left Foot */
  case astra::JointType::RightHip:
    return 13; /*!< Right Hip */
  case astra::JointType::RightKnee:
    return 14; /*!< Right Knee */
  case astra::JointType::RightFoot:
    return 15; /*!< Right Foot */
  case astra::JointType::LeftWrist:
    return 16; /*!< Left Wrist */
  case astra::JointType::RightWrist:
    return 17; /*!< Right Wrist */
  case astra::JointType::Neck:
    return 18; /*!< Neck */
  case astra::JointType::Unknown:
    return 255; /*!< Unknown */
  }
  return -1;
}

void handleBodyFrame(AstraController *ex, astra::Frame frame)
{
  auto bodyFrame = frame.get<astra::BodyFrame>();
  if (bodyFrame.is_valid())
  {
    const auto &bodies = bodyFrame.bodies();

    for (auto &body : bodies)
    {
      auto dictionary = godot::Dictionary();
      for (auto &joint : body.joints())
      {
        int joint_type = jointTypeToInt(joint.type());
        auto world_position = godot::Vector3();
        world_position.x = joint.world_position().x;
        world_position.y = joint.world_position().y;
        world_position.z = joint.world_position().z;

        dictionary[joint_type] = world_position;
      }
      ex->emit_signal("new_body_frame", dictionary);
    }
  }
}

void handleColorFrame(AstraController *ex, astra::Frame frame)
{
  auto colorFrame = frame.get<astra::ColorFrame>();

  if (colorFrame.is_valid())
  {
    int width = colorFrame.width();
    int height = colorFrame.height();

    convert_image(width, height, ex, colorFrame.data());

    ex->emit_signal("new_color_frame", width, height, ex->poolByteArray);
  }
}

void AstraController::update(AstraController *ex)
{
  astra::terminate();
  astra::initialize();

  Godot::print("astra initialized");

  const char *licenseString = "<INSERT LICENSE KEY HERE>";
  orbbec_body_tracking_set_license(licenseString);

  astra::StreamSet sensor;
  auto reader = sensor.create_reader();

  auto colorStream = reader.stream<astra::ColorStream>();
  colorStream.start();

  auto bodyStream = reader.stream<astra::BodyStream>();
  bodyStream.start();

  while (true)
  {
    astra_update();

    if (reader.has_new_frame())
    {
      // Godot::print("new frame");
      auto frame = reader.get_latest_frame();

      if (frame.is_valid())
      {
        handleColorFrame(ex, frame);
        handleBodyFrame(ex, frame);
      }
    }
  }
}

void AstraController::_init()
{
  // initialize any variables here
  poolByteArray = PoolByteArray();
  is_running = false;
  time_passed = 0.0;

  update_thread = new std::thread(AstraController::update, this);
}
