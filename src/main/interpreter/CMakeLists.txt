cmake_minimum_required(VERSION 3.28)
project(interpreter_old)

set(CMAKE_CXX_STANDARD 23)
set(GCC_COVERAGE_COMPILE_FLAGS "-Wall")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${GCC_COVERAGE_COMPILE_FLACOMPIGS}" )
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3")

#[[
add_executable(interpreter interpreter_old/main.cpp
        interpreter_old/bytecode/bytecode.h
        interpreter_old/jobs/jobmanager.cpp
        interpreter_old/jobs/jobmanager.h
        interpreter_old/jobs/threads/jobthread.cpp
        interpreter_old/jobs/threads/jobthread.h
        interpreter_old/jobs/threads/communication.cpp
        interpreter_old/jobs/threads/communication.h
        interpreter_old/jobs/job_mq.cpp
        interpreter_old/jobs/job_mq.h
)]]

add_executable(interpreter interpreter/main.cpp
        interpreter/main.cpp
        interpreter/mqtypes.h
        interpreter/loader.cpp
        interpreter/loader.h
        interpreter/vm_t.cpp
        interpreter/vm_t.h
        interpreter/tic_t.cpp
        interpreter/tic_t.h
        interpreter/tqm_t.h
        interpreter/interpreter_threaded.cpp
        interpreter/interpreter_threaded.h
        interpreter/sync_interpreter.cpp
        interpreter/sync_interpreter.h
        interpreter/memory.cpp
        interpreter/memory.h
        interpreter/debug.h)