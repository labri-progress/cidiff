package org.github.cidiff.clients;

import org.github.cidiff.Action;
import org.github.cidiff.Line;
import org.github.cidiff.Options;
import org.github.cidiff.Pair;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class FilteredMonacoClient extends AbstractDiffClient {

	public FilteredMonacoClient(Pair<List<Line>> lines, Pair<List<Action>> actions) {
		super(lines, actions);
	}
	
	@Override
	public void execute(Options options) {
		DefaultMustacheFactory factory = new DefaultMustacheFactory();
		Mustache mustache = factory.compile("filtered.mustache");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(options.monacoOutput()));
			mustache.execute(writer, Map.of(
				"left-lines", this.lines.left().stream().map(Line::value).map(l -> l.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"").replaceAll("</", "\\\\<\\\\/")).toList(),
				"right-lines", this.lines.right().stream().map(Line::value).map(l -> l.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"").replaceAll("</", "\\\\<\\\\/")).toList(),
				"right-actions", this.actions.right().stream().map(A::from).toList()
			));
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private record A(String type, int left, int right) {
		private static A from(Action action) {
			return switch(action.type()) {
				case ADDED -> new A(action.type().toString().toLowerCase(), -1, action.right().index());
				case DELETED -> new A(action.type().toString().toLowerCase(), action.left().index(), -1);
				default -> new A(action.type().toString().toLowerCase(), action.left().index(), action.right().index());
			};
		}
	}

}
