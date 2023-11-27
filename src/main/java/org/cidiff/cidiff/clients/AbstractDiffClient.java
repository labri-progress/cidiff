package org.cidiff.cidiff.clients;

import org.cidiff.cidiff.Action;
import org.cidiff.cidiff.DiffClient;
import org.cidiff.cidiff.Line;
import org.cidiff.cidiff.Pair;

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
