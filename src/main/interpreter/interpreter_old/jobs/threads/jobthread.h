//
// Created by amosa on 11/23/2024.
//

#ifndef JOBTHREAD_H
#define JOBTHREAD_H

namespace graphMQ {
    class _scheduler;
}

namespace graphMQ {
    class jobthread {
    public:
        _scheduler* scheduler;

        explicit jobthread(_scheduler* scheduler) : scheduler(scheduler) {}

        ~jobthread() {
        }

        void run() const;
    };
}


#endif //JOBTHREAD_H
