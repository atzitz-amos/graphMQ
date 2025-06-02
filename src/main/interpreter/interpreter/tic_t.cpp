//
// Created by amosa on 5/21/2025.
//

#include "tic_t.h"
#include "interpreter_threaded.h"
#include "vm_t.h"


void mq::tic_t::run() {
    constexpr int num_thread = 1; // std::thread::hardware_concurrency();
    for (int i = 0; i < num_thread; ++i) {
        auto* instance = new threaded_interpreter(interpreter_instance, i);
        threads.push_back(instance);

        instance->thread = new std::thread(&threaded_interpreter::run, instance);
    }
}

void mq::tic_t::join() const {
    for (const auto thread : threads) {
        if (thread->thread->joinable()) {
            thread->thread->join();
        }
    }
}


void mq::tic_t::terminate() {
    for (const auto thread : threads) {
        thread->signal.store(mqtype::SIG_TERMINATE_AND_CLEANUP);
    }
}

void mq::tic_t::abort() {
    for (const auto thread : threads) {
        thread->signal.store(mqtype::SIG_ABORT);
    }
}
