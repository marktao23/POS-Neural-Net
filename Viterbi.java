import java.io.*;
import java.util.*;

/**
 * @author Mark Tao, create a Part of Speech tagger using a Hidden Markov Model, using pseuducode from Katherine Paradise and Alberto Li
 */

public class Viterbi {

    //initializing all instance variables for the path, words, tags, and probabilities

    ArrayList<String> hasNotBeenTested;
    ArrayList<String> Path;

    Map<String, Map<String, Integer>> transitions = new HashMap<>();
    Map<String, Map<String, Integer>> words = new HashMap<>();

    Map<String, Map<String, Double>> tagProbability;
    Map<String, Map<String, Double>> obsProbability;

    Map<String, Double> scores = new HashMap<>();
    Set<String> states = new HashSet<>();
    ArrayList<Map<String, String>> viterbiTracker = new ArrayList<>();

    int tagTrack = 1;
    int wordTrack = 1;

    ArrayList<String> input = new ArrayList<>();
    ArrayList<String> wordsToTest = new ArrayList<>();

    //viterbi decoder method
    public void decoder(ArrayList<String> obs) {

        //adding in initial elements into the states and scores.
        String victor = "";

        states.add("#");
        scores.put("#", 0.0);

        //for loop that tracks the next state, its scores, and its backpointer
        for (int a = 0; a < obs.size(); a ++) {

            Map<String, String> backPointer = new HashMap<>();

            Set<String> nextState = new HashSet<>();
            Map<String, Double> nextScore  = new HashMap<>();

            //nested for loop going over the state and its transition, calculating the score of the next state
            for (String currentState: states) {
                for (String transition: tagProbability.get(currentState).keySet()) {
                    nextState.add(transition);

                    double totalScoreOfNext;

                    Double x = scores.get(currentState);
                    Double y = tagProbability.get(currentState).get(transition);
                    Double z = obsProbability.get(transition).get(obs.get(a + 1));

                    totalScoreOfNext = x + y + z;

                    //if the transition isn't in nextScore, or if nextScore is greater than the transition then add it to nextScore
                    if (nextScore.containsKey(transition) == false || totalScoreOfNext > nextScore.get(transition)) {
                        nextScore.put(transition, totalScoreOfNext);
                        backPointer.put(transition, currentState);
                    }
                }
            }

            //use the backpointer to keep track of the final path
            viterbiTracker.add(backPointer);
            states = nextState;
            scores = nextScore;
        }

        //loop through all keys in scores, and finalize value of victor
        for (String string: scores.keySet()) {
            if (victor.length() > 1) {
                victor = string;
            }
            else if (scores.get(victor) < scores.get(string)) {
                victor = string;
            }
        }

        //for loop that creates the path for tagging the input
        for (int b = viterbiTracker.size() - 1; b > 0; b --) {
            Path.add(0, victor);
            victor = viterbiTracker.get(b).get(victor);
        }
    }

    //method that trains sudi
    public void trainModel(ArrayList<String> tags, ArrayList<String> lines) {

        //creating a string that will keep track of the tags
        String tagger = tags.get(tagTrack);

        //for loop that goes through and adds the tags into the map
        for (String string : tags) {

            //if the tracker is less than the size of the list
            if (tagTrack < tags.size() - 1) {

                //if the key is in transitions and the tracker's key is also in transitions, then add the element into transitions
                if (transitions.containsKey(string) && transitions.get(string).containsKey(tags.get(tagTrack))) {
                    transitions.get(string).put(tags.get(tagTrack), transitions.get(string).get(tags.get(tagTrack) + 1));

                //if the key is only in transitions, put it in the first spot
                } else if (transitions.containsKey(string)) {
                    transitions.get(string).put(tags.get(tagTrack), 1);
                }
                //add it in if it's not in there
                else {
                    Map<String, Integer> tagMap = new HashMap<>();
                    tagMap.put(tagger, 1);
                    transitions.put(string, tagMap);
                }
                //increment by 1
                tagTrack = tagTrack + 1;
            }
        }

        //loop through and load the input words into the map
        for (String string : lines) {

            //if tracker is less than size of the list
            if (tagTrack < tags.size() - 1) {

                //performs the same operations as above, but it retrieves the tracker of the words and increments appropriately
                if (words.containsKey(tags.get(wordTrack)) && words.get(tags.get(wordTrack)).containsKey(string)) {
                    words.get(tags.get(wordTrack)).put(tags.get(tagTrack), words.get(tags.get(wordTrack)).get(string) + 1);
                } else if (words.containsKey(tags.get(wordTrack))) {
                    words.get(tags.get(wordTrack)).put(string, 1);
                } else {
                    Map<String, Integer> tagMap2 = new HashMap<>();
                    tagMap2.put(string, 1);
                    words.put(tags.get(wordTrack), tagMap2);
                }
                //increment both the tracker of the tag and the word
                tagTrack = tagTrack + 1;
            }
            wordTrack = wordTrack + 1;
        }

        int trans = 0;
        int transObs = 0;
        Map<String, Double> prob = new HashMap<>();

        //now that both maps are done, iterate through both of them and calculate the log probabilty of each part of speech
        for (String string : transitions.keySet()) {
            trans = getTransHelper(trans, prob, string, transitions);

            transObs = getTransHelper(transObs, prob, string, words);
            tagProbability.put(string, prob);
            obsProbability.put(string, prob);
        }
    }

    //helper method that returns the value of the final transitions/transitions observation values
    private int getTransHelper(int trans, Map<String, Double> prob, String string, Map<String, Map<String, Integer>> transitions) {
        for (String s : transitions.get(string).keySet()) {
            trans = trans + transitions.get(string).get(s);
        }

        for (String x: transitions.get(string).keySet()) {
            prob.put(x, Math.log((double) transitions.get(string).get(x) / trans));
        }
        return trans;
    }

    //console driven test that splits the words and adds them into the input
    public void consoleTest(Viterbi sudiBot) {
        System.out.println("Hi, I'm Sudi. I'll be your speaking assistant. What would you like to tag?");
        Scanner s = new Scanner(System.in);

        String nextWord = s.nextLine();
        String[] splitWords = nextWord.split(" ");
        input.add("#");
        input.addAll(Arrays.asList(splitWords));

        sudiBot.decoder(input);

    }

    //method that loads in the tags
    public ArrayList<String> loadInTags(String file) throws IOException {

        return helper(file, hasNotBeenTested);
    }

    //method that loads in the words
    public ArrayList<String> loadInFile(String file) throws IOException {

        return helper(file, wordsToTest);
    }

    //returns an arraylist (split and read in files for both the tags and the words)
    private ArrayList<String> helper(String file, ArrayList<String> toTest) throws IOException {

        try (BufferedReader inputFile = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = inputFile.readLine()) != null) {

                String nextLine = line.toLowerCase();
                String[] splitWords = nextLine.split(" ");
                toTest.add("#");

                toTest.addAll(Arrays.asList(splitWords));
            }
        } catch (FileNotFoundException e) {
            System.err.println(e);
        }
        return toTest;
    }
}
