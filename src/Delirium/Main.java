package Delirium;

public class Main {
    public static void main(String[] args) {
        Delirium vGen = new Delirium();

//        vGen.genFiles(".//DeliriumTest//", 12, 7, new String[] {"мама", "мыла", "раму"}, 5);

        try {
            vGen.genFiles(".//DeliriumTest//", 1000, 7, 66, 1, new String[]{"мама", "мыла", "раму"}, 5);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

    }
}
