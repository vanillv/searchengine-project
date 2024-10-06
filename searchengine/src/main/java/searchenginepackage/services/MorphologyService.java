package searchenginepackage.services;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class MorphologyService {
    private static final String regex = "[^а-я]";

    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ"};
    private static LuceneMorphology morphology;

    static {
        try {
            morphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String processWord(String word) {
        try {
            String processedWord = word.toLowerCase(Locale.ROOT)
                    .replaceAll("ё", "е").replaceAll(regex, "");
            return morphology.getNormalForms(processedWord).get(0);
        } catch (ArrayIndexOutOfBoundsException e) {
            return "";
        }
    }
    public String processWholeText(String text) {
        String[] textBits = processText(text);
        StringBuilder builder = new StringBuilder();
        for (String bit : textBits) {
            builder.append(bit).append(" ");
        }
        return builder.toString();
    }
    private String[] processText(String text) {
        String[] words = text.toLowerCase(Locale.ROOT)
                .replaceAll("ё", "е").replaceAll(regex, " ").split(" ");
        StringBuilder buffer = new StringBuilder();
        for (String word : words) {
            word = word.trim();
              boolean rightWordForm = word.length() > 2;
          if (rightWordForm) {
              buffer.append(morphology.getNormalForms(word).get(0) + "-");
          }
        }
        String[] result = buffer.toString().split("-");
        return result;
        //for making text morphology-able
        }

    public Map<String, Integer> decomposeTextToLemmasWithRank(String text) {
        String[] processedWords = processText(text);
        Map<String, Integer> lemmas = new HashMap<>();
        for (String word : processedWords) {
            int amount = 1;
            String normalWord = morphology.getNormalForms(word).get(0);
            if (lemmas.containsKey(normalWord)) {
                lemmas.put(normalWord, lemmas.get(normalWord) + 1);
            } else {
                lemmas.put(normalWord, amount);
            }
            lemmas.put(normalWord, amount);
        }
        System.out.println(lemmas.size());
        return lemmas;
    }

    public static void main(String[] args) {
        String site = "https://www.playback.ru";
        String html = new ConnectionService().getContent(site);
        String text = new MorphologyService().processWholeText(html);
        System.out.println(text);
    }
}





