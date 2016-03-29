package com.axt.jvmcallgraph;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.objectweb.asm.ClassReader;

public interface BytecodeSource {
	List<ClassReader> getClassReaders() throws IOException;
}