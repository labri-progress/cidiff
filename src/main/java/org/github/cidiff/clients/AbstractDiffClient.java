package org.github.cidiff.clients;

import org.github.cidiff.Action;
import org.github.cidiff.DiffClient;
import org.github.cidiff.Line;
import org.github.cidiff.Pair;

import java.util.List;

public abstract class AbstractDiffClient implements DiffClient {

	public Pair<String> files;
	public Pair<List<Line>> lines;
	public Pair<List<Action>> actions;

	public AbstractDiffClient(Pair<List<Line>> lines, Pair<List<Action>> actions) {
		this.lines = lines;
		this.actions = actions;
	}

}
