/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import control.Controler;
import java.io.File;
import java.util.HashMap;
import java.util.TreeSet;
import javax.swing.JOptionPane;
import vn.hus.nlp.tokenizer.VietTokenizer;
import vn.hus.nlp.utils.UTF8FileUtility;

/**
 *
 * @author Ken
 */
public class Trainer {

    private final int[] nDocTypes; // Stores number of documents in each type
    private final long[] nWordTypes; // Stores number of words in each type
    private final double[] pTypes; // Stores probability each type
    private final TreeSet<String> stopWords; // Stores stop words
    private final HashMap<String, WordCounter> bagOfWords; // Bag of words
    private final HashMap<String, Item> model; // Model for classifing
    private final VietTokenizer tokenizer;

    public Trainer() {
        nDocTypes = new int[Controler.NUMBER_OF_TYPE];
        nWordTypes = new long[Controler.NUMBER_OF_TYPE];
        pTypes = new double[Controler.NUMBER_OF_TYPE];
        bagOfWords = new HashMap<>();
        model = new HashMap<>();
        stopWords = new TreeSet<>();
        tokenizer = new VietTokenizer();
        loadStopWords();
    }

    public void train(File[] files) throws Exception {
        for (int i = 0; i < Controler.NUMBER_OF_TYPE; ++i) {
            nDocTypes[i] = 0;
            nWordTypes[i] = 0;
            pTypes[i] = 0;
        }
        analyze(files);
        calculate();
        saveModel();
    }

    /**
     * Analyzes training data
     *
     * @param files
     * @throws Exception
     */
    public void analyze(File[] files) throws Exception {
        for (int i = 0; i < files.length; ++i) {
            File[] types = files[i].listFiles();
            for (int j = 0; j < types.length; ++j) {
                File[] docs = types[j].listFiles();
                nDocTypes[j] += docs.length;
                for (int k = 0; k < docs.length; ++k) {
                    analyzeFile(docs[k].getAbsolutePath(), j);
                }
            }
        }
    }

    public void loadStopWords() {
        stopWords.clear();
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

    }

    /**
     * Analyzes a file in a type
     *
     * @param path
     * @param typeIndex
     */
    public void analyzeFile(String path, int typeIndex) {
        String[] lines = UTF8FileUtility.getLines(path);
        String input = "";
        for (int i = 0; i < lines.length; ++i) {
            input += lines[i] + " ";
        }
//        if (Controler.language == 1) {
//            input = tokenizer.tokenize(input)[0];
//        }
        String[] temp = input.split("[,\\.\\?\\;\\!\\:\\(\\)\\[\\]\\{\\}\\p{Space}/]+");
        for (int i = 0; i < temp.length; ++i) {
            temp[i] = temp[i].toLowerCase();
            if (temp[i].equals("") || stopWords.contains(temp[i])) {
                continue;
            }
            ++nWordTypes[typeIndex];
            if (bagOfWords.containsKey(temp[i])) {
                bagOfWords.get(temp[i]).incNInType(typeIndex);
            } else {
                WordCounter wordCounter = new WordCounter();
                wordCounter.setNInType(typeIndex, 1);
                bagOfWords.put(temp[i], wordCounter);
            }
        }
    }

    public void calculate() {
        // Calculates probability each type
        int nDocsAllType = 0;
        for (int i = 0; i < nDocTypes.length; ++i) {
            nDocsAllType += nDocTypes[i];
        }
        for (int i = 0; i < nDocTypes.length; ++i) {
            pTypes[i] = (double) nDocTypes[i] / nDocsAllType;
        }

        // Calculates a word in a document of each type
        long n = bagOfWords.size();
        for (String key : bagOfWords.keySet()) {
            WordCounter wordCounter = bagOfWords.get(key);
            double[] p = new double[Controler.NUMBER_OF_TYPE];
            for (int i = 0; i < Controler.NUMBER_OF_TYPE; ++i) {
//                p[i] = (double) (wordCounter.getNInType(i) + 1) / (nWordTypes[i] + 1);
                p[i] = (double) (wordCounter.getNInType(i) + 1) / (nWordTypes[i] + n);
            }
            model.put(key, new Item(p));
        }
    }

    /**
     * Saves model to file File structure (with n = number of types) : n first
     * lines, each line is p of a type From line n + 1, each line stores a word,
     * p a document is each type if has this word
     */
    public void saveModel() {
        UTF8FileUtility.createWriter(Controler.MODEL_FILE);

        for (int i = 0; i < Controler.NUMBER_OF_TYPE; ++i) {
            UTF8FileUtility.writeln(pTypes[i]);
        }

        for (String key : model.keySet()) {
            Item item = model.get(key);
            String s = key;
            for (int i = 0; i < Controler.NUMBER_OF_TYPE; ++i) {
                s += " " + item.getPInType(i);
            }
            s += "\n";
            UTF8FileUtility.write(s);
        }

        UTF8FileUtility.closeWriter();
    }

    public int getNDocsType(int index) {
        return nDocTypes[index];
    }
}
