//
// Created by amosa on 20.09.2024.
//

#include "main.h"
#include <iostream>
#include <fstream>
#include <string>
#include <cstdint>
#include <thread>
#include <vector>

#include "bytecode/bytecode.h"
#include "jobs/jobmanager.h"


graphMQ::bytecode load_from_bytecode(const std::string& string) {
    std::cout << "Loading from bytecode file... " << std::endl;
    std::vector<graphMQ::node_t> nodes;

    int i = 0;
    int size = 0;
    while (i < string.length()) {
        const uint8_t name = string[i++];
        const uint8_t length = string[i++] - 3;
        const bool isHeavy = string[i++] != '0';
        const uint8_t stackSize = string[i++];
        const uint8_t locSize = string[i++];

        auto* data = new uint8_t[length];
        memcpy(data, string.c_str() + i, length);
        i += length;

        nodes.emplace_back(name, stackSize, locSize, isHeavy, data);
        size++;
    }

    auto* nodes_arr = new graphMQ::node_t[size];
    for (const auto& nd : nodes) {
        if (nd.name >= size) {
            throw std::out_of_range(
                "Error: Out of range id for node `" + std::to_string(nd.name) +
                "`, ids are expected to start at 0 and follow one another without skipping a number. ID `" +
                std::to_string(nd.name) + "is consequently out of range: " + std::to_string(size));
        }
        nodes_arr[nd.name] = nd;
    }
    return graphMQ::bytecode(nodes_arr);
}


int main(const int argc, char* argv[]) {
    if (argc != 2) {
        std::cerr << "Usage: " << argv[0] << " <filename>" << std::endl;
        return 1;
    }
    std::ifstream file(argv[1]);
    if (!file.is_open()) {
        std::cerr << "Could not open file: " << argv[1] << std::endl;
        return 1;
    }
    const graphMQ::bytecode bc = load_from_bytecode(std::string((std::istreambuf_iterator(file)),
                                                                std::istreambuf_iterator<char>()));

    graphMQ::jobmanager manager(bc);
    start_jobs(manager);

    manager.cleanup();

    return 0;
}
