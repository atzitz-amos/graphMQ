//
// Created by amosa on 11/23/2024.
//

#include "communication.h"

#include <mutex>


using namespace graphMQ;
void shared_interface::set_signal(const signals sig) {
    signal = sig;
}
