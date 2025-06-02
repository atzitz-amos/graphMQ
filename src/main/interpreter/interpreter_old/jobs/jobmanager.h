//
// Created by amosa on 11/23/2024.
//

#ifndef JOBMANAGER_H
#define JOBMANAGER_H

#include <condition_variable>
#include <queue>
#include <vector>

#include "job_mq.h"
#include "../bytecode/bytecode.h"
#include "./threads/jobthread.h"
#include "threads/communication.h"

namespace graphMQ {
    class jobmanager;

    class _scheduler {
        const jobmanager* manager;

        std::queue<job_mq> jobs{};
        std::mutex m_mutex{};
        std::condition_variable m_cond{};

        int running_count = 0;

    public:
        explicit _scheduler(const jobmanager* manager) : manager(manager) {}

        const shared_interface* schedule(const job_mq& job);
        const shared_interface* schedule(job_frame* frame, shared_interface* si);
        job_mq fetch();
        job_mq get() const;

        void complete();
    };

    class jobmanager {
        const bytecode_t bc;

    public:
        shared_interface* si = new shared_interface();
        std::vector<jobthread*> threads;

        _scheduler* scheduler = new _scheduler(this);

        bool started = false;

        explicit jobmanager(const bytecode_t bc) : bc(bc) {}

        ~jobmanager() {
            delete si;
            delete scheduler;

            for (const jobthread* jth : threads) {
                delete jth;
            }
        }

        void cleanup() const;
        void start();
    };

    void start_jobs(jobmanager& manager);
}

#endif //JOBMANAGER_H
