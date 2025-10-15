package org.github.cidiff.clients;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import org.github.cidiff.Action;
import org.github.cidiff.Line;
import org.github.cidiff.Options;
import org.github.cidiff.Pair;
import org.github.cidiff.Utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class HtmlClient extends AbstractDiffClient {

	public HtmlClient(Pair<List<Line>> lines, Pair<List<Action>> actions) {
		super(lines, actions);
	}

	@Override
	public void execute(Options options) {
		DefaultMustacheFactory factory = new DefaultMustacheFactory();
		Mustache mustache = factory.compile("monaco.mustache");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(options.htmlOutput()));
			Pair.Free<Pair<List<Line>>, Pair<List<Action>>> p = Utils.allignLines(this.lines, this.actions);
			this.lines = p.left();
			this.actions = p.right();
			mustache.execute(writer, Map.of(
				// replace '\' by two '\', and the '"' by '\"' (escape the backslash and double quote)
				// (I need to double escape the backslash in String#replaceAll, it is an escape character for both the string and the pattern D:)
				"left-lines", this.lines.left().stream().map(Line::value).map(l -> l.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"").replaceAll("</", "\\\\<\\\\/")).toList(),
				"right-lines", this.lines.right().stream().map(Line::value).map(l -> l.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"").replaceAll("</", "\\\\<\\\\/")).toList(),
				"left-actions", this.actions.left().stream().map(a -> A.from(a, true)).toList(),
				"right-actions", this.actions.right().stream().map(a -> A.from(a, false)).toList()
			));
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private record A(String type, int line) {
		private static A from(Action action, boolean isLeft) {
			return new A(action.type().toString().toLowerCase(), isLeft ? action.right().index() : action.left().index());
		}
	}


}
