// -----------------------------------------------------
// Assignment 2
// Question:
// Written by: Hongyu Chen  40070191
// The class MovieManager is created for managing movie records, including error logging, reading and partitioning
// movie records based on their genres, serialization, and navigation through movie records.
// The basic functionalities of the class include:
// 1. Initialization: Upon instantiation, the MovieManager initializes an array for storing error messages
// with a maximum limit (MAX_ERRORS) and a flag array to keep track of whether movies of a certain genre
// have been written to file.
// 2. File Reading and Partitioning (do_part1): This method reads a manifest file containing names of input files
// with movie records. For each file listed, it reads and processes movie records, partitioning them into
// genre-specific files. It handles file existence checks and errors related to reading files. Errors are stored
// in an array and logged to a file (bad-movie_records.txt).
// 3. Serialization (do_part2): Reads a manifest file listing genre-specific CSV files, loads movies from these files,
// serializes the movie records into binary format (*.ser files), and writes the names of these serialized files
// to a new manifest file.
// 4. Deserialization and Navigation (do_part3): Deserializes the movie arrays from the binary files listed in a
// manifest file and allows the user to navigate through the movie records interactively through the console.
// It supports selecting genres and navigating through movies within a genre.
// -----------------------------------------------------

import Exceptions.*;
import java.io.*;
import java.util.Scanner;
import static java.lang.String.*;

public class MovieManager {

    private static final int MAX_ERRORS = 10;
    private final String[] errors;
    private int errorCount;
    private static final String[] GENRES = {
            "musical", "comedy", "animation", "adventure", "drama", "crime", "biography", "horror",
            "action", "documentary", "fantasy", "mystery", "sci-fi", "family", "romance", "thriller", "western"
    };
    private static final String PART2_MANIFEST = "part2_manifest.txt";
    private static final String PART3_MANIFEST = "part3_manifest.txt";
    private final boolean[] genreWrittenFlag = new boolean[GENRES.length];
    private static final int MAX_MOVIES = 1000;

    // Constructor initializes the MovieManager with default settings.
    public MovieManager() {
        errors = new String[MAX_ERRORS];
        for (int i = 0; i < GENRES.length; i++) {
            genreWrittenFlag[i] = false;
        }
    }

    /**
     * Adds an error message to the errors array if the number of recorded errors has not reached the maximum.
     * @param errorMessage The error message to add.
     */
    private void addError(String errorMessage) {
        if (errorCount < MAX_ERRORS) {
            errors[errorCount++] = errorMessage;
        }
    }

    /**
     * Logs an error message along with the file name and line number to a specified PrintWriter.
     * @param errorWriter The PrintWriter to log errors to.
     * @param inputFile The name of the input file where the error occurred.
     * @param lineNumber The line number in the file where the error was detected.
     * @param errorMessage The error message to log.
     * @throws IOException If an I/O error occurs during writing.
     */
    private void logError(PrintWriter errorWriter, String inputFile, int lineNumber, String errorMessage)
            throws IOException {
        errorWriter.println("File: " + inputFile + ", Line: " + lineNumber + ", Error: " + errorMessage);
        errorWriter.flush();
    }

