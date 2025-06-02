//
// Created by amosa on 5/18/2025.
//

#include <iostream>

#include "vm_t.h"

int main() {
    const auto path = "./bytecode_out.byc";

    const auto entrypoint = new mqtype::mq_method[1];
    entrypoint->location = 0;
    entrypoint->stack_size = 2;
    entrypoint->locals_count = 1;
    entrypoint->const_pool = nullptr;

    auto* instructions = new mqtype::byte[]{
        mqtype::bytecodes::LDIII, 0xFF, 0xFF, 0xFF,
        mqtype::bytecodes::STORE_LOCAL, 0x0,
        mqtype::bytecodes::LD_LOCAL, 0x0,
        mqtype::bytecodes::LDI, 0x0,
        mqtype::bytecodes::IF_CMPEQ, 0x12,
        mqtype::bytecodes::LDI, 0x1,
        mqtype::bytecodes::IP_SUB, 0x0,
        mqtype::bytecodes::JMP, 0x6,
        mqtype::bytecodes::DEBUG,
        mqtype::bytecodes::RET
    };

    auto* program = new mqtype::mq_program();
    program->entry_point = entrypoint;
    program->mq_methods = entrypoint;
    program->instructions = instructions;

    const auto vm = new mq::vm_t(program);
    MQ_InitVM(vm);

    auto [return_code, execution_time] = MQ_InterpretBegin(vm);

    std::cout << "Interpreter finished execution." << std::endl << "Total execution time: "
        << execution_time * 1000 << " ms." << std::endl;


    MQ_VMCleanup(vm);


    return 0;
}
