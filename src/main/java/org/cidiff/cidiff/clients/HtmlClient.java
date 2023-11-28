package org.cidiff.cidiff.clients;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import org.cidiff.cidiff.Action;
import org.cidiff.cidiff.Line;
import org.cidiff.cidiff.Pair;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class HtmlClient extends AbstractDiffClient {

	public HtmlClient(Pair<List<Line>> lines, Pair<List<Action>> actions) {
		super(lines, actions);
	}

	public static String color(Action.Type type) {
		return switch (type) {
			case ADDED -> "green";
			case DELETED -> "red";
//            case UPDATED -> "orange";
			case SKIPPED, UNCHANGED, UPDATED, NONE -> "white";
			case MOVED_UPDATED, MOVED_UNCHANGED -> "light_grey";
		};
	}

	public static String[] convertToHtml(Action action) {
		Line right = action.right();
		Line left = action.left();
		return switch (action.type()) {
			case ADDED -> new String[]{"", "<span class=\"added\">%s</span>".formatted(right.value())};
			case DELETED -> new String[]{"<span class=\"deleted\">%s</span>".formatted(left.value()), ""};
			case UNCHANGED, MOVED_UNCHANGED ->
					new String[]{"<span>%s</span>".formatted(left.value()), "<span>%s</span>".formatted(right.value())};
			case UPDATED, MOVED_UPDATED -> {
				String[] lTokens = left.value().split(" ");
				String[] rTokens = right.value().split(" ");
				StringBuilder lBuilder = new StringBuilder("<span>");
				StringBuilder rBuilder = new StringBuilder("<span>");
				for (int i = 0; i < lTokens.length; i++) {
					if (!lTokens[i].equals(rTokens[i])) {
						lBuilder.append("<span class=\"modified\">").append(lTokens[i]).append("</span> ");
						rBuilder.append("<span class=\"modified\">").append(rTokens[i]).append("</span> ");
					} else {
						lBuilder.append(lTokens[i]).append(" ");
						rBuilder.append(rTokens[i]).append(" ");
					}
				}
				lBuilder.append("</span>");
				rBuilder.append("</span>");
				yield new String[]{lBuilder.toString(), rBuilder.toString()};
			}
			default -> new String[]{"", ""};
		};
	}

	//    @Override
	public void execute2() {
		List<TemplateLine> left = IntStream.range(0, this.lines.left().size())
				.mapToObj(i -> {
					Action action = this.actions.left().get(i);
					List<TemplateFragment> fragments = new ArrayList<>();
					final String[] tokens = action.left().value().split(" ");
					if (action.right() == null) {
						for (String token : tokens) {
							fragments.add(new TemplateFragment(token, false));
						}
					} else {
						final String[] otherTokens = action.right().value().split(" ");
						for (int j = 0; j < tokens.length; j++) {
							fragments.add(new TemplateFragment(tokens[j], !tokens[j].equals(otherTokens[j])));
						}
					}
					return new TemplateLine(fragments, i + 1, color(action.type()), action.right() == null ? -1 : action.right().index());
				})
				.toList();
		List<TemplateLine> right = IntStream.range(0, this.lines.right().size())
				.mapToObj(i -> {
					Action action = this.actions.right().get(i);
					String[] htmls = convertToHtml(action);
					List<TemplateFragment> fragments = new ArrayList<>();
					final String[] tokens = action.right().value().split(" ");
					if (action.left() == null) {
						for (String token : tokens) {
							fragments.add(new TemplateFragment(token, false));
						}
					} else {
						final String[] otherTokens = action.left().value().split(" ");
						for (int j = 0; j < tokens.length; j++) {
							fragments.add(new TemplateFragment(tokens[j], !tokens[j].equals(otherTokens[j])));
						}
					}
					return new TemplateLine(fragments, i + 1, color(action.type()), action.left() == null ? -1 : action.left().index());
				})
				.toList();

		DefaultMustacheFactory factory = new DefaultMustacheFactory();
		Mustache mustache = factory.compile("template.mustache");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("output.html"));
			mustache.execute(writer, new TemplateData(left, right));
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void execute() {
		List<TemplateLine2> left = IntStream.range(0, this.lines.left().size())
				.mapToObj(i -> {
					Action action = this.actions.left().get(i);
					String content = convertToHtml(action)[0];
					return new TemplateLine2(content, action.left(), action.right());
				})
				.toList();
		List<TemplateLine2> right = IntStream.range(0, this.lines.right().size())
				.mapToObj(i -> {
					Action action = this.actions.right().get(i);
					String content = convertToHtml(action)[1];
					return new TemplateLine2(content, action.right(), action.left());
				})
				.toList();
		DefaultMustacheFactory factory = new DefaultMustacheFactory();
		Mustache mustache = factory.compile("template2.mustache");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("output.html"));
			mustache.execute(writer, new TemplateData2(left, right));
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public record TemplateData2(List<TemplateLine2> leftLines, List<TemplateLine2> rightLines) {

	}

	public record TemplateLine2(String content, int index, int link) {
		TemplateLine2(String content, Line self, Line other) {
			this(content, self == null ? -1 : self.index(), other == null ? -1 : other.index());
		}
	}

	public record TemplateData(List<TemplateLine> leftLines, List<TemplateLine> rightLines) {
	}

	public record TemplateLine(List<TemplateFragment> fragments, int index, String color, int link) {
	}

	public record TemplateFragment(String value, boolean modified) {

	}

}
