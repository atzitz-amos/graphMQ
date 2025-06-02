//
// Created by amosa on 5/21/2025.
//

#ifndef TIC_H
#define TIC_H
#include <vector>

#include "interpreter_threaded.h"

namespace mq {
    struct vm_t;

    /**
     * ThreadedInterpreterController
     */
    class tic_t {
        vm_t* interpreter_instance;
        std::vector<threaded_interpreter*> threads;

    public:
        explicit tic_t(vm_t* interpreter_instance): interpreter_instance(interpreter_instance) {}

        void run();
        void join() const;

        void terminate();

        void abort();

        ~tic_t() {
            for (const auto& thread : threads) {
                if (thread->thread && thread->thread->joinable()) {
                    thread->thread->join();
                }
                delete thread;
            }
            threads.clear();
        }
    };
}
#endif //TIC_H
