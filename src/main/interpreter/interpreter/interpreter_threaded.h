//
// Created by amosa on 5/21/2025.
//

#ifndef INTERPRETER_THREADED_H
#define INTERPRETER_THREADED_H
#include <memory>
#include <thread>

#include "mqtypes.h"
#include "sync_interpreter.h"


namespace mq {
    struct vm_t;

    class threaded_interpreter {
    public:
        int id;

        vm_t* interpreter;
        std::thread* thread = nullptr;

        mqtype::task* current_task = nullptr;
        sync_interpreter* syncinterpreter = nullptr;

        std::atomic<mqtype::tic_signal> signal = mqtype::SIG_KEEP_ALIVE;

        explicit threaded_interpreter(vm_t* instance, const int id) : id(id), interpreter(instance) {};
        void run();
        void cleanup();

        ~threaded_interpreter() {
            delete thread;
            cleanup();
        }
    };
}


#endif //INTERPRETER_THREADED_H
