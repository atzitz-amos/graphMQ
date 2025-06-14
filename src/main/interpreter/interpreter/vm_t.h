//
// Created by amosa on 5/21/2025.
//

#ifndef INTERPRETER_H
#define INTERPRETER_H

#include "mqtypes.h"
#include "tic_t.h"
#include "tqm_t.h"

#define INTERPRETER_VERSION_MAJOR 1
#define INTERPRETER_VERSION_MINOR 0


namespace mq {
    struct vm_t {
        mqtype::mq_program* program;
        tqm_t tqm;
        tic_t _tic;

        explicit vm_t(mqtype::mq_program* program)
            : program(program), _tic(this) {}

        ~vm_t() {
            delete program;
        }
    };

    vm_t* MQ_CreateVM(const char* program_path);
    void MQ_InitVM(vm_t* vm);

    mqtype::runtime_exec_info MQ_InterpretBegin(vm_t* vm);

    void MQ_VMCleanup(const vm_t* vm);
}


#endif //INTERPRETER_H
