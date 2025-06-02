//
// Created by amosa on 11/23/2024.
//

#include "job_mq.h"

#include <iostream>


using namespace graphMQ;

int load_value(const uint8_t lbl, const job_frame* frame, const uint8_t* node, const int offset) {
    switch (lbl) {
        case 0b000: {
            return (node[offset] << 8) | node[offset + 1];
        }
        case 0b001: {
            return frame->locals[node[offset]];
        }
        case 0b010: {
            return frame->arguments[node[offset]];
        }
        case 0b011: {
            // TODO, constants
            return 0;
        }
        case 0b100: {
            // TODO, itervariables
            return 0;
        }
        case 0b101: {
            // TODO, closure
            return 0;
        }
        case 0b110: {
            const auto* pointer = reinterpret_cast<int*>(frame->SP[0]);
            return pointer[node[offset]];
        }
        case 0b111: {
            return (node[offset] << 8) | node[offset + 1];
        }
        default: {
            return 0;
        }
    }
}

void store_value(const uint8_t lbl, int value, const job_frame* frame, const uint8_t* node, const int offset) {
    switch (lbl) {
        case 0b000: {
            throw std::runtime_error("Cannot store to constant");
        }
        case 0b001: {
            frame->locals[node[offset]] = value;
            break;
        }
        case 0b010: {
            frame->arguments[node[offset]] = value;
            break;
        }
        case 0b011: {
            // TODO, constants
            break;
        }
        case 0b100: {
            // TODO, itervariables
            break;
        }
        case 0b101: {
            // TODO, closure
            break;
        }
        case 0b110: {
            auto* pointer = reinterpret_cast<int*>(frame->SP[0]);
            pointer[node[offset + 1]] = value;
            break;
        }
        default: {};
    }
}

void job_mq::init() const {
    si->set_signal(signals::XJOB_RUNNING);
}

void job_mq::step() {
    const node_t nd = frame->nd;

    if (PC >= sizeof(nd.data)) {
        si->set_signal(signals::XJOB_COMPLETED);
        return;
    }

    std::cout << "Executing instruction " << PC << std::endl;

    const uint8_t instr = nd.data[PC] >> 3;
    uint8_t lbl = nd.data[PC] & 0x7;


    switch (instr) {
        case 0b00000: {
            // PUSH
            std::cout << "PUSH" << std::endl;
            *(frame->SP++) = load_value(lbl, frame, nd.data, PC + 1);
            break;
        }
        case 0b00001: {
            // STORE
            std::cout << "STORE" << std::endl;

            store_value(lbl, *(frame->SP--), frame, nd.data, PC + 1);
            break;
        }
        case 0b00010: {
            // I_NEG

            if (lbl == 0b111) {
                *(frame->SP) = -*(frame->SP);
            }
            else {
                store_value(lbl, -load_value(lbl, frame, nd.data, PC + 1), frame, nd.data, PC + 1);
            }
            break;
        }
        case 0b00011: {
            // B_NOT

            if (lbl == 0b111) {
                *(frame->SP) = !*(frame->SP);
            }
            else {
                store_value(lbl, !load_value(lbl, frame, nd.data, PC + 1), frame, nd.data, PC + 1);
            }
        }
        case 0b00100: {
            // I_ADD
        }
        case 0b00101: {
            // I_SUB
            break;
        }
        case 0b00110: {
            // I_MUL
            break;
        }
        case 0b00111: {
            // I_DIV
            break;
        }
        case 0b10000: {
            // INVOKE
            lbl >>= 1;
            if (lbl == 0b00) {
                // RET
            }
            else if (lbl == 0b01) {
                // AWAIT
            }
            else if (lbl == 0b10) {
                // ASYNCCALL
            }
            else {
                // JOIN
            }
            break;
        }
        case 0b01100: {
            // IP_ADD
            break;
        }
        case 0b01101: {
            // IP_SUB
            break;
        }
        case 0b01110: {
            // IP_MUL
            break;
        }
        case 0b01111: {
            // IP_DIV
            break;
        }
        case 0b10001: {
            // CMP_GT
            break;
        }
        case 0b10010: {
            // CMP_EQ
            break;
        }
        case 0b10011: {
            // CMP_GE
            break;
        }
        case 0b10100: {
            // CMP_LT
            break;
        }
        case 0b10101: {
            // CMP_NE
            break;
        }
        case 0b10110: {
            // CMP_LE
            break;
        }
        case 0b11000: {
            // MAKEARRAY
            break;
        }
        case 0b11001: {
            // ARRACCESS
            break;
        }
        case 0b11010: {
            // ARRSTORE
            break;
        }
        case 0b11011: {
            // ARRLENGTH
            break;
        }

        default: {
            si->set_signal(signals::XJOB_ERROR);
        }
    };

    PC += 4;
}
