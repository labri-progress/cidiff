package org.github.cidiff.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;

public class GenerateViewers {

	public static void main(String[] args) {

		DefaultMustacheFactory factory = new DefaultMustacheFactory();
		Mustache mustache = factory.compile("evaluation.mustache");
		File f = new File("output/diff/");
		f.mkdirs();
		for (int i = 0; i < 10; i++) {
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter("output/diff/viewer" + i + ".html"));
				mustache.execute(writer, Map.of("num", i, "next", i + 1, "prev", i - 1));
				writer.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
