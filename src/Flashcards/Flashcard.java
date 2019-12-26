package Flashcards;

import java.io.*;
import java.util.*;


class Flashcard {

    private final Scanner in = new Scanner(System.in);
    private Map<String, String> flashcards = new LinkedHashMap<>();
    private Map<String, Integer> cardsWithMistakes = new LinkedHashMap<>();
    private List<String> log = new ArrayList<>();
    private File exportFile = null;

    Flashcard(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if ("-import".equals(args[i])) {
                importFromFile(new File(args[i + 1]));
            }
            if ("-export".equals(args[i])) {
                exportFile = new File(args[i + 1]);
            }
        }
    }

    private boolean hasQ(String question) { return flashcards.containsKey(question); }

    private boolean hasA(String answer) { return flashcards.containsValue(answer); }

    private void addCard() {
        log.add("The card:");
        System.out.println("The card:");
        String question = in.nextLine();
        log.add(question);

        if (hasQ(question)) {
            log.add("The card \"" + question + "\" already exists.");
            System.out.println("The card \"" + question + "\" already exists.");
            return;
        }

        log.add("The definition of the card:");
        System.out.println("The definition of the card:");
        String answer = in.nextLine();
        log.add(answer);

        if (hasA(answer)) {
            log.add("The definition \"" + answer + "\" already exists.");
            System.out.println("The definition \"" + answer + "\" already exists.");
            return;
        }

        flashcards.put(question, answer);
        log.add("The pair (\"" + question + "\":\"" + answer + "\") has been added.");
        System.out.println("The pair (\"" + question + "\":\"" + answer + "\") has been added.");
    }

    private void remove() {
        log.add("The card:");
        System.out.println("The card:");
        String question = in.nextLine();
        log.add(question);

        if (hasQ(question)) {
            flashcards.remove(question);
            cardsWithMistakes.remove(question);
            log.add("The card has been removed.");
            System.out.println("The card has been removed.");
        } else {
            log.add("Can't remove \"" + question + "\": there is no such card.");
            System.out.println("Can't remove \"" + question + "\": there is no such card.");
        }
    }

    private void importFlashcards() {
        log.add("File name:");
        System.out.println("File name:");
        String fileName = in.nextLine();
        log.add(fileName);
        importFromFile(new File(fileName));
    }

    private void importFromFile(File importFile) {
        int counter = 0;
        String question;
        String answer;
        String mistakes;

        try (final Scanner scan = new Scanner(importFile)) {
            while (scan.hasNext()) {
                question = scan.nextLine();
                answer = scan.nextLine();
                mistakes = scan.nextLine();
                flashcards.put(question, answer);
                cardsWithMistakes.put(question, Integer.valueOf(mistakes));
                counter++;
            }
        } catch (FileNotFoundException ex) {
            log.add("File not found.");
            System.out.println("File not found.");
        }
        log.add(counter + " cards have been loaded.");
        System.out.println(counter + " cards have been loaded.");
    }

    private void exportFlashcards() {
        log.add("File name:");
        System.out.println("File name:");
        String fileName = in.nextLine();
        log.add(fileName);
        exportToFile(new File(fileName));
    }

    private void exportToFile(File exportFile) {
        int counter = 0;

        try (FileWriter fw = new FileWriter(exportFile)) {
            for (Map.Entry<String, String> pair : flashcards.entrySet()) {
                String mistakes = cardsWithMistakes.get(pair.getKey()).toString();
                fw.write(pair.getKey());
                fw.append("\n");
                fw.write(pair.getValue());
                fw.append("\n");
                fw.write(mistakes);
                fw.append("\n");
                counter++;
            }
            fw.flush();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        log.add(counter + " cards have been saved.");
        System.out.println(counter + " cards have been saved.");
    }

    private void ask() {
        log.add("How many times to ask?");
        System.out.println("How many times to ask?");
        int counter = Integer.parseInt(in.nextLine());
        log.add("" + counter);

        while (counter-- > 0) {
            askRandomQuestion();
        }
    }

    private void askRandomQuestion() {
        Map.Entry<String, String> pair = getRandomEntry();
        assert pair != null;
        String question = pair.getKey();
        String correctAnsw =  pair.getValue();
        log.add("Print the definition of \"" + question + "\"");
        System.out.println("Print the definition of \"" + question + "\"");
        String answer = in.nextLine();
        log.add(answer);

        if (!answer.equals(correctAnsw) && hasA(answer)) {
            cardsWithMistakes.put(question, getWrongAnswCounter(question) + 1);
            log.add("Wrong answer. The correct one is \"" + correctAnsw +
                    "\", you've just written the definition of \"" + getQuestionByAnswer(answer) + "\".");
            System.out.println("Wrong answer. The correct one is \"" + correctAnsw +
                    "\", you've just written the definition of \"" + getQuestionByAnswer(answer) + "\".");
        } else if (!answer.equals(correctAnsw)) {
            cardsWithMistakes.put(question, getWrongAnswCounter(question) + 1);
            log.add("Wrong answer. The correct one is \"" + correctAnsw + "\".");
            System.out.println("Wrong answer. The correct one is \"" + correctAnsw + "\".");
        } else {
            log.add("Correct answer.");
            System.out.println("Correct answer.");
        }
    }

    private Map.Entry<String, String> getRandomEntry() {
        int random = new Random().nextInt(flashcards.size());
        for (Map.Entry<String, String> pair : flashcards.entrySet()) {
            if (random-- <= 0) {
                return pair;
            }
        }
        return null;
    }

    private String getQuestionByAnswer(String answer) {
        for (Map.Entry<String, String> pair : flashcards.entrySet()) {
            if (answer.equals(pair.getValue())) {
                return pair.getKey();
            }
        }
        return null;
    }

    private int getWrongAnswCounter(String question) {
        return cardsWithMistakes.getOrDefault(question, 0);
    }

    private void logToFile() {
        log.add("File name:");
        System.out.println("File name:");
        String fileName = in.nextLine();
        log.add(fileName);
        File logFile = new File(fileName);

        try (FileWriter fw = new FileWriter(logFile)) {
            for (String logLine : log) {
                fw.write(logLine);
                fw.append("\n");
            }
            fw.write("The log has been saved.");
            fw.flush();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        System.out.println("The log has been saved.");
    }

    private void hardestCard() {
        if (cardsWithMistakes.isEmpty()) {
            log.add("There are no cards with errors.");
            System.out.println("There are no cards with errors.");
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
                output.append(" is \"").append(hardestCards.get(0)).append("\". You have ")
                        .append(maxMistakes).append(" errors answering it.");
            } else {
                output.append("s are ");
                for (String wrongCard : hardestCards) {
                    output.append("\"").append(wrongCard).append("\", ");
                }
                output = new StringBuilder(output.substring(0, output.length() - 2));
                output.append(". You have ").append(maxMistakes).append(" errors answering them.");
            }
            log.add(output.toString());
            System.out.println(output);
        }
    }

    private void resetStats() {
        cardsWithMistakes.clear();
        log.add("Card statistics has been reset.");
        System.out.println("Card statistics has been reset.");
    }

    void playCards() {
        boolean exit = false;
        while (!exit) {
            log.add("Input action (add, remove, import, export, ask, exit, log, hardest card, reset stats):");
            System.out.println("Input action (add, remove, import, export, ask, exit, log, hardest card, reset stats):");
            String action = in.nextLine();
            log.add(action);
            switch (action) {
                case "add":
                    addCard();
                    break;
                case "remove":
                    remove();
                    break;
                case "import":
                    importFlashcards();
                    break;
                case "export":
                    exportFlashcards();
                    break;
                case "ask":
                    ask();
                    break;
                case "exit":
                    exit = true;
                    System.out.println("Bye bye!");
                    if (exportFile != null) {
                        exportToFile(exportFile);
                    }
                    break;
                case "log":
                    logToFile();
                    break;
                case "hardest card":
                    hardestCard();
                    break;
                case "reset stats":
                    resetStats();
                    break;
            }
        }
    }
}