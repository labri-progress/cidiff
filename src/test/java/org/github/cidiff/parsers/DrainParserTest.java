package org.github.cidiff.parsers;

import org.github.cidiff.Utils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class DrainParserTest {

//	@Test
	public void testParse() {
		DrainParser parser = new DrainParser(null);
		List<String> input = """
				LabSZ sshd[24206]: input_userauth_request: invalid user test9 [preauth]
				LabSZ sshd[24208]: input_userauth_request: invalid user webmaster [preauth]
				LabSZ sshd[24490]: Failed password for invalid user ftpuser from 0.0.0.0 port 62891 ssh2
				LabSZ sshd[24492]: Failed password for invalid user pi from 0.0.0.0 port 49289 ssh2
				LabSZ sshd[24501]: Failed password for invalid user ftpuser from 0.0.0.0 port 60836 ssh2
				LabSZ sshd[24245]: input_userauth_request: invalid user pgadmin [preauth]
				""".lines().map(String::trim).toList();

		assertArrayEquals(
				"""
				LabSZ <*> input_userauth_request: invalid user <*> [preauth]
				LabSZ <*> input_userauth_request: invalid user <*> [preauth]
				LabSZ <*> Failed password for invalid user <*> from 0.0.0.0 port <*> ssh2
				LabSZ <*> Failed password for invalid user <*> from 0.0.0.0 port <*> ssh2
				LabSZ <*> Failed password for invalid user <*> from 0.0.0.0 port <*> ssh2
				LabSZ <*> input_userauth_request: invalid user <*> [preauth]
				""".lines().toArray(),
				// map each lines to their template
				input.stream()
						.map(line -> parser.internal.treeSearch(parser.internal.rootNode, Arrays.asList(Utils.split(line))))
						.map(c -> String.join(" ", c.logTemplate))
						.toArray()

		);
	}

}
