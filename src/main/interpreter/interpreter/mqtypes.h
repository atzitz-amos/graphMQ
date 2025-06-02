//
// Created by amosa on 5/19/2025.
//

#ifndef FRAME_H
#define FRAME_H
#include <thread>

namespace mqtype {
    typedef unsigned char byte;
    typedef unsigned char* instr_t;

    struct mq_method {
        int location;

        int stack_size;
        int locals_count;

        intptr_t* const_pool;

        ~mq_method() {
            delete[] const_pool;
            const_pool = nullptr;
        }
    };

    struct mq_program {
        instr_t instructions;
        mq_method* mq_methods;

        mq_method* entry_point;

        ~mq_program() {
            delete[] mq_methods;
            delete[] instructions;
            mq_methods = nullptr;
            instructions = nullptr;
            entry_point = nullptr;
        }
    };


    struct frame {
        int location;
        int PC = 0;

        int stack_size = 0;
        int locals_count = 0;

        intptr_t* stack;
        intptr_t* SP = stack;

        intptr_t* locals_pool;
        intptr_t* args_pool;

        intptr_t* const_pool;

        intptr_t* return_value_addr;

        frame* previous_frame = nullptr;

        frame(const mq_method* method, intptr_t* args_ptr, intptr_t* return_value_addr) :
            location(method->location),
            stack_size(method->stack_size),
            locals_count(method->locals_count),
            stack(new intptr_t[method->stack_size]),
            locals_pool(new intptr_t[method->locals_count]),
            args_pool(args_ptr),
            const_pool(method->const_pool),
            return_value_addr(return_value_addr) {}

        ~frame() {
            delete[] stack;
            delete[] locals_pool;
            stack = nullptr;
            locals_pool = nullptr;
            args_pool = nullptr;
            return_value_addr = nullptr;
        }
    };

    struct task {
        frame* current_frame = nullptr;

        bool CS_FLAG = false;
        bool AC_FLAG = false;

        bool isScheduled = false;

        int ac_return_value = 0;

        ~task() {
            delete current_frame;
            current_frame = nullptr;
        }
    };

    struct runtime_exec_info {
        int return_code;
        double execution_time;
    };

    enum tic_signal {
        SIG_KEEP_ALIVE = 0, // Keep alive, means if no tasks are scheduled, the interpreter should not exit
        SIG_NO_MORE_TASKS = 1, // No More Tasks, means the interpreter should exit if no tasks are scheduled
        SIG_TERMINATE_AND_CLEANUP = 2, // Terminate the interpreter as soon as possible and cleanup
        SIG_ABORT = 3 // Abort the interpreter, critical error happened
    };

    namespace bytecodes {
        #define DEFINE_OPCODE(name) constexpr mqtype::byte name = __COUNTER__;

        // Define opcodes with automatic incrementing values
        DEFINE_OPCODE(NOP)
        DEFINE_OPCODE(LD_LOCAL)
        DEFINE_OPCODE(LD_CONST)
        DEFINE_OPCODE(LD_ARG)
        DEFINE_OPCODE(LDI)
        DEFINE_OPCODE(LDII)
        DEFINE_OPCODE(LDIII)
        DEFINE_OPCODE(LDIIII)
        DEFINE_OPCODE(STORE_LOCAL)
        DEFINE_OPCODE(ADD)
        DEFINE_OPCODE(SUB)
        DEFINE_OPCODE(MUL)
        DEFINE_OPCODE(DIV)
        DEFINE_OPCODE(MOD)
        DEFINE_OPCODE(NEG)
        DEFINE_OPCODE(IP_ADD)
        DEFINE_OPCODE(IP_SUB)
        DEFINE_OPCODE(IP_MUL)
        DEFINE_OPCODE(IP_DIV)
        DEFINE_OPCODE(IP_MOD)
        DEFINE_OPCODE(IP_NEG)
        DEFINE_OPCODE(OR)
        DEFINE_OPCODE(AND)
        DEFINE_OPCODE(XOR)
        DEFINE_OPCODE(NOT)
        DEFINE_OPCODE(IP_OR)
        DEFINE_OPCODE(IP_AND)
        DEFINE_OPCODE(IP_XOR)
        DEFINE_OPCODE(IP_NOT)
        DEFINE_OPCODE(CALL)
        DEFINE_OPCODE(SCHED)
        DEFINE_OPCODE(AWAIT)
        DEFINE_OPCODE(RET)
        DEFINE_OPCODE(IF_CMPGE)
        DEFINE_OPCODE(IF_CMPGT)
        DEFINE_OPCODE(IF_CMPEQ)
        DEFINE_OPCODE(IF_CMPNE)
        DEFINE_OPCODE(IF_CMPLT)
        DEFINE_OPCODE(IF_CMPLE)
        DEFINE_OPCODE(JMP)

        DEFINE_OPCODE(DEBUG);

        static constexpr int MAX_OPCODE = __COUNTER__ - 1; // Maximum opcode value
    }

    enum status_value {
        STATUS_OK = 0,
        STATUS_ERROR_SHOULD_ABORT = 1,
        STATUS_TASK_OVER = 2
    };

    struct status_t {
        char* message;
        status_value value;

        static status_t error(char* message) {
            return {message, STATUS_ERROR_SHOULD_ABORT};
        }

        static status_t ok() {
            return {nullptr, STATUS_OK};
        }

        static status_t task_over() {
            return {nullptr, STATUS_TASK_OVER};
        }
    };
}

#endif //FRAME_H
