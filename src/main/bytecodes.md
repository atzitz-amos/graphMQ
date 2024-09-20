# GraphMQ ByteCodes
## General Bytecode syntax
Bytecode can be represented either in bytes or in human-readable format. The following bytecodes describe the human-readable format.
Every entry still specifies under `Syntax` the bytes representing the instruction.

Every bytecode in the byte format consists of four bytes.
 - *Byte 1* — Bytecode of the instruction
 - *Byte 2* — Arg 0
 - *Byte 3* — Arg 1
 - *Byte 4* — Arg 2

The three lowest bits of the bytecode *(in lightblue in the documentation)* represents the instruction's first argument type as of:
| LSb | Meaning                                                       | Notation in human-readable format |
|-----|---------------------------------------------------------------|-----------------------------------|
| 000 | Arg 0 and Arg 1 together represents an integer                | 1234                              |
| 001 | Arg 0 represents an index in the local variables pool         | #1234                             |
| 010 | Arg 0 represents an index in the function args pool           | <1234>                            |
| 011 | Arg 0 represents an index in the constants pool               | *1234                             |
| 100 | Arg 0 represents an index in the itervariables pool           | $1234                             |
| 101 | Arg 0 represents an index in the global variables pool        | ..1234                            |
| 110 | Arg 0 represents an offset from the pointer in `TOS`          | .1234                             |
| 111 | Arg 0 and Arg 1 together represents an offset in the bytecode | ->1234                            |

# Stack-Based operations

## PUSH
The `push` operation pushes `ARG0` onto the stack depending on the three lowest bits, i.e. 
```
push #0;
```
pushes the first local variable onto the stack.

Syntax:
$${\color{gray}00000\color{lightblue}bbb}$$

> [!NOTE]
> If an integer is too big to fit in the two bytes, it should be stored into the constants pool and the corresponding `push` command should be used.


## STORE
The `store` operation stores `TOS` into the corresponding place described by `ARG0`, i.e.
```
store ..0;
```
stores `TOS` into the first global variable.

Syntax:
$${\color{gray}00001\color{lightblue}bbb}$$

> [!IMPORTANT]
> Using this instruction with LSb `110` will result in an offset from `TOS1` instead of `TOS`, `TOS` already being used.


# Arithmetic and Logic
## I_NEG / B_NEG
Performs integer negation or boolean negation. Replace the variable pointed to by `ARG0` with the result, i.e.
```
i_neg #0;
```
performs: `locals[0] = -locals[0]`

If the three `LSb` are `000`, the operation will execute on the stack, i.e.
```
i_neg;
```
performs `TOS = -TOS`

Syntax:
$${\color{gray}00010\color{lightblue}bbb}$$ and $${\color{gray}00011\color{lightblue}bbb}$$

> [!IMPORTANT]
> Using this instruction with LSb `110` will result in an offset from `TOS1` instead of `TOS`, `TOS` already being used.


## I_ADD / I_SUB / I_MUL / I_DIV
Pops `TOS` from the stack and pushes the result of the corresponding operation with `ARG0`, i.e.
```
i_add <0>;
```
will pop `TOS` add it with the first argument of the current function and push the result back onto the stack.

Syntax:
$${\color{gray}001\color{pink}xx\color{lightblue}bbb}$$, where $${\color{pink}xx}$$ specifies the operation, as of:
| Code | Operation |
|------|-----------|
| 00   | I_ADD     |
| 01   | I_SUB     |
| 10   | I_MUL     |
| 11   | I_DIV     |

> [!IMPORTANT]
> Using this instruction with LSb `110` will result in an offset from `TOS1` instead of `TOS`, `TOS` already being used.


## IP_ADD / IP_SUB / IP_MUL / IP_DIV
Performs the corresponding inplace operation on the variable pointed to by `ARG0` with `TOS`, i.e.
```
ip_add $0;
```
performs: `itervars[0] += TOS`

Syntax:
$${\color{gray}011\color{pink}xx\color{lightblue}bbb}$$, where $${\color{pink}xx}$$ specifies the operation, as of:
| Code | Operation |
|------|-----------|
| 00   | IP_ADD    |
| 01   | IP_SUB    |
| 10   | IP_MUL    |
| 11   | IP_DIV    |

> [!IMPORTANT]
> Using this instruction with LSb `110` will result in an offset from `TOS1` instead of `TOS`, `TOS` already being used.


## CMP_LT / CMP_LE / CMP_EQ / CMP_NQ / CMP_GT / CMP_GE
Pops `TOS` and `TOS1`. Performs the corresponding comparison. If the three `LSb` are `111`, jumps if true to `ARG0`, else pushes the result onto the stack, i.e.
```
cmp_lt ->2;
```
jumps to bytecode offset of 2 if `TOS1 < TOS`.

