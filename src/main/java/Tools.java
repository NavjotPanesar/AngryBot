import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.*;

public class Tools {
    private static Parser parser;
    private static final String[] START1 = {"b", "c", "g", "p", "f", "d"};
    private static final String[] START2 = {"r", "l"};
    private static final String[] VOWEL = {"i", "o", "u"};
    private static final String[] CONSONANT1 = {"n", "m"};
    private static final String[] CONSONANT2 = {"g", "k", "p", "b"};
    private static final String[] END = {"us", "er", "oid", "aloid", "is","ite"};

    private static final List<String> SIZE_ADJECTIVES = Arrays.asList("big", "huge", "giant", "small", "tiny", "puny", "milky");
    private static final List<String> ADJECTIVES = Arrays.asList("ugly", "stinking", "funky", "grimy", "slimy", "gunky", "disgusting", "revolting", "horrible", "horrid", "putrid", "pungent", "crispy", "crusty", "busted", "encrusted", "foul", "rotten", "slippery", "wet", "moist", "diseased", "fruity", "dumb", "stupid", "retarded", "melted", "melty", "dirty", "filthy", "nasty", "fat", "plump", "husky", "pudgy", "heavy", "fleshy", "obese", "tubby", "ripe", "rotund", "round", "blubbery");

    static void initializeParser() {
        try {
            InputStream modelIn = Tools.class.getResourceAsStream("/en-parser-chunking.bin"); // Replace with the correct path
            ParserModel model = new ParserModel(modelIn);
            parser = ParserFactory.create(model);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

// takes in time in seconds and simplifies to days hours minutes and seconds.

    public static String formatDuration(long seconds) {
        Duration duration = Duration.ofSeconds(seconds);

        long days = duration.toDaysPart();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long remainingSeconds = duration.toSecondsPart();

        StringBuilder formattedDuration = new StringBuilder();

        if (days > 0) {
            formattedDuration.append(days).append(" day").append(days > 1 ? "s" : "").append(" ");
        }
        if (hours > 0) {
            formattedDuration.append(hours).append(" hour").append(hours > 1 ? "s" : "").append(" ");
        }
        if (minutes > 0) {
            formattedDuration.append(minutes).append(" minute").append(minutes > 1 ? "s" : "").append(" ");
        }
        if (remainingSeconds > 0 || formattedDuration.length() == 0) {
            formattedDuration.append(remainingSeconds).append(" second").append(remainingSeconds != 1 ? "s" : "");
        }

        return formattedDuration.toString().trim();
    }

    // Scrambles a string and returns it
    public static String stringScramble(String input) {
        List<Character> list = new ArrayList<Character>();
        for (char c : input.toCharArray()) list.add(c);
        Collections.shuffle(list);
        StringBuilder builder = new StringBuilder();
        for (char c : list) builder.append(c);
        return builder.toString().toLowerCase(Locale.ROOT);
    }

    public static List csvToList(String input) {
        List<String> result = new ArrayList<>();

        if (input != null && !input.isEmpty()) {
            String[] parts = input.split(";");
            for (String part : parts) {
                result.add(part);
            }
        }
        return result;
    }

    public static String listToCsv(List<String> input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (String item : input) {
            if (result.length() > 0) {
                result.append(";");
            }
            result.append(item);
        }
        return result.toString();
    }


    static boolean containsParserTypes(String sentence) {
        if (parser != null) {
            Parse parse = parseSentence(sentence);
            if (parse != null) {
                // Check for specific parser types (e.g., SBARQ for questions)
                List<String> types = getParserTypes(parse);
                for (String type : types) {
                    if (isQuestionOrRequestType(type)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static Parse parseSentence(String sentence) {
        if (parser != null) {
            return ParserTool.parseLine(sentence, parser, 1)[0];
        }
        return null;
    }

    private static List<String> getParserTypes(Parse parse) {
        List<String> types = new ArrayList<>();
        for (Parse child : parse.getChildren()) {
            types.add(child.getType());
            types.addAll(getParserTypes(child));
        }
        return types;
    }

    private static boolean isQuestionOrRequestType(String type) {
        // Define additional parser types that indicate questions or requests
        String[] questionAndRequestTypes = {"SBARQ", "SQ", "VP", "ROOT", "NP", "PP", "ADJP", "ADVP"};
        for (String validType : questionAndRequestTypes) {
            if (type.equals(validType)) {
                return true;
            }
        }
        return false;
    }


    public static String generateSillyWord() {
        Random random = new Random();

        // Generate a random number of adjectives (0 to 5) with adjusted probabilities
        int numAdjectives = 0;
        double probability = random.nextDouble();
        if (probability < 0.4) {
            numAdjectives = 0;
        } else if (probability < 0.7) {
            numAdjectives = 1;
        } else if (probability < 0.85) {
            numAdjectives = 2;
        } else if (probability < 0.95) {
            numAdjectives = 3;
        } else if (probability < 0.99) {
            numAdjectives = 4;
        } else {
            numAdjectives = 5;
        }

        // Shuffle the list of adjectives to ensure randomness
        List<String> shuffledAdjectives = new ArrayList<>(ADJECTIVES);
        Collections.shuffle(shuffledAdjectives);

        // Add size-related adjective as the first word
        List<String> adjectiveList = new ArrayList<>();
        if (numAdjectives > 0 && !SIZE_ADJECTIVES.isEmpty()) {
            adjectiveList.add(SIZE_ADJECTIVES.get(random.nextInt(SIZE_ADJECTIVES.size())));
        }

        // Add additional adjectives based on the generated number
        for (int i = 0; i < numAdjectives && i < shuffledAdjectives.size(); i++) {
            adjectiveList.add(shuffledAdjectives.get(i));
        }

        // Start with any of START1
        String start = START1[random.nextInt(START1.length)];

        // Random chance of adding in START2, but not if START1 is "d"
        if (!start.equals("d") && random.nextBoolean()) {
            start += START2[random.nextInt(START2.length)];
        }

        // Pick a random vowel
        String vowel = VOWEL[random.nextInt(VOWEL.length)];

        // Pick a random letter from CONSONANT1
        String consonant1 = CONSONANT1[random.nextInt(CONSONANT1.length)];

        // If the word starts with "c" and there is no START2, do not allow "i" as the next letter
        if (start.equals("c") && !start.endsWith("r") && !start.endsWith("l")) {
            while (vowel.equals("i")) {
                vowel = VOWEL[random.nextInt(VOWEL.length)];
            }
        }

        // Pick a random letter from CONSONANT2, but not "p" if CONSONANT1 is "n" or "k" if CONSONANT1 is "m"
        String consonant2 = CONSONANT2[random.nextInt(CONSONANT2.length)];
        if (consonant1.equals("n") && consonant2.equals("p")) {
            consonant2 = CONSONANT2[random.nextInt(CONSONANT2.length - 1)]; // Exclude "p"
        } else if (consonant1.equals("m") && consonant2.equals("k")) {
            consonant2 = CONSONANT2[random.nextInt(CONSONANT2.length - 1)]; // Exclude "k"
        }

        // Pick a random ending
        String ending = END[random.nextInt(END.length)];

        // Combine the parts to get the word
        String word = String.join(" ", adjectiveList) + " " + start + vowel + consonant1 + consonant2 + ending;

        // Trim whitespace from the beginning and end, and capitalize the first letter
        word = word.trim();
        word = Character.toUpperCase(word.charAt(0)) + word.substring(1);
        if (word.length()> 32) return generateSillyWord();
        return word;
    }

}