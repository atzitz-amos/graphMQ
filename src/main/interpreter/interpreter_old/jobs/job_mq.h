//
// Created by amosa on 11/23/2024.
//

#ifndef JOB_H
#define JOB_H
#include "threads/communication.h"

namespace graphMQ {
    class _scheduler;
}

namespace graphMQ {
    class job_mq {
    public:
        job_frame* frame;
        shared_interface* si;
        const _scheduler* scheduler;

        int PC = 0;

        explicit job_mq(job_frame* frame, shared_interface* si,
                        const _scheduler* scheduler) : frame(frame), si(si), scheduler(scheduler) {}

        void init() const;
        void step();
    };
}

#endif //JOB_H
