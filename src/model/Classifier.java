/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import control.Controler;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import vn.hus.nlp.tokenizer.VietTokenizer;
import vn.hus.nlp.utils.UTF8FileUtility;

/**
 *
 * @author Ken
 */
public class Classifier {

    private double[] pTypes; // Stores p each type
    private HashMap<String, Item> model;
    TreeSet<String> stopWords;

    public Classifier() {
        pTypes = new double[Controler.NUMBER_OF_TYPE];
        model = new HashMap<>();
        stopWords = loadStopWords();
    }

    public Classifier(HashMap<String, Item> model) {
        pTypes = new double[Controler.NUMBER_OF_TYPE];
        this.model = model;
    }

    public void loadModel() throws FileNotFoundException {
        File f = new File(Controler.MODEL_FILE);
        if (!f.exists()) {
            throw new FileNotFoundException();
        }
        String[] lines = UTF8FileUtility.getLines(Controler.MODEL_FILE);
        for (int i = 0; i < Controler.NUMBER_OF_TYPE; ++i) {
            pTypes[i] = Double.parseDouble(lines[i]);
        }
        for (int i = Controler.NUMBER_OF_TYPE; i < lines.length; ++i) {
            String[] temp = lines[i].split(" ");
            double[] p = new double[Controler.NUMBER_OF_TYPE];
            for (int j = 0; j < Controler.NUMBER_OF_TYPE; ++j) {
                p[j] = Double.parseDouble(temp[j + 1]);
                model.put(temp[0], new Item(p));
            }
        }
    }

    public int classifyFile(String path) {
        HashMap<String, Integer> bagOfWordInput = analyzeInputFile(path);
        double[] p = new double[Controler.NUMBER_OF_TYPE];
        for (int i = 0; i < Controler.NUMBER_OF_TYPE; ++i) {
            p[i] = Math.log(pTypes[i]);
        }
        for (String key : bagOfWordInput.keySet()) {
            if (model.containsKey(key)) {
                Item item = model.get(key);
                for (int i = 0; i < Controler.NUMBER_OF_TYPE; ++i) {
                    p[i] += bagOfWordInput.get(key) * Math.log(item.getPInType(i));
                }
            }
        }
        int maxPIndex = 0;
        for (int i = 1; i < Controler.NUMBER_OF_TYPE; ++i) {
            if (p[i] > p[maxPIndex]) {
                maxPIndex = i;
            }
        }
        return maxPIndex;
    }

    public int classifyString(String input) {
        HashMap<String, Integer> bagOfWordInput = analyzeInputString(input);
        double[] p = new double[Controler.NUMBER_OF_TYPE];
        for (int i = 0; i < Controler.NUMBER_OF_TYPE; ++i) {
            p[i] = Math.log(pTypes[i]);
        }
        for (String key : bagOfWordInput.keySet()) {
            if (model.containsKey(key)) {
                Item item = model.get(key);
                for (int i = 0; i < Controler.NUMBER_OF_TYPE; ++i) {
                    p[i] += bagOfWordInput.get(key) * Math.log(item.getPInType(i));
                }
            }
        }
        int maxPIndex = 0;
        for (int i = 1; i < Controler.NUMBER_OF_TYPE; ++i) {
            if (p[i] > p[maxPIndex]) {
                maxPIndex = i;
            }
        }
        return maxPIndex;
    }

    public HashMap<String, Integer> analyzeInputFile(String path) {
        HashMap<String, Integer> result = new HashMap<>();

        String lines[] = UTF8FileUtility.getLines(path);
        String input = "";
        for (int i = 0; i < lines.length; ++i) {
            input += lines[i] + " ";
        }

        return analyzeInputString(input);
    }

    public HashMap<String, Integer> analyzeInputString(String input) {
        HashMap<String, Integer> result = new HashMap<>();

//        if (Controler.language == 1) {
//            VietTokenizer tokenizer = new VietTokenizer();
//            input = tokenizer.tokenize(input)[0];
//        }
        String[] temp = input.split("[,\\.\\?\\;\\!\\:\\(\\)\\[\\]\\{\\}\\p{Space}/]+");
        for (int i = 0; i < temp.length; ++i) {
            temp[i] = temp[i].toLowerCase();
            if (temp[i].equals("") || stopWords.contains(temp[i])) {
                continue;
            }
            if (result.containsKey(temp[i])) {
                result.put(temp[i], result.get(temp[i]) + 1);
            } else {
                result.put(temp[i], 1);
            }
        }

        return result;
    }

    public TreeSet<String> loadStopWords() {
        TreeSet<String> stopWords = new TreeSet<>();
        String temp[];
        if (Controler.language == 0) {
            temp = UTF8FileUtility.getLines("stopwords_en.txt");
        } else {
//            temp = UTF8FileUtility.getLines(Controler.runDirectory + "\\stopwords_vi.txt");
            temp = UTF8FileUtility.getLines("stopwords_vi.txt");
        }
        for (int i = 0; i < temp.length; ++i) {
            stopWords.add(temp[i]);
        }
        return stopWords;
    }
}
