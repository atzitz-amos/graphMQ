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
    struct vm_t;

    class sync_interpreter {
        const mqtype::mq_program* program;
        mqtype::task* task;

        vm_t* vm;

    public:
        sync_interpreter(const mqtype::mq_program* program, mqtype::task* task, vm_t* vm) : program(program),
            task(task), vm(vm) {}

        mqtype::status_t step() const;

        mqtype::instr_t current_instr() const {
            return program->instructions + task->current_frame->location + task->current_frame->PC;
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

        void MQ_RestoreFrame() const {
            task->current_frame = task->current_frame->previous_frame;
        }

        mqtype::task* MQ_CreateTask(mqtype::frame* frame) const {
            auto* new_task = new mqtype::task();
            new_task->current_frame = frame;
            new_task->isScheduled = true;
            return new_task;
        }

        mqtype::frame* MQ_NewFrame(const mqtype::mq_method* method, const int argc) const {
            auto* args_ptr = new intptr_t[argc];
            for (int i = argc - 1; i >= 0; --i) {
                args_ptr[i] = MQ_StackPop();
            }
            return new mqtype::frame(method, args_ptr);
        }
    };
}

#endif //SYNCINTERPRETER_H
