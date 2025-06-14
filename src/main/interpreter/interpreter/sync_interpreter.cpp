//
// Created by amosa on 5/22/2025.
//

#include "sync_interpreter.h"

#include <iostream>

#include "interpreter_threaded.h"

#include "debug.h"
#include "vm_t.h"

using namespace mqtype::bytecodes;

// @formatter:off

mqtype::status_t mq::sync_interpreter::step() const {
    static constexpr void* jump_table[MAX_OPCODE + 1] = {
        [NOP] = &&op_NOP,
        [LD_LOCAL] = &&op_LD_LOCAL,
        [LD_CONST] = &&op_LD_CONST,
        [LD_ARG] = &&op_LD_ARG,
        [LDI] = &&op_LDI,
        [LDII] = &&op_LDII,
        [LDIII] = &&op_LDIII,
        [LDIIII] = &&op_LDIIII,
        [STORE_LOCAL] = &&op_STORE_LOCAL,
        [ADD] = &&op_ADD,
        [SUB] = &&op_SUB,
        [MUL] = &&op_MUL,
        [DIV] = &&op_DIV,
        [MOD] = &&op_MOD,
        [NEG] = &&op_NEG,
        [IP_ADD] = &&op_IP_ADD,
        [IP_SUB] = &&op_IP_SUB,
        [IP_MUL] = &&op_IP_MUL,
        [IP_DIV] = &&op_IP_DIV,
        [IP_MOD] = &&op_IP_MOD,
        [IP_NEG] = &&op_IP_NEG,
        [OR] = &&op_OR,
        [AND] = &&op_AND,
        [XOR] = &&op_XOR,
        [NOT] = &&op_NOT,
        [IP_OR] = &&op_IP_OR,
        [IP_AND] = &&op_IP_AND,
        [IP_XOR] = &&op_IP_XOR,
        [IP_NOT] = &&op_IP_NOT,
        [CALL] = &&op_CALL,
        [SCHED] = &&op_SCHED,
        [AWAIT] = &&op_AWAIT,
        [RET] = &&op_RET,
        [IF_CMPGE] = &&op_IF_CMPGE,
        [IF_CMPGT] = &&op_IF_CMPGT,
        [IF_CMPEQ] = &&op_IF_CMPEQ,
        [IF_CMPNE] = &&op_IF_CMPNE,
        [IF_CMPLT] = &&op_IF_CMPLT,
        [IF_CMPLE] = &&op_IF_CMPLE,
        [JMP] = &&op_JMP,
        [DEBUG] = &&op_DEBUG // Placeholder for debug operations
    };

    const mqtype::byte instruction = *current_instr();
    if (jump_table[instruction] == nullptr) {
        return mqtype::status_t::error("Invalid instruction encountered: " + std::to_string(instruction));
    }

    intptr_t temp;
    mqtype::frame* new_frame;
    mqtype::task* s_task;

    #ifdef DEBUG_ENABLED
        std::string stack_content = "";
        std::string locals_content = "";
    #endif

    goto *jump_table[instruction];

MQ_DefineOP(op_NOP,
    MQ_PCIncr();)

MQ_DefineOP(op_LD_LOCAL,
    MQ_StackPush(current_frame()->locals_pool[ARG0]);
    MQ_PCIncr(2);)

MQ_DefineOP(op_LD_CONST,
    MQ_StackPush(current_frame()->const_pool[ARG0]);
    MQ_PCIncr(2);)

MQ_DefineOP(op_LD_ARG,
    MQ_StackPush(current_frame()->args_pool[ARG0]);
    MQ_PCIncr(2);)

MQ_DefineOP(op_LDI,
    MQ_StackPush(ARG0);
    MQ_PCIncr(2);)

MQ_DefineOP(op_LDII,
    MQ_StackPush(ARG0 << 8 | ARG1);
    MQ_PCIncr(3);)

MQ_DefineOP(op_LDIII,
    MQ_StackPush(ARG0 << 16 | ARG1 << 8 | ARG2);
    MQ_PCIncr(4);)

MQ_DefineOP(op_LDIIII,
    MQ_StackPush(ARG0 << 24 | ARG1 << 16 | ARG2 << 8 | ARG3);
    MQ_PCIncr(5);)

MQ_DefineOP(op_STORE_LOCAL,
    current_frame()->locals_pool[ARG0] = MQ_StackPop();
    MQ_PCIncr(2);)

MQ_DefineOP(op_ADD,
    MQ_StackReduce(+);
    MQ_PCIncr();)

MQ_DefineOP(op_SUB,
    MQ_StackReduce(-);
    MQ_PCIncr();)

MQ_DefineOP(op_MUL,
    MQ_StackReduce(*);
    MQ_PCIncr();)

MQ_DefineOP(op_DIV,
    // TODO
)

MQ_DefineOP(op_MOD,
    MQ_StackReduce(/);
)

MQ_DefineOP(op_NEG,
    MQ_StackPush(-MQ_StackPop());
)

MQ_DefineOP(op_IP_ADD,
    current_frame()->locals_pool[ARG0] += MQ_StackPop();
    MQ_PCIncr(2);
)

MQ_DefineOP(op_IP_SUB,
    current_frame()->locals_pool[ARG0] -= MQ_StackPop();
    MQ_PCIncr(2);
)

MQ_DefineOP(op_IP_MUL,
    current_frame()->locals_pool[ARG0] *= MQ_StackPop();
    MQ_PCIncr(2);
)

MQ_DefineOP(op_IP_DIV,
    // TODO
)

MQ_DefineOP(op_IP_MOD,
    current_frame()->locals_pool[ARG0] %= MQ_StackPop();
    MQ_PCIncr(2);
)

MQ_DefineOP(op_IP_NEG,
    current_frame()->locals_pool[ARG0] *= -1;
    MQ_PCIncr(2);
)

MQ_DefineOP(op_OR,
    MQ_StackReduce(||);
    MQ_PCIncr();
)

MQ_DefineOP(op_XOR,
    MQ_StackReduce(^);
    MQ_PCIncr();
)

MQ_DefineOP(op_AND,
    MQ_StackReduce(&&);
    MQ_PCIncr();
)

MQ_DefineOP(op_NOT,
    MQ_StackPush(!MQ_StackPop());
    MQ_PCIncr();
)

MQ_DefineOP(op_IP_OR,
    current_frame()->locals_pool[ARG0] |= MQ_StackPop();
    MQ_PCIncr(2);
)

MQ_DefineOP(op_IP_XOR,
    current_frame()->locals_pool[ARG0] ^= MQ_StackPop();
    MQ_PCIncr(2);
)

MQ_DefineOP(op_IP_AND,
    current_frame()->locals_pool[ARG0] &= MQ_StackPop();
    MQ_PCIncr(2);
)

MQ_DefineOP(op_IP_NOT,
    current_frame()->locals_pool[ARG0] = !current_frame()->locals_pool[ARG0];
    MQ_PCIncr(2);
)

MQ_DefineOP(op_CALL,
    new_frame = MQ_NewFrame(program->mq_methods + ARG0, ARG1);
    new_frame->previous_frame = current_frame();

    MQ_PCIncr(3);
    task->current_frame = new_frame;
)

op_SCHED:
    s_task = MQ_CreateTask(MQ_NewFrame(program->mq_methods + ARG0, ARG1));
    current_frame()->locals_pool[ARG2] = reinterpret_cast<intptr_t> (s_task);
    MQ_PCIncr(4);
    vm->tqm.create(s_task, 2);
    goto CLEANUP;


op_AWAIT:
    s_task = reinterpret_cast<mqtype::task*>(current_frame()->locals_pool[ARG0]);
    if (s_task->CS_FLAG) {
        MQ_StackPush(s_task->ac_return_value);
        delete s_task;  // We can safely assume the task is done
        MQ_PCIncr(2);
    } else {
        s_task->AC_TASK_ORIGIN = task;
        s_task->AC_FLAG = true;
        return mqtype::status_t::mark_as_free();
    }
    goto CLEANUP;


MQ_DefineOP(op_RET,
    if (task->isScheduled) {
        task->CS_FLAG = true;
        task->ac_return_value = MQ_StackPop();
        if (task->AC_FLAG) {
            vm->tqm.create(task->AC_TASK_ORIGIN, 1);  // Schedule the task that was waiting for this task
        }
        return mqtype::status_t::mark_as_free();
    } else {
        if (task->current_frame->previous_frame == nullptr) {  // Task is done
            return mqtype::status_t::should_cleanup();
        }

        temp = MQ_StackPop();
        MQ_RestoreFrame();
        MQ_StackPush(temp);
    }
)

MQ_DefineOP(op_IF_CMPGE,
    temp = MQ_StackPop();
    if (MQ_StackPop() >= temp) {
        MQ_SetPC(ARG0);
    } else {
        MQ_PCIncr(2);
    }
)

MQ_DefineOP(op_IF_CMPGT,
    temp = MQ_StackPop();
    if (MQ_StackPop() > temp) {
        MQ_SetPC(ARG0);
    } else {
        MQ_PCIncr(2);
    }
)

MQ_DefineOP(op_IF_CMPEQ,
    temp = MQ_StackPop();
    if (MQ_StackPop() == temp) {
        MQ_SetPC(ARG0);
    } else {
        MQ_PCIncr(2);
    }
)

MQ_DefineOP(op_IF_CMPNE,
    temp = MQ_StackPop();
    if (MQ_StackPop() != temp) {
        MQ_SetPC(ARG0);
    } else {
        MQ_PCIncr(2);
    }
)

MQ_DefineOP(op_IF_CMPLT,
    temp = MQ_StackPop();
    if (MQ_StackPop() < temp) {
        MQ_SetPC(ARG0);
    } else {
        MQ_PCIncr(2);
    }
)

MQ_DefineOP(op_IF_CMPLE,
    temp = MQ_StackPop();
    if (MQ_StackPop() <= temp) {
        MQ_SetPC(ARG0);
    } else {
        MQ_PCIncr(2);
    }
)

MQ_DefineOP(op_JMP,
    MQ_SetPC(ARG0);
)

#ifdef DEBUG_ENABLED
    op_DEBUG:
        for (int i = 0; i < (current_frame()->SP - current_frame()->stack); ++i) {
            stack_content += std::to_string(current_frame()->stack[i]) + ", ";
        }
    for (int i = 0; i < current_frame()->localsc; ++i) {
        locals_content += std::to_string(current_frame()->locals_pool[i]) + ", ";
    }
    print("\nDEBUG:  PC = ", current_frame()->PC, ", SP = ", (current_frame()->SP - current_frame()->stack), "\n",
          "\tCurrent instruction: ", static_cast<int>(current_instr()[0]), "\n", "\tStack contents: stack = ",
          stack_content, "\n", "\tLocals contents: locals = ", locals_content, "\n\n");
    MQ_PCIncr();
    goto CLEANUP;

// MQ_DefineOP(op_DEBUG,
//     for (int i = 0; i < (current_frame()->SP - current_frame()->stack); ++i) {
//         stack_content += current_frame()->stack[i] + " ";
//     }
//     for (int i = 0; i < current_frame()->localsc; ++i) {
//         locals_content += current_frame()->locals_pool[i] + " ";
//     }
//
//     print("\nDEBUG:  PC = ", current_frame()->PC, ", SP = ", (current_frame()->SP - current_frame()->stack), "\n",
//         "\tCurrent instruction: ", static_cast<int>(current_instr()[0]), "\n",
//         "\tStack contents: stack = ", stack_content, "\n",
//         "\tLocals contents: locals = ", locals_content, "\n\n");
//
//     MQ_PCIncr();
// )
#endif

CLEANUP:
    return mqtype::status_t::ok();
}

// @formatter:on
