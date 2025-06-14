//
// Created by amosa on 5/18/2025.
//

#include <iostream>
#include <map>

#include "vm_t.h"

int main() {
    const auto path = "./bytecode_out.byc";

    const auto methods = new mqtype::mq_method[2];
    methods->location = 0;
    methods->stack_size = 2;
    methods->locals_count = 0;
    methods->argc = 0;
    methods->const_pool = nullptr;

    (methods + 1)->location = 10;
    (methods + 1)->stack_size = 5;
    (methods + 1)->locals_count = 2;
    (methods + 1)->argc = 1;
    (methods + 1)->const_pool = nullptr;

    auto* instructions = new mqtype::byte[]{
        mqtype::bytecodes::LDIIII, 0x00, 0x00, 0x00, 0x15,
        mqtype::bytecodes::CALL, 0x1, 0x1,
        mqtype::bytecodes::DEBUG,
        mqtype::bytecodes::RET,
        // fib
        mqtype::bytecodes::LD_ARG, 0x0,
        mqtype::bytecodes::LDI, 0x1,
        mqtype::bytecodes::IF_CMPLE, 0x1E,
        mqtype::bytecodes::LD_ARG, 0x0,
        mqtype::bytecodes::LDI, 0x1,
        mqtype::bytecodes::SUB,
        mqtype::bytecodes::SCHED, 0x1, 0x1, 0x0,
        mqtype::bytecodes::LD_ARG, 0x0,
        mqtype::bytecodes::LDI, 0x2,
        mqtype::bytecodes::SUB,
        mqtype::bytecodes::SCHED, 0x1, 0x1, 0x1,
        mqtype::bytecodes::AWAIT, 0x0,
        mqtype::bytecodes::AWAIT, 0x1,
        mqtype::bytecodes::ADD,
        mqtype::bytecodes::RET,
        mqtype::bytecodes::LD_ARG, 0x0,
        mqtype::bytecodes::RET
    };

    // auto* instructions = new mqtype::byte[]{
    //     mqtype::bytecodes::LDI, 0x1,
    //     mqtype::bytecodes::SCHED, 0x1, 0x1, 0x0,
    //     mqtype::bytecodes::AWAIT, 0x0,
    //     mqtype::bytecodes::DEBUG,
    //     mqtype::bytecodes::RET,
    //     // m1(int)
    //     mqtype::bytecodes::LD_ARG, 0x0,
    //     mqtype::bytecodes::LDI, 0x2,
    //     mqtype::bytecodes::MUL,
    //     mqtype::bytecodes::RET
    // };


    auto* program = new mqtype::mq_program();
    program->entry_point = methods;
    program->mq_methods = methods;
    program->instructions = instructions;

    const auto vm = new mq::vm_t(program);
    MQ_InitVM(vm);

    auto [return_code, execution_time] = MQ_InterpretBegin(vm);

    std::cout << std::endl;

    std::cout << "Interpreter finished execution." << std::endl << "Total execution time: "
        << execution_time * 1000 << " ms." << std::endl;


    MQ_VMCleanup(vm);


    return 0;
}
