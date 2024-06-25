package org.github.cidiff.clients;

import org.github.cidiff.Action;
import org.github.cidiff.Line;
import org.github.cidiff.Options;
import org.github.cidiff.Pair;
import org.github.cidiff.Utils;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class FilteredMonacoClient extends AbstractDiffClient {

	public FilteredMonacoClient(Pair<List<Line>> lines, Pair<List<Action>> actions) {
		// display only the right part, not the left part
		// display 3 lines around each hunks
		// actually send every line but fold the white lines
		super(lines, actions);
	}
	
	@Override
	public void execute(Options options) {
		// TODO: @hubnern the id of the lines is not modified when the lines are alligned, which cause problems in the javascript code because we need to use this id to know the mapping of the line
		DefaultMustacheFactory factory = new DefaultMustacheFactory();
		Mustache mustache = factory.compile("filtered.mustache");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("filtered.html"));
			mustache.execute(writer, Map.of(
				"left-lines", this.lines.left().stream().map(Line::value).map(l -> l.replaceAll("\"", "\\\\\"")).toList(),
				"right-lines", this.lines.right().stream().map(Line::value).map(l -> l.replaceAll("\"", "\\\\\"")).toList(),
				"right-actions", this.actions.right().stream().map(A::from).toList()
			));
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private record Range(int line, int start, int end, boolean whole, String type) {
	}
	private record R(int lline, int lstart, int lend, boolean lwhole, String ltype, int rline, int rstart, int rend, boolean rwhole, String rtype) {
		private static R from(Range l, Range r) {
			return new R(l.line, l.start, l.end, l.whole, l.type, r.line, r.start, r.end, r.whole, r.type);
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
