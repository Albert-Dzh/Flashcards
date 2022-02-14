package Flashcards;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;


class Flashcard {

    private static final Logger logger = LogManager.getLogger(Flashcard.class);

    private File exportFile     = null;
    private final Scanner in    = new Scanner(System.in);
    private final Map<String, String> flashcards            = new LinkedHashMap<>();
    private final Map<String, Integer> cardsWithMistakes    = new LinkedHashMap<>();

    Flashcard(String[] args) {
        for (int i = 0; i < args.length; i++)
            if      ("-export".equals(args[i])) exportFile   = new File(args[i + 1]);
            else if ("-import".equals(args[i])) importFromFile(new File(args[i + 1]));
    }

    private boolean hasQ(String question)   { return flashcards.containsKey(question); }
    private boolean hasA(String answer)     { return flashcards.containsValue(answer); }

    private void addCard() {
        logger.info("The card:");
        String question = in.nextLine();
        logger.debug(question);

        if (hasQ(question)) {
            logger.info(String.format("The card \"%s\" already exists.", question));
            return;
        }

        logger.info("The definition of the card:");
        String answer = in.nextLine();
        logger.debug(answer);

        if (hasA(answer)) {
            logger.info(String.format("The definition \"%s\" already exists.", answer));
            return;
        }

        flashcards.put(question, answer);
        logger.info(String.format("The pair (\"%s\":\"%s\") has been added.", question, answer));
    }

    private void remove() {
        logger.info("The card:");
        String question = in.nextLine();
        logger.debug(question);

        if (hasQ(question)) {
            flashcards.remove(question);
            cardsWithMistakes.remove(question);
            logger.info("The card has been removed.");
        } else {
            logger.info(String.format("Can't remove \"%s\": there is no such card.", question));
        }
    }

    private void importFlashcards() {
        logger.info("File name:");
        String fileName = in.nextLine();
        logger.debug(fileName);
        importFromFile(new File(fileName));
    }

    private void importFromFile(File importFile) {
        int counter = 0;
        String question, answer, mistakes;

        try (final Scanner scan = new Scanner(importFile)) {
            while (scan.hasNext()) {
                question    = scan.nextLine();
                answer      = scan.nextLine();
                mistakes    = scan.nextLine();
                flashcards.put(question, answer);
                cardsWithMistakes.put(question, Integer.parseInt(mistakes));
                counter++;
            }
        } catch (FileNotFoundException ex) {
            logger.info("File not found.");
        }
        logger.info(String.format("%d cards have been loaded.", counter));
    }

    private void exportFlashcards() {
        logger.info("File name:");
        String fileName = in.nextLine();
        logger.debug(fileName);
        exportToFile(new File(fileName));
    }

    private void exportToFile(File exportFile) {
        try (FileWriter fw = new FileWriter(exportFile)) {
            for (Map.Entry<String, String> pair : flashcards.entrySet())
                fw.write(String.format("%s%n%s%n%s%n", pair.getKey(), pair.getValue(), cardsWithMistakes.get(pair.getKey())));

            fw.flush();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        logger.info(String.format("%d cards have been saved.", flashcards.size()));
    }

    private void ask() {
        logger.info("How many times to ask?");
        int counter = Integer.parseInt(in.nextLine());
        logger.debug(counter);

        while (counter-- > 0) askRandomQuestion();
    }

    private void askRandomQuestion() {
        Map.Entry<String, String> pair = getRandomEntry();
        String question = pair.getKey();
        String correctAnsw =  pair.getValue();
        logger.info(String.format("Print the definition of \"%s\"", question));
        String answer = in.nextLine();
        logger.debug(answer);

        if (!answer.equals(correctAnsw)) {
            cardsWithMistakes.put(question, getWrongAnswCounter(question) + 1);
            logger.info(String.format("Wrong answer. The correct one is \"%s\"%s", correctAnsw, hasA(answer) ?
                    String.format(", you've just written the definition of \"%s\".", getQuestionByAnswer(answer)) :
                    "."));
        } else {
            logger.info("Correct answer.");
        }
    }

    private Map.Entry<String, String> getRandomEntry() {
        int random = new Random().nextInt(flashcards.size());
        var it = flashcards.entrySet().iterator();

        while (--random > 0) it.next();
        return it.next();
    }

    private String getQuestionByAnswer(String answer) {
        return flashcards.values().stream().filter(s -> s.equals(answer)).findFirst().orElse(null);
    }

    private int getWrongAnswCounter(String question) { return cardsWithMistakes.getOrDefault(question, 0); }

    private void logToFile() { logger.info("The log has been saved"); }

    private void hardestCard() {
        if (cardsWithMistakes.isEmpty()) {
            logger.info("There are no cards with errors.");
        } else {
            List<String> hardestCards = new LinkedList<>();
            int maxMistakes = 0;
            for (Map.Entry<String, Integer> pair : cardsWithMistakes.entrySet()) {
                String question = pair.getKey();
                int mistakesCounter = pair.getValue();

                if (maxMistakes < mistakesCounter) {
                    hardestCards.clear();
                    hardestCards.add(question);
                } else if (maxMistakes == mistakesCounter) {
                    hardestCards.add(question);
                }
                maxMistakes = Math.max(maxMistakes, mistakesCounter);
            }
            StringBuilder output = new StringBuilder("The hardest card");
            if (hardestCards.size() == 1) {
                output.append(String.format(" is \"%s\". You have %d errors answering it.", hardestCards.get(0), maxMistakes));
            } else {
                StringJoiner mistakenCards = new StringJoiner(", ", "", "");
                for (String wrongCard : hardestCards) mistakenCards.add(String.format("\"%s\"", wrongCard));

                output.append(String.format("s are %s. You have %d errors answering them.", mistakenCards, maxMistakes));
            }
            logger.info(output.toString());
        }
    }

    private void resetStats() {
        cardsWithMistakes.clear();
        logger.info("Card statistics has been reset.");
    }

    void playCards() {
        boolean exit = false;
        while (!exit) {
            logger.info("Input action (add, remove, import, export, ask, exit, log, hardest card, reset stats):");
            String action = in.nextLine();
            logger.debug(action);
            switch (action) {
                case "ask"          -> ask();
                case "remove"       -> remove();
                case "add"          -> addCard();
                case "log"          -> logToFile();
                case "reset stats"  -> resetStats();
                case "hardest card" -> hardestCard();
                case "import"       -> importFlashcards();
                case "export"       -> exportFlashcards();
                case "exit"         -> exit = true;
            }
        }
        System.out.println("Bye bye!");
        if (exportFile != null) exportToFile(exportFile);
    }
}