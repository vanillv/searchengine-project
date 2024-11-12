package searchenginepackage.services;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
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
            String[] words = text.toLowerCase(Locale.ROOT)
                    .replaceAll("ё", "е").replaceAll(regex, " ").split(" ");
            StringBuffer buffer = new StringBuffer();
            for (String word : words) {
                boolean rightWordForm = false;
                if (morphology.checkString(word) && !word.trim().isEmpty()) {
                    List<String> morphInfo = morphology.getMorphInfo(word);
                        for (String form : morphInfo) {
                            for (String particle : particlesNames) {
                                if (!form.matches(particle)) {
                                    rightWordForm = true;
                                }
                            }
                            if (rightWordForm) {
                                    buffer.append(morphology.getNormalForms(word).get(0) + "-");
                         }
                     }
                }
            }
            String[] result = buffer.toString().split("-");
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
}





