package org.atzitz.graphMQ.interpreter.interpreter.bytecode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public record BytecodeLoader(byte[] bytecode) {

    public static BytecodeLoader of(String bytecodePath) throws IOException {
        byte[] bytecode = Files.readAllBytes(Path.of(bytecodePath));
        return new BytecodeLoader(bytecode);
    }

    public static Bytecode load(String bytecodePath) throws IOException {
        return BytecodeLoader.of(bytecodePath).load();
    }

    public Bytecode load() {
        Bytecode result = new Bytecode();

        int i = 0;
        while (i < this.bytecode.length) {
            byte name = this.bytecode[i++];
            int length = this.bytecode[i++] - 2;
            boolean isHeavy = this.bytecode[i++] != 0;
            int stackSize = Byte.toUnsignedInt(this.bytecode[i++]);
            byte[] data = Arrays.copyOfRange(this.bytecode, i, i + length);
            i += length;

            result.addNode(name, isHeavy, stackSize, data);
        }

        return result;
    }
}
