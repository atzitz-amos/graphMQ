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

        std::atomic<int> task_count = 0; // Number of tasks currently running

    public:
        explicit tic_t(vm_t* interpreter_instance): interpreter_instance(interpreter_instance) {}

        void Run();
        void Join() const;

        void Terminate();

        void Abort();

        ~tic_t() {
            for (const auto& thread : threads) {
                if (thread->thread && thread->thread->joinable()) {
                    thread->thread->join();
                }
                delete thread;
            }
            threads.clear();
        }


        void IncrTaskCount() {
            ++task_count;
        }

        void DecrTaskCount() {
            --task_count;
        }

        bool NoMoreTasks() const {
            return task_count.load() == 0;
        }
    };
}
#endif //TIC_H
