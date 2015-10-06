/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import model.Classifier;
import model.Trainer;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import view.UI;
import vn.hus.nlp.tokenizer.VietTokenizer;

/**
 *
 * @author Ken
 */
public class Controler {

    public static final int NUMBER_OF_TYPE = 8;
    public static final String MODEL_FILE = "model.txt";
    public static final String[] TYPES = {"Chính trị", "Giáo dục", "Kinh tế",
        "Khác", "Khoa học",
        "Thể thao", "Văn hóa", "Y tế"};
    public static int language;
    public static String runDirectory;
    private final UI view;
    private boolean isModelLoaded;
    private final Trainer trainer;
    private final Classifier classifier;
    private File[] trainingFolders, testFolders;

    public Controler() {
        language = 1;
        isModelLoaded = false;
        view = new UI();
        view.setVisible(true);
        addListener();
        trainer = new Trainer();
        classifier = new Classifier();
        trainingFolders = null;
        testFolders = null;
    }

    private void addListener() {
        view.getBtnBrowse().addActionListener((ActionEvent e) -> {
            onClickBrowse();
        });
        view.getBtnBrowse2().addActionListener((ActionEvent e) -> {
            onClickBrowse2();
        });
        view.getBtnTrain().addActionListener((ActionEvent e) -> {
            onClickTrain();
        });
        view.getBtnLoadModel().addActionListener((ActionEvent e) -> {
            onClickLoadModel();
        });
        view.getBtnTest().addActionListener((ActionEvent e) -> {
            onClickTest();
        });
        view.getBtnGetURLContent().addActionListener((ActionEvent e) -> {
            onClickGetURLContent();
        });
        view.getBtnClassifyURL().addActionListener((ActionEvent e) -> {
            onClickClassify();
        });
    }

    public void onClickBrowse() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("D:\\Training data\\DATA_RANDOM"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Select training data folder(s)";
            }
        });
        fileChooser.showOpenDialog(view);
        trainingFolders = fileChooser.getSelectedFiles();
        if (trainingFolders == null || trainingFolders.length == 0) {
            return;
        }
        String s = "";
        for (int i = 0; i < trainingFolders.length; ++i) {
            s += trainingFolders[i].getName() + ";";
        }
        view.getTxtDataPath().setText(s);
    }

    public void onClickBrowse2() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("D:\\Training data\\DATA_RANDOM"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Select test folder(s)";
            }
        });
        fileChooser.showOpenDialog(view);
        testFolders = fileChooser.getSelectedFiles();
        if (testFolders == null || testFolders.length == 0) {
            return;
        }
        String s = "";
        for (int i = 0; i < testFolders.length; ++i) {
            s += testFolders[i].getName() + ";";
        }
        view.getTxtTestFolder().setText(s);
    }

    public void onClickTrain() {
        if (trainingFolders == null || trainingFolders.length == 0) {
            JOptionPane.showMessageDialog(view, "Choose data folder!");
            return;
        }
        if (view.getRadVietnamese().isSelected()) {
            language = 1;
        } else {
            language = 0;
        }
        try {
            trainer.train(trainingFolders);
            view.getTxaDisplayInfo().append("Done!\n");
            for (int i = 0; i < NUMBER_OF_TYPE; ++i) {
                view.getTxaDisplayInfo().append(TYPES[i] + " : " + trainer.getNDocsType(i) + " docs\n");
            }
            view.getTxaDisplayInfo().append("****************************************\n");
            classifier.loadModel();
            isModelLoaded = true;
            JOptionPane.showMessageDialog(view, "Training successed! Model is loaded!");
        } catch (Exception ex) {
            Logger.getLogger(Controler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void onClickLoadModel() {
        try {
            classifier.loadModel();
            isModelLoaded = true;
            JOptionPane.showMessageDialog(view, "Model is loaded!");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Controler.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(view, "File not found!");
        }
    }

    public void onClickTest() {
        if (!isModelLoaded) {
            try {
                classifier.loadModel();
                isModelLoaded = true;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Controler.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(view, "File not found!");
            }
        }

        DefaultTableModel dtm = (DefaultTableModel) view.getTblResult().getModel();
        dtm.getDataVector().removeAllElements();
        int total = 0, correct = 0;
        try {
            for (int i = 0; i < testFolders.length; ++i) {
                File[] types = testFolders[i].listFiles();
                for (int j = 0; j < types.length; ++j) {
                    File[] docs = types[j].listFiles();
                    for (int k = 0; k < docs.length; ++k) {
                        //test file
                        ++total;
                        Object[] row = new Object[4];
                        row[0] = docs[k].getName();
                        row[1] = TYPES[j];
                        int result = classifier.classifyFile(docs[k].getAbsolutePath());
                        row[2] = TYPES[result];
                        if (result == j) {
                            ++correct;
                            row[3] = "Correct";
                        } else {
                            row[3] = "Wrong";
                        }
                        dtm.addRow(row);
                    }
                }
            }
            float percents = (float) correct * 100 / total;
            dtm.addRow(new Object[]{"Total", "", "", percents + "%"});
            String info = "";
            info += "Tested: " + total + "file(s)\n";
            info += "Correct: " + correct + "file(s) (" + percents + "%)";
            JOptionPane.showMessageDialog(view, info);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Error!\n" + ex.toString());
            ex.printStackTrace();
        }
    }

    public void onClickGetURLContent() {
        String url = view.getTxtURL().getText();
        if (url == null || url.equals("")) {
            return;
        }
        try {
            String content = getURLContent(url);
            view.getTxaURLContent().setText(content);
        } catch (Exception ex) {
            Logger.getLogger(Controler.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(view, "Error!\n" + ex.toString());
        }
    }

    public void onClickClassify() {
        String input = view.getTxaURLContent().getText();
        VietTokenizer tokenizer = new VietTokenizer();
        input = tokenizer.tokenize(input)[0];
        if (!isModelLoaded) {
            try {
                classifier.loadModel();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Controler.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(view, "File not found!");
                return;
            }
        }
        int result = classifier.classifyString(input);
        JOptionPane.showMessageDialog(view, TYPES[result]);
    }

    public String getURLContent(String url) throws Exception {
        String result = "";
        Connection.Response res = Jsoup.connect(url)
                .method(Method.POST)
                .userAgent("Mozilla/5.0 (Windows NT 6.0) Chrome/19.0.1084.46 Safari/536.5")
                .timeout(500 * 1000)
                .ignoreHttpErrors(true)
                .execute();

        Map<String, String> cookies = res.cookies();
        Document doc = Jsoup.connect(url).method(Method.POST)
                .userAgent("Mozilla/5.0 (Windows NT 6.0) Chrome/19.0.1084.46 Safari/536.5")
                .timeout(500 * 1000)
                .ignoreHttpErrors(true)
                .cookies(cookies)
                .get();

        doc.select("div [class=\"PhotoCMS_Caption\"],div [class=\"footer box19\"]").remove();

        Element header = doc.select("h2[class=\"fon33 mt1 sapo\"]").first();
        header.select("span,a").remove();
        result += header.text() + "\n";

        Elements contents = doc.select("p");
        for (Element e : contents) {
            result += e.text() + "\n";
        }
        return result;
    }

    public static void main(String[] args) throws MalformedURLException, IOException {
        Controler controler = new Controler();
    }
}
