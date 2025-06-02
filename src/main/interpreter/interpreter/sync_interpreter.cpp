//
// Created by amosa on 5/22/2025.
//

#include "sync_interpreter.h"

#include <iostream>

#include "interpreter_threaded.h"

using namespace mqtype::bytecodes;

// @formatter:off

mqtype::status_t mq::sync_interpreter::step() const {
    static constexpr void* jump_table[MAX_OPCODE + 1] = {
        [NOP] = &&op_NOP,
        [LD_LOCAL] = &&op_LD_LOCAL,
        [LD_CONST] = &&op_LD_CONST,
        [LD_ARG] = &&op_LD_ARG,
        [LDI] = &&op_LDI,
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
        throw std::runtime_error("Invalid instruction encountered: " + std::to_string(instruction));
    }

    intptr_t temp;

    goto *jump_table[instruction];

MQ_DefineOP(op_NOP,
    MQ_PCIncr();
)

MQ_DefineOP(op_LD_LOCAL,
    MQ_StackPush(current_frame()->locals_pool[ARG0]);
    MQ_PCIncr(2);
)

MQ_DefineOP(op_LD_CONST,
    MQ_StackPush(current_frame()->const_pool[ARG0]);
    MQ_PCIncr(2);
)

MQ_DefineOP(op_LD_ARG,
    MQ_StackPush(current_frame()->args_pool[ARG0]);
    MQ_PCIncr(2);
)

MQ_DefineOP(op_LDI,
    MQ_StackPush(ARG0);
    MQ_PCIncr(2);
)

MQ_DefineOP(op_STORE_LOCAL,
    current_frame()->locals_pool[ARG0] = MQ_StackPop();
    MQ_PCIncr(2);
)

MQ_DefineOP(op_ADD,
    MQ_StackReduce(+);
    MQ_PCIncr();
)

MQ_DefineOP(op_SUB,
    MQ_StackReduce(-);
    MQ_PCIncr();
)

MQ_DefineOP(op_MUL,
    MQ_StackReduce(*);
    MQ_PCIncr();
)

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
    MQ_PCIncr();
)

MQ_DefineOP(op_IP_SUB,
    current_frame()->locals_pool[ARG0] -= MQ_StackPop();
    MQ_PCIncr();
)

MQ_DefineOP(op_IP_MUL,
    current_frame()->locals_pool[ARG0] *= MQ_StackPop();
    MQ_PCIncr();
)

MQ_DefineOP(op_IP_DIV,
    // TODO
)

MQ_DefineOP(op_IP_MOD,
    current_frame()->locals_pool[ARG0] %= MQ_StackPop();
    MQ_PCIncr();
)

MQ_DefineOP(op_IP_NEG,
    current_frame()->locals_pool[ARG0] *= -1;
    MQ_PCIncr();
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
    MQ_PCIncr();
)

MQ_DefineOP(op_IP_XOR,
    current_frame()->locals_pool[ARG0] ^= MQ_StackPop();
    MQ_PCIncr();
)

MQ_DefineOP(op_IP_AND,
    current_frame()->locals_pool[ARG0] &= MQ_StackPop();
    MQ_PCIncr();
)

MQ_DefineOP(op_IP_NOT,
    current_frame()->locals_pool[ARG0] = !current_frame()->locals_pool[ARG0];
    MQ_PCIncr();
)

MQ_DefineOP(op_CALL,
    // TODO
)

MQ_DefineOP(op_SCHED,
    // TODO
)

MQ_DefineOP(op_AWAIT,
    // TODO
)

MQ_DefineOP(op_RET,
    return mqtype::status_t::task_over();
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

MQ_DefineOP(op_DEBUG,
    std::cout << "DEBUG:  PC = " << current_frame()->PC << ", SP = " << (current_frame()->SP - current_frame()->stack) << std::endl
    << "\tCurrent instruction: " << static_cast<int>(current_instr()[0]) << std::endl
    << "\tStack contents: ";
    for (int i = 0; i < (current_frame()->SP - current_frame()->stack); ++i) {
        std::cout << current_frame()->stack[i] << " ";
    }
    std::cout << std::endl << "\tLocals contents: ";
    for (int i = 0; i < current_frame()->locals_count; ++i) {
        std::cout << current_frame()->locals_pool[i] << " ";
    }
    std::cout << std::endl;
    MQ_PCIncr();
)


CLEANUP:
    return mqtype::status_t::ok();
}

// @formatter:on
