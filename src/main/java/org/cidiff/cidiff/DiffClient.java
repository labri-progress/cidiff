package org.cidiff.cidiff;

import org.cidiff.cidiff.clients.ConsoleClient;
import org.cidiff.cidiff.clients.HtmlClient;
import org.cidiff.cidiff.clients.JsonClient;
import org.cidiff.cidiff.clients.MetricsClient;
import org.cidiff.cidiff.clients.SwingClient;

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
