//
// Created by amosa on 5/21/2025.
//

#ifndef LOADER_H
#define LOADER_H
#include "mqtypes.h"


namespace mq {
    mqtype::mq_program* MQ_LoadFromPath(const char* path);
}

#endif //LOADER_H
