package com.axt.jvmcallgraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.objectweb.asm.ClassReader;

public class JarBytecodeSource implements BytecodeSource {

	private String jarFileName;

	public JarBytecodeSource(String jarfile) {
		this.jarFileName = jarfile;
	}

	@Override
	public List<ClassReader> getClassReaders() throws IOException {
		try(JarFile jarFile = new JarFile(new File(jarFileName))) {
			return Util.enumerationAsStream(jarFile.entries())
				.filter(x -> !x.isDirectory())
				.filter(x -> x.getName().endsWith(".class"))
				.map(new Function<JarEntry, ClassReader>() {
					@Override
					public ClassReader apply(JarEntry t) {
						try {
							return new ClassReader(jarFile.getInputStream(t));
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
}
