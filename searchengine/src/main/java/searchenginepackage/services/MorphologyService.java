package searchenginepackage.services;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class MorphologyService {
    private Logger log = LoggerFactory.getLogger(MorphologyService.class);
    private static final String regex = "[^А-Яа-я]";
    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ"};
    private static volatile LuceneMorphology morphology;
    static {
        try {
            morphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private String[] processText(String text) {
        if (text == null || text.isBlank()) {
            return new String[0];
        }
        String[] words = text.toLowerCase(Locale.ROOT)
                .replaceAll("ё", "е").replaceAll(regex, " ").split(" ");
        StringBuffer buffer = new StringBuffer();
        for (String word : words) {
            word = word.trim();
            if (morphology.checkString(word) && !word.isBlank() && word.length() > 2) {
                List<String> morphInfo = morphology.getMorphInfo(word);
                for (String form : morphInfo) {
                    if (Arrays.stream(particlesNames).noneMatch(form::contains)) {
                        buffer.append(morphology.getNormalForms(word).get(0)).append("-/");
                    }
                }
            }
        }
        String[] result = buffer.toString().split("-/");
        return result;
    }
    public Map<String, Integer> decomposeTextToLemmasWithRank(String text) {
        try {
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
            }
            return lemmas;
        }
        catch (Exception e) {
            log.error("Morphology service error: " + e.getMessage());
        };
        return null;
    }
    public List<String> lemmatizeElementContent(Element element) {
        String textContent = element.text();
        String[] words = textContent.toLowerCase(Locale.ROOT)
                .replaceAll("ё", "е")
                .replaceAll(regex, " ")
                .split("\\s+");
        Set<String> lemmas = new HashSet<>();
        for (String word : words) {
            word = word.trim();
            if (!word.isEmpty() && morphology.checkString(word)) {
                List<String> normalForms = morphology.getNormalForms(word);
                if (normalForms != null && !normalForms.isEmpty()) {
                    lemmas.add(normalForms.get(0));
                }
            }
        }

        return new ArrayList<>(lemmas);
    }

    public String lemmatizeWord(String word) {
        if (word == null || word.isBlank()) {
            return "";
        }
        String processedWord = word.toLowerCase(Locale.ROOT)
                .replaceAll("ё", "е").replaceAll(regex, " ").trim();
        if (!processedWord.isEmpty() && morphology.checkString(processedWord)) {
            List<String> normalForms = morphology.getNormalForms(processedWord);
            if (!normalForms.isEmpty()) {
                return normalForms.get(0);
            }
        }
        return word;
    }

}





