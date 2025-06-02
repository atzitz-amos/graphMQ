//
// Created by amosa on 11/23/2024.
//

#include "jobthread.h"
#include "../jobmanager.h"

#include <iostream>


using namespace graphMQ;


void jobthread::run() const {
    // Load job
    while (true) {
        job_mq job = scheduler->fetch();
        if (job.frame == nullptr) {
            std::cout << "Terminating thread" << std::endl;
            return;
        }

        job.init();
        while (job.si->signal != signals::XJOB_COMPLETED) {
            if (job.si->signal == signals::XJOB_ERROR) {
                std::cout << "Error in job" << std::endl;
                break;
            }
            if (job.si->signal == signals::XJOB_WAITING) {
                // TODO
            }
            job.step();
        }

        scheduler->complete();
    }
}
