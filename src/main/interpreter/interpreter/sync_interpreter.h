//
// Created by amosa on 5/22/2025.
//

#ifndef SYNCINTERPRETER_H
#define SYNCINTERPRETER_H
#include <functional>

#include "mqtypes.h"


#define MQ_StackReduce(op) \
    temp = MQ_StackPop(); MQ_StackPush(MQ_StackPop() op temp)

#define MQ_DefineOP(op, handler) \
    op: handler \
    goto CLEANUP;

#define ARG0 fetch_arg(0)
#define ARG1 fetch_arg(1)
#define ARG2 fetch_arg(2)
#define ARG3 fetch_arg(3)

namespace mq {
    class sync_interpreter {
        const mqtype::mq_program* program;
        mqtype::task* task;

        const mqtype::instr_t base_instr_addr = program->instructions + task->current_frame->location;

    public:
        explicit sync_interpreter(const mqtype::mq_program* program,
                                  mqtype::task* task) : program(program), task(task) {}

        mqtype::status_t step() const;

        mqtype::instr_t current_instr() const {
            return base_instr_addr + task->current_frame->PC;
        }

        mqtype::frame* current_frame() const {
            return task->current_frame;
        }

        intptr_t fetch_arg(const int offset) const {
            return current_instr()[offset + 1];
        }

        void MQ_StackPush(const intptr_t value) const {
            *task->current_frame->SP++ = value;
        }

        intptr_t MQ_StackPop() const {
            return *--task->current_frame->SP;
        }

        void MQ_PCIncr(const int value = 1) const {
            task->current_frame->PC += value;
        }

        void MQ_SetPC(const int value) const {
            task->current_frame->PC = value;
        };
    };
}

#endif //SYNCINTERPRETER_H
