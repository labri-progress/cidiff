package org.github.cidiff;

import org.github.cidiff.clients.ConsoleClient;
import org.github.cidiff.clients.HtmlClient;
import org.github.cidiff.clients.JsonClient;
import org.github.cidiff.clients.MetricsClient;
import org.github.cidiff.clients.SwingClient;

import java.util.List;
import java.util.function.BiFunction;

@FunctionalInterface
public interface DiffClient {

	void execute();

	enum Type {
		SWING(SwingClient::new),
		JSON(JsonClient::new),
		CONSOLE(ConsoleClient::new),
		METRICS(MetricsClient::new),
		HTML(HtmlClient::new);

		private final BiFunction<Pair<List<Line>>, Pair<List<Action>>, DiffClient> constructor;

		Type(BiFunction<Pair<List<Line>>, Pair<List<Action>>, DiffClient> constructor) {
			this.constructor = constructor;
		}

		public DiffClient construct(Pair<List<Line>> lines, Pair<List<Action>> actions) {
			return this.constructor.apply(lines, actions);
		}
	}

}
