import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class passwordManager {
    private static void generatePassword() {
        String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
        Random random = new Random();
        String result = "";

        for (int i = 0; i < 20; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length()); 
            char randomChar = CHARACTERS.charAt(randomIndex); 
            String randomStr = String.valueOf(randomChar);
            result = result + randomStr;
        }

        try {
            Runtime.getRuntime().exec(new String[]{"wl-copy", result});
        } catch (IOException e) {
            System.out.println("Copying of password failed");
        }

        try {
            Runtime.getRuntime().exec(new String[]{"notify-send", "Password Generation Complete", "Your password has been generated successfully and is ready for use in your clipboard"});
        } catch (IOException e) {
            System.out.println("Sending of notifiction failed");
        }

    }

    private static void findPassword(String service, String secretPath) {
        boolean serviceFound = false;
        int linesAfterMatch = 0;
        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec(new String[]{"sops", "-d", secretPath});
        } catch (IOException e) {
            System.out.println("Retrival of passwords failed");
        }

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String data = null;
        try {
            while ((data = stdInput.readLine()) != null) {
                if (serviceFound) {
                    linesAfterMatch++;

                    if (linesAfterMatch == 2) {
                        if (data.contains("password")) {
                            data = data.replace("password: ", "").trim();
                            try {
                                Runtime.getRuntime().exec(new String[]{"wl-copy", data});
                            } catch (IOException e) {
                                System.out.println("Copying of password failed");
                            }

                            try {
                                Runtime.getRuntime().exec(new String[]{"notify-send", "Password Generation Complete", "Your password has been found successfully and is ready for use in your clipboard"});
                            } catch (IOException e) {
                                System.out.println("Sending of notifiction failed");
                            }
                        }
                        break;
                    }
                } else if (data.contains(service)) {
                    serviceFound = true;
                }
            }

            //if (!serviceFound) {
            //    System.out.println("You have not provided a valid service as the second argument");
            //} 

        } catch (IOException e) {
            System.out.println("Data has failed to be read correctly");
        }
    }

    public static void main(String[] args) throws IOException{
        if (args.length > 0) {
            switch (args[0]) {
                case "generate":
                    generatePassword();
                    break;
                case "find":
                    if (args[1].length() > 0) {
                        if (args.length >= 3 && args[2].length() > 0) {
                            Path path = Paths.get("/tmp", "secrets-location");
                            if (Files.exists(path)) {
                                System.out.println("A previously used secrets location has been found, would you like this to be used instead of the location provided? (y/n)");
                                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                                String input = reader.readLine();
                                if (input.contains("y")) {
                                    try (Scanner fsc = new Scanner(path)) {
                                        while(fsc.hasNextLine()) {
                                            String line = fsc.nextLine().trim();
                                            findPassword(args[1], line);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else if (input.contains("n")) {
                                    findPassword(args[1], args[2]);
                                } else {
                                    System.out.println("Please type y or n to confirm selection");
                                }
                            } else {
                                try {
                                    Files.createFile(path);
                                    Files.writeString(path, args[2]);
                                    findPassword(args[1], args[2]);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            Path path = Paths.get("/tmp", "secrets-location");
                            if (Files.exists(path)) {
                                try (Scanner sc = new Scanner(path)) {
                                    while(sc.hasNextLine()) {
                                        String line = sc.nextLine().trim();
                                        findPassword(args[1], line);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                System.out.println("Secrets location is incorrect or not provided");
                            }
                        }
                    } else {
                        System.out.println("Service is incorrect or not provided");
                    } 
                    break;
                default:
                    System.out.println("Incorrect argument provided, availble arguments are generate and find");
                    break;
            }
        } else {
            System.out.println("No argument provided, availble arguments are generate and find");
        }
    }

}
