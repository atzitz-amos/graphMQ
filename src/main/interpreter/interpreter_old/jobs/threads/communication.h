//
// Created by amosa on 11/23/2024.
//

#ifndef COMMUNICATION_H
#define COMMUNICATION_H


#include <mutex>

#include "../../bytecode/bytecode.h"

namespace graphMQ {
    enum class signals {
        XJOB_STARTED = 0x0,
        XJOB_RUNNING = 0x1,
        XJOB_WAITING = 0x2,
        XJOB_ERROR = 0x3,
        XJOB_COMPLETED = 0x4,

        MJOB_HALT = 0x5,
    };

    class job_frame {
    public:
        int* locals = nullptr;
        int* stack = nullptr;
        int* arguments = nullptr;
        int* closure = nullptr;
        int* SP = nullptr;

        node_t nd;

        ~job_frame() {
            delete[] locals;
            delete[] stack;
        }
    };

    class shared_interface {
    public:
        bool running = false;

        std::mutex m_mutex{};
        signals signal = signals::XJOB_STARTED;

        void set_signal(signals sig);
    };
}


#endif //COMMUNICATION_H
