package bgu.spl.net.impl.BGSServer;

public class Filter {
    private static final String[] FilterWords = {"Kaki", "Pipi", "Shnozel"};

    public static String filter(String text) {
        String[] arr = text.split(" ");
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < FilterWords.length; j++) {
                if (arr[i].equals(FilterWords[j])) {
                    arr[i] = "<filtered>";
                } 
            }
        }

        return String.join(" ", arr);
    }
}
