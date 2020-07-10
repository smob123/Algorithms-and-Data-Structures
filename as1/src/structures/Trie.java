package structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Trie {
	// empty root node
	TrieNode root = new TrieNode();

	/**
	 * adds a new stop object to the trie
	 * 
	 * @param word the name of the stop
	 * @param stop the stop object to associate with the name
	 */
	public void insert(String word, Stop stop) {
		TrieNode current = root;

		for (int i = 0; i < word.length(); i++) {
			// current char in the word
			char c = word.charAt(i);

			// check if the current node has a child, which matches the current char
			if (!current.children.containsKey(c)) {
				// create a new node, and add it the the current node's children
				TrieNode newNode = new TrieNode(c);
				current.children.put(c, newNode);
			}

			// move on to the next node
			current = current.children.get(c);
		}

		// add the stop to the node's stop list
		current.stops.add(stop);
		// set the current node to be the end of a word
		current.isEndOfWord = true;
	}

	/**
	 * returns suggestions for auto-completion based on a prefix.
	 * 
	 * @param word the prefix that will be used to search for stops
	 * @return suggestions for auto-completion based on a prefix
	 */
	public ArrayList<Stop> getSuggestions(String word) {
		TrieNode current = root;
		ArrayList<Stop> results = new ArrayList<>();

		for (int i = 0; i < word.length(); i++) {
			char c = word.charAt(i);

			// check if the current node has a child that matches the current char
			if (!current.children.containsKey(c)) {
				return results;
			}

			// move on to the current node's child
			current = current.children.get(c);
		}

		// check if the current node is the end of a word, and add it to the list of
		// results
		if (current.isEndOfWord) {
			results.addAll(current.stops);
		}

		// get all the current node's descendants if it has any
		if (!current.children.keySet().isEmpty()) {
			searchChildNodes(current, results);
		}

		return results;
	}

	/**
	 * returns a node's descendants.
	 * 
	 * @param rootNode the node that will be searched
	 * @param results  list of results to add the descendants into
	 */
	private void searchChildNodes(TrieNode rootNode, ArrayList<Stop> results) {
		// add the current node to the list of results if it's an end of a word
		if (rootNode.isEndOfWord) {
			results.addAll(rootNode.stops);
		}

		// verify that the current node has children
		if (rootNode.children.keySet().isEmpty()) {
			return;
		}

		// recall the method using all the current node's children to get their
		// descendants as well
		for (char key : rootNode.children.keySet()) {
			searchChildNodes(rootNode.children.get(key), results);
		}
	}

	private class TrieNode {
		char character;
		// set of children this node has
		HashMap<Character, TrieNode> children = new HashMap<>();
		// checks whether this node is an end of a word or not
		boolean isEndOfWord = false;
		// list of stops that share the same name
		List<Stop> stops = new ArrayList<>();

		public TrieNode() {
		}

		public TrieNode(char character) {
			this.character = character;
		}
	}
}
