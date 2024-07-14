// -----------------------------------------------------
// Assignment 2
// Question:
// Written by: Hongyu Chen  40070191
// The Main class serves as the driver class for a movie management application that utilizes the MovieManager class
// to perform a series of operations related to movie record processing.
// -----------------------------------------------------

public class Main {
    public static void main(String[] args) {
        // part 1’s manifest file
        String part1_manifest = "part1_manifest.txt";
        // part 2’s manifest file
        String part2_manifest = "part2_manifest.txt";
        // part 3’s manifest file
        String part3_manifest = "part3_manifest.txt";

        MovieManager movieManager = new MovieManager();
        movieManager.do_part1(part1_manifest);  // partition
        movieManager.do_part2(part2_manifest);  // serialize
        movieManager.do_part3(part3_manifest);  // deserialize and navigate
    }
}