    /**
     * Processes the first part of the movie data processing pipeline by reading and partitioning movies based on a manifest file.
     * @param manifestFilePath Path to the manifest file listing input files.
     */
    public void do_part1(String manifestFilePath) {
        // Reset genre flags for new processing
        for (int i = 0; i < GENRES.length; i++) {
            genreWrittenFlag[i] = false;
        }
        // Load and verify the manifest file exists
        File manifestFile = new File(manifestFilePath);
        if (!manifestFile.exists()) {
            System.err.println("Manifest file does not exist: " + manifestFilePath);
            return;
        }
        // Process each input file listed in the manifest
        try (BufferedReader manifestReader = new BufferedReader(new FileReader(manifestFile))) {
            String inputFile;
            while ((inputFile = manifestReader.readLine()) != null) {
                // Build the absolute or relative path to the input file based on the manifest file's directory
                String inputFilePath = new File(manifestFile.getParent(), inputFile).getAbsolutePath();
                File input = new File(inputFilePath);
                if (!input.exists()) {
                    System.err.println("Input file listed in manifest does not exist: " + inputFilePath);
                    continue; // Skip this file and continue with the next
                }
                // Process the movie data
                readAndPartitionMovie(inputFilePath);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Manifest file not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error reading the manifest file: " + e.getMessage());
        }
        // After processing, write genre-specific data
        writeGenresToManifest();
    }

    /**
     * Processes the second part of the movie data pipeline, serializing movie objects based on a second manifest.
     * @param PART2_MANIFEST Path to the manifest file for part 2, listing genre-specific CSV files.
     */
    public void do_part2(String PART2_MANIFEST) {
        File part2ManifestFile = new File(PART2_MANIFEST);
        try (BufferedReader part2ManifestReader = new BufferedReader(new FileReader(part2ManifestFile))) {
            String genreFileName;
            try (PrintWriter part3ManifestWriter = new PrintWriter(new FileWriter(PART3_MANIFEST))) {
                while ((genreFileName = part2ManifestReader.readLine()) != null) {
                    Movie[] movies = loadMoviesFromCSV(genreFileName);
                    if (movies.length > 0) {
                        // Serialize movie data and update part 3 manifest
                        serializeMovieArray(movies, genreFileName.replace(".csv", ".ser"));
                        part3ManifestWriter.println(genreFileName.replace(".csv", ".ser"));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Part 2 manifest file not found: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error reading the Part 2 manifest file: " + e.getMessage());
        }
    }

   /**
   * Processes the third part of the movie data pipeline, deserializing movie arrays for navigation.
   * @param PART3_MANIFEST Path to the manifest file for part 3.
   */
    public Movie[][] do_part3(String PART3_MANIFEST) {
        Movie[][] allMovies = deserializeMovieArray(PART3_MANIFEST);
        navigateMovieArrays(allMovies);

        return allMovies;
    }

    /**
     * Reads movie data from a file, validates each movie record, and partitions validated records into genre-specific files.
     * Invalid records are logged to an error file.
     * @param inputFile Path to the file containing movie records.
     * @throws FileNotFoundException If the specified input file does not exist.
     */
    public void readAndPartitionMovie(String inputFile) throws FileNotFoundException {
        String line;
        int lineNumber = 0;
        File errorFile = new File("bad-movie_records.txt");

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile));
             PrintWriter errorWriter = new PrintWriter(new FileOutputStream(errorFile, true))) {

            while ((line = br.readLine()) != null) {
                lineNumber++;
                errorCount = 0;
                try {
                    // Validate the current movie record
                    Movie movie = validateMovieRecord(line);
                    if (movie != null) {
                        // Write valid movie to its respective genre file
                        writeMovieToGenreFile(movie);
                    }
                } catch (MissingQuoteException | ExcessFieldsException | MissingFieldsException e) {
                    logError(errorWriter, inputFile, lineNumber, e.getMessage());
                } catch (Exception e) {
                    addError(e.getMessage());
                }
                // If there are any collected errors, log them
                if (errorCount > 0) {
                    for (int i = 0; i < errorCount; i++) {
                        if (errors[i] != null) {
                            logError(errorWriter, inputFile, lineNumber, errors[i]);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the input file: " + e.getMessage());
        }
    }

    /**
     * Validates a single movie record, ensuring it meets expected format and data requirements.
     * @param record A string representing a single line from the movie records file.
     * @return A Movie object if the record is valid, otherwise null.
     * @throws MissingQuoteException, ExcessFieldsException, MissingFieldsException, and other validation exceptions if the record is invalid.
     */
    private Movie validateMovieRecord(String record) throws MissingQuoteException, ExcessFieldsException,
            MissingFieldsException, BadScoreException, BadTitleException, BadGenreException,
            BadDurationException, BadNameException, BadRatingException, BadYearException {
        if (record == null || record.isEmpty()) {
            return null;
        }

        // Expected number of fields in a movie record
        final int expectedFieldCount = 10;
        // Array to hold the fields extracted from the record
        String[] fields = new String[expectedFieldCount];
        int fieldIndex = 0;
        StringBuilder currentField = new StringBuilder();
        // Flag to handle quoted fields which may contain commas
        boolean inQuotes = false;

        // Parse the record one character at a time
        for (int i = 0; i < record.length(); i++) {
            char ch = record.charAt(i);
            if (ch == '\"') {
                inQuotes = !inQuotes; // Toggle the inQuotes flag
                // Optionally handle appending quotes to field value here, if needed
            } else if (ch == ',' && !inQuotes) {
                if (fieldIndex < expectedFieldCount) {
                    fields[fieldIndex++] = currentField.toString().trim();
                    currentField.setLength(0); // Clear the StringBuilder for the next field
                    if (fieldIndex >= expectedFieldCount) {
                        // Break if the next field would exceed the array bounds
                        break;
                    }
                } else {
                    // If this line is reached, it means an extra field is being processed beyond the expected count
                    throw new ExcessFieldsException("Excess number of fields.");
                }
            } else {
                currentField.append(ch);
            }
        }
        // After the loop, if not in the middle of processing a quoted field and have not exceeded field count, assign the last field
        if (!inQuotes && fieldIndex < expectedFieldCount) {
            fields[fieldIndex] = currentField.toString().trim();
        }

        // Check if all fields were filled
        if (fieldIndex < expectedFieldCount - 1) {
            throw new MissingFieldsException("Missing fields. Expected " + expectedFieldCount + " but found " + (fieldIndex + 1));
        }

        // Validation of individual fields can proceed here
        validateFields(fields);

        // Assuming createMovieFromFields method exists and properly constructs a Movie object from fields
        return createMovieFromFields(fields);
    }

    /**
     * Validates the various fields of a movie record.
     * This method calls specific validation methods for each field of a movie record.
     * @param fields An array of String, where each element is a field from a movie record.
     */
    private void validateFields(String[] fields) throws BadYearException, BadTitleException, BadGenreException,
            BadScoreException, BadDurationException, BadRatingException, BadNameException, MissingQuoteException {
        validateYear(fields[0]);
        validateTitle(fields[1]);
        validateDuration(fields[2]);
        validateGenre(fields[3]);
        validateRating(fields[4]);
        validateScore(fields[5]);
        String[] names = {fields[6], fields[7], fields[8], fields[9]};
        validateNames(names);
    }

    /**
     * Validates the year field of a movie record.
     * @param yearString The year as a String.
     * @throws BadYearException If the year is not within the range 1990 to 1999.
     */
    private void validateYear(String yearString) throws BadYearException {
        try {
            int year = Integer.parseInt(yearString);
            if (year < 1990 || year > 1999) {
                throw new BadYearException("Invalid year: " + year + ". The year must be between 1990 and 1999.");
            }
        } catch (NumberFormatException e) {
            throw new BadYearException("The year must be an integer between 1990 and 1999.");
        }
    }

    /**
     * Validates the title field of a movie record.
     * @param title The title of the movie.
     * @throws BadTitleException If the title is missing.
     * @throws MissingQuoteException If quotes are required but missing.
     */
    private void validateTitle(String title) throws BadTitleException, MissingQuoteException {
        if (title == null || title.isEmpty()) {
            throw new BadTitleException("Missing title");
        }
    }

    /**
     * Validates the duration field of a movie record.
     * @param durationString The duration in minutes as a String.
     * @throws BadDurationException If the duration is not between 30 and 300 minutes.
     */
    private void validateDuration(String durationString) throws BadDurationException {
        try {
            int duration = Integer.parseInt(durationString);
            if (duration < 30 || duration > 300) {
                throw new BadDurationException("Invalid duration: " + duration);
            }
        } catch (NumberFormatException e) {
            throw new BadDurationException("The duration must be an integer between 30 and 300 minutes.");
        }
    }

    /**
     * Validates the genre field of a movie record.
     * @param genre The genre of the movie.
     * @throws BadGenreException If the genre is not recognized.
     */
    private void validateGenre(String genre) throws BadGenreException {
        if (genre == null || genre.isEmpty()) {
            throw new BadGenreException("Missing genre");
        }
        genre = genre.toLowerCase();
        if (!(genre.equals("musical") || genre.equals("comedy") || genre.equals("animation") || genre.equals("adventure") ||
                genre.equals("drama") || genre.equals("crime") || genre.equals("biography") || genre.equals("horror") ||
                genre.equals("action") || genre.equals("documentary") || genre.equals("fantasy") || genre.equals("mystery") ||
                genre.equals("sci-fi") || genre.equals("family") || genre.equals("romance") || genre.equals("thriller") ||
                genre.equals("western"))) {
            throw new BadGenreException("Invalid genre: " + genre);
        }
    }

    /**
     * Validates the rating field of a movie record.
     * @param rating The rating of the movie.
     * @throws BadRatingException If the rating is not recognized.
     */
    private void validateRating(String rating) throws BadRatingException {
        if (rating == null || rating.isEmpty()) {
            throw new BadRatingException("Missing rating");
        }
        rating = rating.toLowerCase();
        if (!(rating.equals("pg") || rating.equals("unrated") || rating.equals("g") || rating.equals("r") ||
                rating.equals("pg-13") || rating.equals("nc-17"))) {
            throw new BadRatingException("Invalid rating: " + rating);
        }
    }

    /**
     * Validates the score field of a movie record.
     *
     * @param scoreString The score field as a String, which needs to be validated.
     * @throws BadScoreException If the score is not within the specified range or cannot be parsed as a double.
     */
    private void validateScore(String scoreString) throws BadScoreException {
        try {
            double score = Double.parseDouble(scoreString);
            if (score < 0.0 || score > 10.0) {
                throw new BadScoreException("Invalid score: " + score + ". Score must be between 0.0 and 10.0.");
            }
        } catch (NumberFormatException e) {
            throw new BadScoreException("Score must be a positive double value less than or equal to 10.");
        }
    }

    /**
     * Validates the name fields in a movie record.
     *
     * @param names An array of Strings representing the name fields in a movie record that need to be validated.
     * @throws BadNameException If any name in the array is null, empty, or only whitespace.
     */
    private void validateNames(String[] names) throws BadNameException {
        for (String name : names) {
            if (name == null || name.trim().isEmpty()) {
                throw new BadNameException("Missing name(s) in the record.");
            }
        }
    }

    /**
     * Creates a Movie object from the array of String fields.
     *
     * @param fields An array of Strings representing the fields of a movie.
     * @return A new Movie object constructed from the provided fields.
     */
    private Movie createMovieFromFields(String[] fields) {
        int year = Integer.parseInt(fields[0].trim());
        String title = fields[1].trim().replaceAll("^\"|\"$", ""); // Remove surrounding quotes
        int duration = Integer.parseInt(fields[2].trim());
        String genres = fields[3].trim();
        String rating = fields[4].trim();
        double score = Double.parseDouble(fields[5].trim());
        String director = fields[6].trim();
        String actor1 = fields[7].trim();
        String actor2 = fields[8].trim();
        String actor3 = fields[9].trim();

        return new Movie(year, title, duration, genres, rating, score, director, actor1, actor2, actor3);
    }

    /**
     * Writes a movie's data to a genre-specific CSV file. If the genre has not been written to before,
     * it flags it as written to prevent duplicate headers or initializations.
     *
     * @param movie The movie object to write to the file.
     */
    private void writeMovieToGenreFile(Movie movie) {
        // Generate the file name based on the movie's genre, converting to lowercase for consistency
        String genreFileName = movie.getGenres().toLowerCase() + ".csv";
        // Get the index of the genre in a predefined list of genres
        int genreIndex = getGenreIndex(movie.getGenres());
        try (PrintWriter pw = new PrintWriter(new FileWriter(genreFileName, true))) {
            // Convert the movie object to a CSV formatted String
            String movieRecord = convertMovieToCSV(movie);
            // Write the formatted movie record to the file
            pw.println(movieRecord);
            // Flag the genre as having been written if it hasn't been already
            if (genreIndex != -1 && !genreWrittenFlag[genreIndex]) {
                genreWrittenFlag[genreIndex] = true;
            }
        } catch (IOException e) {
            System.err.println("Error writing movie to genre file: " + e.getMessage());
        }
    }

    /**
     * Retrieves the index of a given genre in a predefined list of genres.
     *
     * @param genre The genre to find the index of.
     * @return The index of the genre in the list, or -1 if not found.
     */
    private int getGenreIndex(String genre) {
        for (int i = 0; i < GENRES.length; i++) {
            if (GENRES[i].equalsIgnoreCase(genre)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Converts a Movie object into a CSV-formatted string.
     *
     * @param movie The Movie object to convert.
     * @return A String representing the movie in CSV format.
     */
    private String convertMovieToCSV(Movie movie) {
        return format("%d, %s, %d, %s, %s, %f, %s, %s, %s, %s",
                movie.getYear(),
                movie.getTitle(),
                movie.getDuration(),
                movie.getGenres(),
                movie.getRating(),
                movie.getScore(),
                movie.getDirector(),
                movie.getActor1(),
                movie.getActor2(),
                movie.getActor3());
    }

    /**
     * Writes genre filenames to the second part manifest file.
     * This method is used to prepare the manifest for the next phase of processing,
     * listing each genre file that has been created or modified.
     */
    public void writeGenresToManifest() {
        try (PrintWriter manifestWriter = new PrintWriter(new FileWriter(PART2_MANIFEST))) {
            for (String genre : GENRES) {
                // For each genre, write the corresponding file name to the manifest
                String fileName = genre + ".csv";
                manifestWriter.println(fileName);
            }
        } catch (IOException e) {
            System.err.println("Error writing to part2_manifest.txt: " + e.getMessage());
        }
    }

    /**
    * Loads movies from a CSV file into an array of Movie objects.
    * This method reads each line from the given file, validates and converts it into a Movie object,
    * and then adds it to an array of movies. If any validation errors occur, they are printed to the console.
    * @param fileName The name of the CSV file from which to load movies.
    * @return An array of Movie objects loaded from the CSV file. The array size is equal to the number of successfully
    *         loaded movies, which may be less than the capacity of the initial array if some records are invalid or if
    *         the file contains fewer than MAX_MOVIES records.
    */
    private Movie[] loadMoviesFromCSV(String fileName) {
        Movie[] movies = new Movie[MAX_MOVIES];
        int movieCount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null && movieCount < MAX_MOVIES) {
                try {
                    Movie movie = validateMovieRecord(line);
                    if (movie != null) {
                        movies[movieCount++] = movie;
                    }
                } catch (MissingQuoteException | BadScoreException | BadTitleException | BadGenreException |
                         BadDurationException | MissingFieldsException | BadNameException | BadRatingException |
                         ExcessFieldsException | BadYearException e) {
                    e.getMessage();
                }
            }
        } catch (FileNotFoundException e) {
            e.getMessage();
//            System.out.println("File " + fileName + "not found.");
        } catch (IOException e) {
            System.out.println("Error reading the file " + fileName + ": " + e.getMessage() + ".");
        }
        return java.util.Arrays.copyOf(movies, movieCount);
    }

    /**
     * Serializes an array of Movie objects to a file. This method takes an array of Movie objects
     * and writes it to a specified file using object serialization, which allows for the
     * persistent storage of complex objects in a way that can be reversed (deserialized) later.
     *
     * @param movies The array of Movie objects to be serialized.
     * @param fileName The name of the file where the serialized data will be stored.
     */
    public void serializeMovieArray(Movie[] movies, String fileName) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(movies);
        } catch (FileNotFoundException e) {
            System.out.println("File " + fileName + " not found.");
        } catch (IOException e) {
            System.out.println("Error serializing movies array to file " + fileName + ".");
        }
    }

    /**
     * Deserializes arrays of Movie objects from files listed in a manifest file. Each file contains
     * a serialized array of Movie objects corresponding to a specific genre. This method reads the manifest file,
     * deserializes each listed file into an array of Movie objects, and stores these arrays in a 2D array
     * indexed by genre.
     *
     * @param PART3_MANIFEST The path to the manifest file listing the serialized movie files.
     * @return A 2D array of Movie objects, where each sub-array contains movies of a specific genre.
     */
    public Movie[][] deserializeMovieArray(String PART3_MANIFEST) {
        // Initialize a 2D array to hold the arrays of Movie objects, one per genre
        Movie[][] movies2D = new Movie[GENRES.length][];
        File part3ManifestFile = new File(PART3_MANIFEST);

        // Check each genre to find a matching serialized file
        try (BufferedReader br = new BufferedReader(new FileReader(part3ManifestFile))) {
            String binaryFileName;
            while ((binaryFileName = br.readLine()) != null) {
                boolean found = false;
                for (int i = 0; i < GENRES.length; i++) {
                    if (binaryFileName.toLowerCase().startsWith(GENRES[i].toLowerCase())) {
                        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(binaryFileName))) {
                            // Deserialize the file into an array of Movie objects and store it in the 2D array
                            movies2D[i] = (Movie[]) ois.readObject();
                            found = true;
                            // Stop searching once the matching genre is found
                            break;
                        } catch (FileNotFoundException e) {
                            System.err.println("Could not find file for genre: " + GENRES[i]);
                        } catch (ClassNotFoundException | IOException e) {
                            System.err.println("Error deserializing file for genre: " + GENRES[i]);
                        }
                    }
                }
                if (!found) {
                    System.err.println("No matching genre found for file: " + binaryFileName);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Part 3 manifest file not found.");
        } catch (IOException e) {
            System.err.println("Error reading the Part 3 manifest file.");
        }
        return movies2D;
    }

    /**
     * Provides a navigation system for browsing movies stored in a 2D array, where each row represents a different genre.
     * Users can select genres, navigate through movies within those genres, and view details of specific movies.
     * Navigation commands are input through the console.
     *
     * @param allMovies A 2D array of Movie objects, categorized by genre.
     */
    private void navigateMovieArrays(Movie[][] allMovies) {
        Scanner scanner = new Scanner(System.in);
        int currentGenre = 0; // Index of the currently selected genre.
        int currentMovieIndex = 0; // Index of the currently highlighted movie within the selected genre.

        String choice;
        do {
            // Display the main menu and prompt for a choice.
            displayMainMenu(allMovies, currentGenre);
            System.out.print("Enter Your Choice: ");
            choice = scanner.nextLine().toLowerCase();

            switch (choice.toLowerCase()) {
                case "s":
                    // Select a new genre to navigate.
                    currentGenre = selectGenre(allMovies, scanner);
                    currentMovieIndex = 0; // Reset movie index upon genre change.
                    break;
                case "n":
                    // Navigate within the selected genre if it contains movies.
                    if (allMovies[currentGenre] != null && allMovies[currentGenre].length > 0) {
                        currentMovieIndex = navigateGenreMovies(allMovies[currentGenre], currentMovieIndex, scanner);
                    } else {
                        System.out.println("No records in this genre.");
                    }
                    break;
                case "x":
                    System.out.println("Exiting navigation.");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    break;
            }
        } while (!choice.equals("x"));
        scanner.close();
    }

    /**
     * Displays the main menu with options to select a genre, navigate within a genre, or exit the navigation system.
     * Shows the number of records available in the currently selected genre.
     *
     * @param allMovies A 2D array of Movie objects, categorized by genre.
     * @param currentGenre The index of the currently selected genre.
     */

    private void displayMainMenu(Movie[][] allMovies, int currentGenre) {
        System.out.println("-------------------------------");
        System.out.println("            Main Menu          ");
        System.out.println("-------------------------------");
        System.out.println("Welcom to the movie navigation system created by Hongyu!");
        System.out.println();
        System.out.println("s: Select a movie array to navigate");
        if (allMovies[currentGenre] != null) {
            System.out.println("n: Navigate " + GENRES[currentGenre] +
                    " movies (" + allMovies[currentGenre].length + " records)");
        } else {
            System.out.println("n: Navigate musical movies (0 records)");
        }
        System.out.println("x: Exit");
        System.out.println("-------------------------------");
    }

    /**
     * Allows the user to select a genre to navigate. Presents a list of genres, each with a count of available movies.
     * Waits for user input and returns the index of the selected genre.
     *
     * @param allMovies A 2D array of Movie objects, categorized by genre.
     * @param scanner A Scanner object for reading user input.
     * @return The index of the selected genre.
     */
    private int selectGenre(Movie[][] allMovies, Scanner scanner) {
        int genreChoice = -1;
        while (genreChoice < 0 || genreChoice >= GENRES.length) {
            System.out.println("-------------------------------");
            System.out.println("         Genre Sub-Menu        ");
            System.out.println("-------------------------------");
            for (int i = 0; i < GENRES.length; i++) {
                if (allMovies[i] != null) {
                    System.out.println((i + 1) + ": " + GENRES[i] + " (" + allMovies[i].length + " movies)");
                } else {
                    System.out.println((i + 1) + ": " + GENRES[i] + " (0 movies)");
                }
            }
            System.out.println("-------------------------------");
            System.out.print("Enter Your Choice: ");

            try {
                genreChoice = Integer.parseInt(scanner.nextLine()) - 1; // Convert to 0-based index
                if (genreChoice < 0 || genreChoice >= GENRES.length) {
                    System.out.println("Invalid choice. Please try again.");
                } else {
                    return genreChoice;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        return genreChoice; // This will only execute if a valid choice is made
    }

    /**
     * Allows the user to navigate through movies of a selected genre.
     * The user can move forwards or backwards through the list of movies based on numerical input.
     * Entering '0' returns the user to the main menu. Positive numbers move forward through the list,
     * while negative numbers move backwards. The method ensures navigation remains within the bounds of the movie array.
     *
     * @param movies An array of Movie objects representing movies of a specific genre.
     * @param currentMovieIndex The current index within the movies array being displayed.
     * @param scanner A Scanner object for reading user input from the console.
     * @return The new current movie index after navigation.
     */
    private int navigateGenreMovies(Movie[] movies, int currentMovieIndex, Scanner scanner) {
        int choice;
        do {
            System.out.println("Navigating " + movies[0].getGenres() + " movies (" + movies.length + ")");
            System.out.print("Enter Your Choice (0 to return to the main menu): ");
            choice = Integer.parseInt(scanner.nextLine());

            if (choice != 0) {
                int prevIndex = currentMovieIndex;
                if (choice < 0) {
                    currentMovieIndex = Math.max(0, currentMovieIndex + choice);
                } else {
                    currentMovieIndex = Math.min(movies.length - 1, currentMovieIndex + choice - 1);
                }
                displayMovies(movies, prevIndex, currentMovieIndex);
            }
        } while (choice != 0);
        return currentMovieIndex;
    }

    /**
     * Displays a range of movies from a given array, specified by starting and ending indices.
     * This function is designed to showcase a sequential list of movies to the user, handling cases where
     * the index is out of bounds by displaying appropriate messages (BOF for beginning of file, EOF for end of file).
     *
     * @param movies An array of Movie objects to display.
     * @param prevIndex The previous index from which the user navigated.
     * @param newIndex The new index to which the user has navigated.
     */
    private void displayMovies(Movie[] movies, int prevIndex, int newIndex) {
        if (newIndex < 0 || newIndex >= movies.length) {
            System.out.println("Index out of bounds. No movies to display.");
            return;
        }
        // Determine the start and end indices for displaying movies.
        int startIndex = Math.min(prevIndex, newIndex);
        int endIndex = Math.max(prevIndex, newIndex);

        // Loop through the specified range and display each movie.
        for (int i = startIndex; i <= endIndex; i++) {
            if (i < 0) {
                System.out.println("BOF has been reached.");
                break;
            } else if (i >= movies.length) {
                System.out.println("EOF has been reached.");
                break;
            } else {
                System.out.println((i+1) + ": " + movies[i]);
            }
        }
    }
}