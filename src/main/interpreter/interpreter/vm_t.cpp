//
// Created by amosa on 5/21/2025.
//

#include "vm_t.h"

#include <iostream>
#include "loader.h"


mqtype::task* MQ_CreateEntryTask(const mqtype::mq_program* program) {
    auto* entry_task = new mqtype::task();

    entry_task->current_frame = new mqtype::frame(
        program->entry_point,
        nullptr, // No arguments for the entry point
        nullptr // No return value address for the entry point
    );

    return entry_task;
}

mq::vm_t* mq::MQ_CreateVM(const char* program_path) {
    mqtype::mq_program* program = MQ_LoadFromPath(program_path);
    if (!program) {
        throw std::runtime_error("Failed to load program from path: " + std::string(program_path));
    }
    return new vm_t(program);
}

void mq::MQ_InitVM(vm_t* vm) {
    mqtype::task* entry_task = MQ_CreateEntryTask(vm->program);
    vm->tqm.create(entry_task, 1);
}

mqtype::runtime_exec_info mq::MQ_InterpretBegin(vm_t* vm) {
    vm->tic.run();

    const auto begin = std::chrono::steady_clock::now();
    vm->tic.join();

    mqtype::runtime_exec_info exec_info;
    exec_info.return_code = 0;
    exec_info.execution_time = std::chrono::duration<double>(std::chrono::steady_clock::now() - begin).count();
    return exec_info;
}

void mq::MQ_VMCleanup(const vm_t* vm) {
    delete vm;
}
