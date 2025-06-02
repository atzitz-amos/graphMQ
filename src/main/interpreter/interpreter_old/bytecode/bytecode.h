//
// Created by amosa on 11/23/2024.
//

#ifndef BYTECODE_H
#define BYTECODE_H
#include <cstdint>


namespace graphMQ {
    class node;
    class bytecode;
    typedef node node_t;
    typedef bytecode bytecode_t;


    class node {
    public:
        uint8_t name;

        uint8_t stackSize;
        uint8_t locSize;
        bool isHeavy;

        uint8_t* data = nullptr;

        node() : name(0), stackSize(0), locSize(0), isHeavy(false) {}

        node(const uint8_t name, const uint8_t stackSize, const uint8_t locSize, const bool isHeavy,
             uint8_t* data)
            : name(name), stackSize(stackSize), locSize(locSize), isHeavy(isHeavy), data(data) {}

        ~node() = default;
    };

    class bytecode {
    public:
        node_t* nodes;
        explicit bytecode(node_t* nodes): nodes(nodes) {}

        ~bytecode() {}
    };
}

#endif //BYTECODE_H
