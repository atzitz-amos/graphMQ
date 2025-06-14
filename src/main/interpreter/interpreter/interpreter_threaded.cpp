//
// Created by amosa on 5/21/2025.
//

#include "interpreter_threaded.h"
#include "vm_t.h"

#include <iostream>

#include "sync_interpreter.h"


void mq::threaded_interpreter::run() {
    __debug("Created");

    while (true) {
        if (signal.load() == mqtype::SIG_ABORT) {
            cleanup();

            __debug("Quitting...");
            return;
        }

        if (current_task == nullptr || syncinterpreter == nullptr) {
            current_task = interpreter->tqm.pop();

            if (current_task == nullptr) {
                if (interpreter->_tic.NoMoreTasks()) {
                    __debug("No more tasks, quitting...");
                    return;
                }
                std::this_thread::sleep_for(std::chrono::milliseconds(100));
                continue;
            }

            syncinterpreter = new sync_interpreter(interpreter->program, current_task, interpreter);

            interpreter->_tic.IncrTaskCount();
            __debug("Took task over: ...");
        }

        if (const auto [message, value, arg] = syncinterpreter->step(); value == mqtype::STATUS_ERROR_SHOULD_ABORT) {
            std::cerr << "[threaded_interpreter " << id << "]" << " Fatal Error: " << message << std::endl <<
                "Aborting...";
            interpreter->_tic.Abort();
        }
        else if (value == mqtype::STATUS_THREAD_FREED) {
            interpreter->_tic.DecrTaskCount();
            if (syncinterpreter) {
                delete syncinterpreter;
                syncinterpreter = nullptr;
            }
            current_task = nullptr;
            __debug("Task done, freeing thread...");
        }
        else if (value == mqtype::STATUS_SHOULD_CLEANUP) {
            interpreter->_tic.DecrTaskCount();
            cleanup();
            __debug("Cleaning up...");
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
