#ifndef ASTRA_CONTROLLER_H
#define ASTRA_CONTROLLER_H

#include <Godot.hpp>
#include <Node.hpp>
#include <astra/astra.hpp>
#include <astra_core/astra_core.hpp>
#include <iomanip>
#include <iostream>
#include <string>
#include <thread>
#include <chrono>

namespace godot {

class AstraController : public Node {
  GODOT_CLASS(AstraController, Node)

private:
  float time_passed;
  std::thread *update_thread;
  bool is_running;

public:
  godot::PoolByteArray poolByteArray;
  static void _register_methods();

  static void update(AstraController *ex);

  AstraController();
  ~AstraController();

  void _init(); // our initializer called by Godot

  void _process(float delta);
};

} // namespace godot

#endif