Syntax:
$${\color{gray}10\color{pink}xxx\color{lightblue}bbb}$$, where $${\color{pink}xxx}$$ specifies the operation, as of:
| Name | Operation   | Bytecode modifier |
|------|-------------|-------------------|
| LT   | TOS1 < TOS  | 100               |
| LE   | TOS1 <= TOS | 110               |
| EQ   | TOS1 == TOS | 010               |
| NQ   | TOS1 != TOS | 101               |
| GT   | TOS1 > TOS  | 001               |
| GT   | TOS1 >= TOS | 011               |


# For loops
## O_EFOR / U_EFOR
Ordered element for loop / Unordered element for loop: Iterates over `TOS` either in unordered, parallelized way, or ordered way. The element at any given iteration will be stored under `itervars[LOOP_NESTING_LEVEL]`. The three `LSb` must be set to `111` and `ARG0` indicates the end of the loop in the bytecode, i.e.
```
0: push #0;
1: o_efor ->3;
2: push $0;
3: ip_add #1;
```
This code iterates over the first local variable adding every element to the second local variable. Concretely, performs this pseudo code:
```
for each (element of localvars[0]) {
  localvars[1] += element;
}
```

Syntax: 
$${\color{gray}0100\color{pink}x\color{lightblue}bbb}$$, with $${\color{pink}x}$$ indicating whether the loop is ordered or not: 1 means ordered, 0 unordered.

> [!NOTE]
> This code is equivalent:
> ```
> 0: push #0;
> 1: o_efor {
>   2: push $0;
>   3: ip_add #1;
> }
> ```



# All Instructions Table
| Bytecode                                    | Instruction |
|---------------------------------------------|-------------|
| $${\color{gray}00000\color{lightblue}bbb}$$ | PUSH        |
| $${\color{gray}00001\color{lightblue}bbb}$$ | STORE       |
| $${\color{gray}00010\color{lightblue}bbb}$$ | I_NEG       |
| $${\color{gray}00011\color{lightblue}bbb}$$ | B_NEG       |
| $${\color{gray}00100\color{lightblue}bbb}$$ | I_ADD       |
| $${\color{gray}00101\color{lightblue}bbb}$$ | I_SUB       |
| $${\color{gray}00110\color{lightblue}bbb}$$ | I_MUL       |
| $${\color{gray}00111\color{lightblue}bbb}$$ | I_DIV       |
| $${\color{gray}01000\color{lightblue}bbb}$$ | U_EFOR      |
| $${\color{gray}01001\color{lightblue}bbb}$$ | O_EFOR      |
| $${\color{gray}01010\color{lightblue}bbb}$$ | U_IFOR      |
| $${\color{gray}01011\color{lightblue}bbb}$$ | O_IFOR      |
| $${\color{gray}01100\color{lightblue}bbb}$$ | IP_ADD      |
| $${\color{gray}01101\color{lightblue}bbb}$$ | IP_SUB      |
| $${\color{gray}01110\color{lightblue}bbb}$$ | IP_MUL      |
| $${\color{gray}01111\color{lightblue}bbb}$$ | IP_DIV      |
| $${\color{gray}10000\color{lightblue}bbb}$$ | RET         |
| $${\color{gray}10001\color{lightblue}bbb}$$ | CMP_GT      |
| $${\color{gray}10010\color{lightblue}bbb}$$ | CMP_EQ      |
| $${\color{gray}10011\color{lightblue}bbb}$$ | CMP_GE      |
| $${\color{gray}10100\color{lightblue}bbb}$$ | CMP_LT      |
| $${\color{gray}10101\color{lightblue}bbb}$$ | CMP_NQ      |
| $${\color{gray}10110\color{lightblue}bbb}$$ | CMP_LE      |
| $${\color{gray}10111\color{lightblue}bbb}$$ | CALL        |
| $${\color{gray}11000000}$$                  | I_MAKEARRAY |
| $${\color{gray}11000001}$$                  | B_MAKEARRAY |
| $${\color{gray}11000010}$$                  | F_MAKEARRAY |
| $${\color{gray}11000011}$$                  | R_MAKEARRAY |
| $${\color{gray}11001\color{lightblue}bbb}$$ | ARRACCESS   |
| $${\color{gray}11010\color{lightblue}bbb}$$ | ARRAYLENGTH |
| $${\color{gray}11011\color{lightblue}bbb}$$ |             |
| $${\color{gray}11100\color{lightblue}bbb}$$ |             |
| $${\color{gray}11101\color{lightblue}bbb}$$ |             |
| $${\color{gray}11110\color{lightblue}bbb}$$ |             |
| $${\color{gray}11111\color{lightblue}bbb}$$ |             |

