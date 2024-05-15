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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class MonacoClient extends AbstractDiffClient {

	public MonacoClient(Pair<List<Line>> lines, Pair<List<Action>> actions) {
		super(lines, actions);
	}

	@Override
	public void execute(Options options) {
		// TODO: @hubnern the id of the lines is not modified when the lines are alligned, which cause problems in the javascript code because we need to use this id to know the mapping of the line
		DefaultMustacheFactory factory = new DefaultMustacheFactory();
		Mustache mustache = factory.compile("monaco.mustache");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("output.html"));
			Utils.allignLines(this.lines, this.actions);
			List<Range> left = IntStream.range(0, this.actions.left().size()).mapToObj(i -> convert(this.actions.left().get(i), i, true)).flatMap(l -> l.stream()).toList();
			List<Range> right = IntStream.range(0, this.actions.right().size()).mapToObj(i -> convert(this.actions.right().get(i), i, false)).flatMap(l -> l.stream()).toList();
			List<R> ranges = new ArrayList<>();
			for (int i = 0; i < left.size(); i++) {
				ranges.add(R.from(left.get(i), right.get(i)));
			}
			mustache.execute(writer, Map.of(
				"left-lines", this.lines.left().stream().map(Line::value).map(l -> l.replaceAll("\"", "\\\\\"")).toList(),
				"right-lines", this.lines.right().stream().map(Line::value).map(l -> l.replaceAll("\"", "\\\\\"")).toList(),
				//"left-ranges", IntStream.range(0, this.actions.left().size()).mapToObj(i -> convert(this.actions.left().get(i), i, true)).flatMap(l -> l.stream()).toList(),
				//"right-ranges", IntStream.range(0, this.actions.right().size()).mapToObj(i -> convert(this.actions.right().get(i), i, false)).flatMap(l -> l.stream()).toList(),
				"ranges", ranges,
				"left-actions", this.actions.left().stream().map(a -> A.from(a, true)).toList(),
				"right-actions", this.actions.right().stream().map(a -> A.from(a, false)).toList()
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
	private record A(String type, int line) {
		private static A from(Action action, boolean isLeft) {
			return switch(action.type()) {
				case ADDED, DELETED -> new A(action.type().toString().toString().toLowerCase(), -1);
				default -> new A(action.type().toString().toLowerCase(), isLeft ? action.right().index() : action.left().index());
			};
			//if (action.right() == null || action.left() == null) {
			//	System.out.println(action);
			//}
			//return new A(action.type().toString().toLowerCase(), isLeft ? action.right().index() : action.left().index());
		}
	}

	private List<Range> convert(Action action, int i, boolean isLeft) {
		if (isLeft) {
			return convert(action.type(), action.left(), action.right(), i, true);
		} else {
			return convert(action.type(), action.right(), action.left(), i, false);
		}
	}

	private List<Range> convert(Action.Type type, Line left, Line right, int index, boolean isLeft) {
		String t = type.toString().toLowerCase();
		if ((type == Action.Type.DELETED && !isLeft) || (type == Action.Type.ADDED && isLeft)) {
			t = t + "-light";
		}
		return switch (type) {
			case UPDATED, MOVED_UPDATED -> {
				ArrayList<Range> l = new ArrayList<>();
				if (type == Action.Type.MOVED_UPDATED) {
					l.add(new Range(index+1, 1, left.value().length(), true, t));
				}
				String[] sl = left.value().split("\s");
				String[] sr = right.value().split("\s");
				int x=0;
				int j = 0;
				for (int i = 0; i < sl.length; ++i) {
					if (sl[i].isEmpty()) { ++x; continue; }  // was empty (because more than one space between words), skip this one a account for it
					if (sr[j].isEmpty()) { --i; ++j; continue; }  // was empty on right side, increment j and decrement i to stay on the same i on the next iteration
					if (!sl[i].equals(sr[j])) {
						l.add(new Range(index+1, x+1, x+1+sl[i].length(), false, t));
					}
					x += sl[i].length() + 1;
					++j;
				}
				yield l;
			}
			case MOVED_UNCHANGED -> List.of(new Range(index+1, 1, left.value().length(), true, t));
			case UNCHANGED -> List.of();
			default -> List.of(new Range(index+1, 1, left.value().length(), true, t));
		};

	}

}
