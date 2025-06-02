//
// Created by amosa on 11/23/2024.
//

#include "jobmanager.h"

#include <iostream>
#include <thread>
#include <vector>


#include "threads/jobthread.h"

namespace graphMQ {
    void jobmanager::cleanup() const {}

    void jobmanager::start() {
        auto* frame = new job_frame();

        frame->nd = bc.nodes[0]; // Entrypoint

        frame->locals = bc.nodes[0].locSize == 0 ? nullptr : new int[bc.nodes[0].locSize];
        frame->stack = bc.nodes[0].stackSize == 0 ? nullptr : new int[bc.nodes[0].stackSize];
        frame->arguments = nullptr;
        frame->closure = nullptr; // TODO
        frame->SP = frame->stack;

        scheduler->schedule(frame, si);

        started = true;
    }

    const shared_interface* _scheduler::schedule(const job_mq& job) {
        std::unique_lock lock(m_mutex);
        jobs.push(job);
        m_cond.notify_one();

        return job.si;
    }

    const shared_interface* _scheduler::schedule(job_frame* frame, shared_interface* si) {
        const job_mq job(frame, si, this);
        this->schedule(job);
        return si;
    }

    job_mq _scheduler::fetch() {
        std::unique_lock lock(m_mutex);
        m_cond.wait(lock,
                    [this] {
                        return manager->started && (!jobs.empty() || running_count == 0);
                    });
        if (manager->si->signal == signals::MJOB_HALT) {
            return job_mq(nullptr, nullptr, nullptr);
        }
        const job_mq job = jobs.front();
        jobs.pop();

        running_count++;

        return job;
    }

    job_mq _scheduler::get() const {
        return jobs.front();
    }

    void _scheduler::complete() {
        --running_count;
    }


    void start_jobs(jobmanager& manager) {
        const auto thread_count = std::thread::hardware_concurrency();
        if (thread_count == 0) {
            throw std::domain_error("Error: thread count couldn't be retrieved");
        }

        std::vector<std::thread> threads;
        for (int i = 0; i < thread_count; i++) {
            auto jb = new jobthread(manager.scheduler);

            threads.emplace_back(jobthread::run, jb);
        }

        manager.start();

        for (std::thread& th : threads) {
            th.join();
        }
    }
}
