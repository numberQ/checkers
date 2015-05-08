package com.company;

import java.io.*;

public class Main {

    public static void main(String[] args) {
        Game game = new Game();
        game.initGame();
    }

    public static void write(File file, String text) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)))) {
            out.println(text);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
