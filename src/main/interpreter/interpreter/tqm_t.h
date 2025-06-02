//
// Created by amosa on 5/21/2025.
//

#ifndef TQM_H
#define TQM_H
#include "mqtypes.h"


namespace mq_queue {
    template <typename T>
    struct node {
        T* element;

        int priority;
        node<T>* next = nullptr;

        explicit node(T* element, const int priority) :
            element(element), priority(priority) {}

        ~node() {
            delete element;
        }
    };

    /**
     * Thread Queue Manager
     */
    template <typename T>
    class priority_queue {
    public:
        node<T>* head;

        std::mutex _mutex;

    public:
        priority_queue() {
            head = nullptr;
        }

        void create(T* element, int priority) {
            std::lock_guard lock(_mutex);

            const auto new_node = new node<T>(element, priority);

            if (head == nullptr) {
                head = new_node;
            }
            else if (head->priority < priority) {
                new_node->next = head;
                head = new_node;
            }
            else {
                node<T>* current = head;
                while (current->next != nullptr && current->next->priority >= priority) {
                    current = current->next;
                }

                node<T>* temp = current->next;
                current->next = new_node;
                new_node->next = temp;
            }
        }

        bool empty() {
            return head == nullptr;
        }

        T* pop() {
            std::lock_guard lock(_mutex);

            if (head == nullptr) {
                throw std::runtime_error("Queue is empty");
            }

            const node<T>* temp = head;
            head = head->next;

            return temp->element;
        }

        ~priority_queue() {
            while (head != nullptr) {
                const auto temp = head;
                head = head->next;
                delete temp;
            }
        }
    };
}

namespace mq {
    class tqm_t : public mq_queue::priority_queue<mqtype::task> {};
}


#endif //TQM_H
