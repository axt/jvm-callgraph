package com.axt.jvmcallgraph;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.objectweb.asm.ClassReader;

public class DirectoryBytecodeSource implements BytecodeSource {

	private String directory;

	public DirectoryBytecodeSource(String directory) {
		this.directory = directory;
	}

	@Override
	public List<ClassReader> getClassReaders() throws IOException {
		return Files.walk(Paths.get(directory))
			.sequential()
			.filter(x -> !x.toFile().isDirectory())
			.filter(x -> x.toFile().getAbsolutePath().endsWith(".class"))
			.map(new Function<Path, ClassReader>() {
				@Override
				public ClassReader apply(Path t) {
					try {
						return new ClassReader(new FileInputStream(t.toFile()));
					} catch(IOException e) {
						//TODO
						System.out.println("EXC");
						return null;
					}
				}				
			})
			.filter(x -> x != null)
			.collect(Collectors.toList());
	}
}