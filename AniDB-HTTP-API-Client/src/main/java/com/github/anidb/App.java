package com.github.anidb;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) throws IOException {
		AnidDB client = new AnidDB();
		System.out.println(client.loadAnime(10948).getTitles());
		client.test();
	}
}
