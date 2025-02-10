package org.github.cidiff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Drain {

	private int depth;
	private double st;
	private int maxChild;
	public Node rootNode;

	public static Drain INSTANCE = null;

	public Drain(int depth, float st, int maxChild) {
		this.depth = depth - 2;
		this.st = st;
		this.maxChild = maxChild;
		rootNode = new Node(0, "");
	}

	public Drain() {
		depth = 2;
		st = 0.4;
		maxChild = 100;
		rootNode = new Node(0, "");
	}

	public LogCluster treeSearch(Node rn, List<String> seq) {
		int seqLen = seq.size();
		if (!rn.childD.containsKey("" + seqLen)) {
			return null;
		}
		Node parentn = rn.childD.get("" + seqLen);
		int currentDepth = 1;
		for (String token : seq) {
			if (currentDepth >= this.depth || currentDepth > seqLen) {
				break;
			}
			if (parentn.childD.containsKey(token)) {
				parentn = parentn.childD.get(token);
			} else if (parentn.childD.containsKey("<*>")) {
				parentn = parentn.childD.get("<*>");
			} else {
				return null;
			}
			currentDepth += 1;
		}
		List<LogCluster> logClustL = parentn.clusters;
		return this.fastMatch(logClustL, seq);
	}

	public boolean hasNumber(String s) {
		for (char c : s.toCharArray()) {
			if (Character.isDigit(c)) {
				return true;
			}
		}
		return false;
	}

	public void addSeqToPrefixTree(Node rn, LogCluster logClust) {
		int seqLen = logClust.logTemplate.size();
		if (!rn.childD.containsKey("" + seqLen)) { // treat every "number" key as string key of that number
			Node firstLayerNode = new Node(1, "" + seqLen);
			rn.childD.put("" + seqLen, firstLayerNode);
		}
		Node parentn = rn.childD.get("" + seqLen);
		int currentDepth = 1;
		for (String token : logClust.logTemplate) {
			// Add current log cluster to the leaf node
			if (currentDepth >= this.depth || currentDepth > seqLen) {
				parentn.clusters.add(logClust);
				break;
			}
			// If token not matched in this layer of existing tree.
			if (!parentn.childD.containsKey(token)) {
				if (!this.hasNumber(token)) {
					if (parentn.childD.containsKey("<*>")) {
						if (parentn.childD.size() < this.maxChild) {
							Node newNode = new Node(currentDepth, token);
							parentn.childD.put(token, newNode);
							parentn = newNode;
						} else {
							parentn = parentn.childD.get("<*>");
						}
					} else {
						if (parentn.childD.size() + 1 < this.maxChild) {
							Node newNode = new Node(currentDepth + 1, token);
							parentn.childD.put(token, newNode);
							parentn = newNode;
						} else if (parentn.childD.size() + 1 == this.maxChild) {
							Node newNode = new Node(currentDepth + 1, "<*>");
							parentn.childD.put("<*>", newNode);
							parentn = newNode;
						} else {
							parentn = parentn.childD.get("<*>");
						}
					}
				} else {
					if (!parentn.childD.containsKey("<*>")) {
						Node newNode = new Node(currentDepth + 1, "<*>");
						parentn.childD.put("<*>", newNode);
						parentn = newNode;
					} else {
						parentn = parentn.childD.get("<*>");
					}
				}
			} else {
				parentn = parentn.childD.get(token);
			}
			currentDepth += 1;
		}
	}

	private LogCluster fastMatch(List<LogCluster> logClustL, List<String> seq) {
		double maxSim = -1;
		int maxNumOfPara = -1;
		LogCluster maxClust = null;
		for (LogCluster logClust : logClustL) {
			double[] v = this.seqDist(logClust.logTemplate, seq);
			double curSim = v[0];
			int curNumOfPara = (int) v[1];
			if ((curSim > maxSim) || (curSim == maxSim && curNumOfPara > maxNumOfPara)) {
				maxSim = curSim;
				maxNumOfPara = curNumOfPara;
				maxClust = logClust;
			}
		}
		if (maxSim > this.st) {
			return maxClust;
		}
		return null;
	}

	private double[] seqDist(List<String> seq1, List<String> seq2) {
		assert seq1.size() == seq2.size();
		int simTokens = 0;
		int numOfPar = 0;
		for (int i = 0; i < seq1.size(); i++) {
			String token1 = seq1.get(i);
			String token2 = seq2.get(i);
			if (token1 == "<*>") {
				numOfPar += 1;
				continue;
			}
			if (token1.equals(token2)) {
				simTokens += 1;
			}
		}
		return new double[]{((double) simTokens) / seq1.size(), numOfPar};
	}

	private List<String> getTemplate(List<String> seq1, List<String> seq2) {
		assert seq1.size() == seq2.size();
		List<String> retVal = new ArrayList<>();
		for (int i = 0; i < seq1.size(); i++) {
			if (seq1.get(i).equals(seq2.get(i))) {
				retVal.add(seq1.get(i));
			} else {
				retVal.add("<*>");
			}
		}
		return retVal;
	}

	public ArrayList<LogCluster> parse(List<String> lines) {
		ArrayList<LogCluster> logCluL = new ArrayList<>();
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			List<String> logmessageL = new ArrayList<>(Arrays.asList(Utils.split(line)));
			LogCluster matchCluster = this.treeSearch(this.rootNode, logmessageL);
			if (matchCluster == null) {
				LogCluster newCluster = new LogCluster(logmessageL, new ArrayList<>(Arrays.asList(i)));
				logCluL.add(newCluster);
				this.addSeqToPrefixTree(this.rootNode, newCluster);
			} else {
				List<String> newTemplate = this.getTemplate(logmessageL, matchCluster.logTemplate);
				matchCluster.logIDL.add(i);
				if (!String.join(" ", newTemplate).equals(String.join(" ", matchCluster.logTemplate))) {
					matchCluster.logTemplate = newTemplate;
				}
			}
		}
		return logCluL;
	}

	public static class LogCluster {

		public List<String> logTemplate;
		private List<Integer> logIDL;

		public LogCluster() {
			logTemplate = new ArrayList<>();
			logIDL = new ArrayList<>();
		}

		public LogCluster(List<String> logTemplate, List<Integer> logIDL) {
			this.logTemplate = logTemplate;
			this.logIDL = logIDL;
		}

		@Override
		public String toString() {
			return "LogCluster{" +
					"logTemplate=" + logTemplate +
					", logIDL=" + logIDL +
					'}';
		}

		public int sightings() {
			return logIDL.size();
		}

	}

	public static class Node {

		private HashMap<String, Node> childD;
		private int depth;
		private String digitOrToken;
		private List<LogCluster> clusters;

		public Node(int depth, String digitOrToken) {
			childD = new HashMap<>();
			this.depth = depth;
			this.digitOrToken = digitOrToken;
			clusters = new ArrayList<>();
		}

		@Override
		public String toString() {
			return "Node{" +
					"childD=" + childD +
					", depth=" + depth +
					", digitOrToken='" + digitOrToken + '\'' +
					", clusters=" + clusters +
					'}';
		}

	}

}
