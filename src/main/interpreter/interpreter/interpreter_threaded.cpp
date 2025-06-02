//
// Created by amosa on 5/21/2025.
//

#include "interpreter_threaded.h"
#include "vm_t.h"

#include <iostream>

#include "sync_interpreter.h"


void mq::threaded_interpreter::run() {
    while (true) {
        if (signal.load() == mqtype::SIG_ABORT) {
            std::terminate();
        }
        if (signal.load() == mqtype::SIG_TERMINATE_AND_CLEANUP) {
            cleanup();
            return;
        }

        if (current_task == nullptr || syncinterpreter == nullptr) {
            if (interpreter->tqm.empty()) {
                if (signal.load() == mqtype::SIG_KEEP_ALIVE) {
                    std::this_thread::sleep_for(std::chrono::milliseconds(100));
                }
                else {
                    signal.store(mqtype::SIG_TERMINATE_AND_CLEANUP);
                }
                continue;
            }
            current_task = interpreter->tqm.pop();

            syncinterpreter = new sync_interpreter(interpreter->program, current_task);
        }

        auto [message, value] = syncinterpreter->step();

        if (value == mqtype::STATUS_ERROR_SHOULD_ABORT) {
            std::cerr << "Error: " << message << std::endl;
            signal.store(mqtype::SIG_ABORT);
            return;
        }
        if (value == mqtype::STATUS_TASK_OVER) {
            signal.store(mqtype::SIG_TERMINATE_AND_CLEANUP);
        }
    }
}

void mq::threaded_interpreter::cleanup() {
    if (syncinterpreter) {
        delete syncinterpreter;
        syncinterpreter = nullptr;
    }
    if (current_task) {
        delete current_task;
        current_task = nullptr;
    }
}
