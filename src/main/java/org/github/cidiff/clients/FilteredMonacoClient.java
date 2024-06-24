package org.github.cidiff.clients;

import org.github.cidiff.Action;
import org.github.cidiff.Line;
import org.github.cidiff.Pair;

import java.util.ArrayList;
import java.util.List;

public class FilteredMonacoClient extends MonacoClient {

	public FilteredMonacoClient(Pair<List<Line>> lines, Pair<List<Action>> actions) {
		super(Pair.of(
				actions.left().stream().filter(action -> action.type() == Action.Type.DELETED).map(Action::left).collect(ArrayList::new, ArrayList::add, ArrayList::addAll),
				actions.right().stream().filter(action -> action.type() == Action.Type.ADDED).map(Action::right).collect(ArrayList::new, ArrayList::add, ArrayList::addAll)
		), Pair.of(
				actions.left().stream().filter(action -> action.type() == Action.Type.DELETED).collect(ArrayList::new, ArrayList::add, ArrayList::addAll),
				actions.right().stream().filter(action -> action.type() == Action.Type.ADDED).collect(ArrayList::new, ArrayList::add, ArrayList::addAll)
		));
	}

}
