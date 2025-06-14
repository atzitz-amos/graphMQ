//
// Created by amosa on 6/13/2025.
//

#ifndef DEBUG_H
#define DEBUG_H

#define DEBUG_ENABLED


#ifdef DEBUG_ENABLED
#include <iostream>
#include <mutex>

inline std::ostream&
print_one(std::ostream& os) {
    return os;
}

template <class A0, class... Args>
std::ostream&
print_one(std::ostream& os, const A0& a0, const Args&... args) {
    os << a0;
    return print_one(os, args...);
}

template <class... Args>
std::ostream&
print(std::ostream& os, const Args&... args) {
    return print_one(os, args...);
}

inline std::mutex&
get_cout_mutex() {
    static std::mutex m;
    return m;
}

template <class... Args>
std::ostream&
print(const Args&... args) {
    std::lock_guard<std::mutex> _(get_cout_mutex());
    return print(std::cout, args...);
}
#endif


#ifdef DEBUG_ENABLED2
    #define __debug(x) print("[threaded_interpreter ", id, "] ", x, "\n")
#else
    #define __debug(x)
    #define print(...)
#endif


#endif //DEBUG_H
