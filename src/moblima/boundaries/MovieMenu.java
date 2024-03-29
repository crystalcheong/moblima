package moblima.boundaries;

import moblima.control.handlers.ReviewHandler;
import moblima.entities.Movie;
import moblima.entities.Movie.ContentRating;
import moblima.entities.Movie.ShowStatus;
import moblima.entities.Review;
import moblima.utils.Helper;
import moblima.utils.Helper.Preset;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static moblima.entities.Review.MAX_RATING;
import static moblima.entities.Review.MIN_RATING;
import static moblima.utils.Helper.colorPrint;

/**
 * The type Movie menu.
 */
public class MovieMenu extends Menu {
  private static boolean showLimitedMovies = true;
  private static boolean showReviews = true;
  private static ReviewHandler handler;
  private static MovieMenu instance;

  private MovieMenu() {
    super();
    handler = new ReviewHandler();
    this.refreshMenu(this.getMovieMenu());
  }

  /**
   * Gets instance.
   *
   * @return the instance
   */
  public static MovieMenu getInstance() {
    return getInstance(showLimitedMovies, showReviews);
  }

  /**
   * Gets instance.
   *
   * @param showLimited the show limited
   * @param showReviews the show reviews
   * @return the instance
   */
  public static MovieMenu getInstance(boolean showLimited, boolean showReviews) {
    showLimitedMovies = showLimited;
    showReviews = showReviews;
    if (instance == null) instance = new MovieMenu();
    return instance;
  }

  /**
   * Gets handler.
   *
   * @return the handler
   */
//+ getHandler():MovieHandler
  public static ReviewHandler getHandler() {
    return handler;
  }

  @Override
  public void showMenu() {
    Helper.logger("MovieMenu.showMenu", "Displaying menu . . .\n" + this.menuMap);
    this.displayMenu();
  }

  /**
   * Gets viewable movies.
   *
   * @return the viewable movies
   */
//+ getViewableMovie(): List<Movie>
  public List<Movie> getViewableMovies() {
    List<Movie> movies = handler.getMovies();

    List<ShowStatus> unlistedStatus = List.of(ShowStatus.END_SHOWING);
    if (showLimitedMovies && movies.size() > 0) {
      movies = movies.stream()
          .filter(m -> !unlistedStatus.contains(m.getShowStatus()))
          .collect(Collectors.toList());
    }
    return movies;
  }

  /**
   * Gets movie menu.
   *
   * @return the movie menu
   */
//+ getMovieMenu(showLimited : boolean):LinkedHashMap<String, Runnable>
  public LinkedHashMap<String, Runnable> getMovieMenu() {
    return this.getMovieMenu(null);
  }

  /**
   * Update review menu.
   *
   * @param addMovieRunnable the add movie runnable
   */
  public void updateReviewMenu(Runnable addMovieRunnable) {
    this.refreshMenu(this.getMovieMenu(addMovieRunnable));
  }

  /**
   * Gets movie menu.
   *
   * @param addMovieRunnable the add movie runnable
   * @return the movie menu
   */
//+ getMovieMenu(addMovieRunnable : Runnable):LinkedHashMap<String, Runnable>
  public LinkedHashMap<String, Runnable> getMovieMenu(Runnable addMovieRunnable) {
    LinkedHashMap<String, Runnable> menuMap = new LinkedHashMap<String, Runnable>();
    List<Movie> movies = this.getViewableMovies();
    Helper.logger("MovieMenu.getMovieMenu", "SHOW LIMITED: " + showLimitedMovies);
    Helper.logger("MovieMenu.getMovieMenu", "SHOW REVIEWS: " + showReviews);
    Helper.logger("MovieMenu.getMovieMenu", "MOVIES: " + movies.size());
    Helper.logger("MovieMenu.getMovieMenu", "RUNNABLE: " + (addMovieRunnable != null));

    for (int i = 0; i < movies.size(); i++) {
      Movie movie = movies.get(i);
      int movieIdx = handler.getMovieIdx(movie.getId());
      menuMap.put((i + 1) + ". " + movie.getTitle(), () -> {
        handler.setSelectedMovieIdx(movieIdx);
        handler.printMovieDetails(movieIdx, false);

        if (showReviews) {
          if (addMovieRunnable != null) addMovieRunnable.run();
          else this.selectReviewOptions(handler.getSelectedMovie().getId(), "", "");
        }
      });
    }
    menuMap.put((menuMap.size() + 1) + ". Return to previous menu", () -> System.out.println("\t>>> " + "Returning to previous menu..."));
    return menuMap;
  }

  /**
   * Select review options boolean.
   *
   * @param movieId      the movie id
   * @param reviewerName the reviewer name
   * @param reviewerId   the reviewer id
   * @return the boolean
   */
//+ selectReviewOptions():boolean
  public boolean selectReviewOptions(int movieId, String reviewerName, String reviewerId) {
    boolean status = false;
    boolean canAddReview = !reviewerName.isEmpty() && !reviewerId.isEmpty();
    String reviewId = handler.checkIfAlreadyReviewed(reviewerId, movieId);
    Helper.logger("MovieMenu.selectReviewOptions", "canAddReview: " + canAddReview);

    List<String> proceedOptions = new ArrayList<String>() {
      {
        add("View all reviews");
        add("Add review");
        add("Return to previous menu");
      }
    };

    if (!canAddReview) proceedOptions.remove(1);
    else if (reviewId != null) proceedOptions.set(1, "Update review");


    while (!status) {
      System.out.println("Next steps:");
      this.displayMenuList(proceedOptions);
      int proceedSelection = getListSelectionIdx(proceedOptions, false);


      // Return to previous menu
      if (proceedSelection >= proceedOptions.size() - 1) {
        System.out.println("\t>>> " + "Returning to previous menu...");
        return status;
      }

      // View all reviews
      else if (proceedSelection == 0) {
        List<Review> movieReviews = handler.getMovieReviews(movieId);
        if (movieReviews.size() < 1) {
          colorPrint("No reviews available", Preset.WARNING);
          continue;
        }

        for (Review review : movieReviews) {
          colorPrint("\nReview #" + review.getId(), Preset.HIGHLIGHT);
          System.out.println(review);
        }

        // Automatically revert to previous menu
        if (!canAddReview) status = true;
      }

      // Add Review
      else if (canAddReview && proceedSelection == 1) {
        int reviewIdx = -1;

        // Update Review
        if (reviewId != null) {
          reviewIdx = handler.getReviewIdx(reviewId);
          if (reviewIdx < 0) {
            reviewId = null;
            continue;
          }

          handler.setSelectedReviewIdx(reviewIdx);
          System.out.println(handler.getSelectedReview());
          status = this.selectUpdatableAction(reviewId);
          break;
        }

        int rating = -1;
        String review = null;
        System.out.println("Add Movie Review");

        while (reviewIdx < 0) {
          scanner = new Scanner(System.in).useDelimiter("\n");

          try {
            // Prompt for rating [1-5]
            while (rating < 0) {
              System.out.print("Rating: ");

              if (scanner.hasNext()) {
                String input = scanner.nextLine();
                rating = Helper.parseStrToInt(input);

                // VALIDATION: Verify valid rating
                if (rating < 1 || rating > 5) {
                  colorPrint("Invalid rating value, rate from 1 – 5 [best].", Preset.ERROR);
                  rating = -1;
                  continue;
                }
              }
            }

            // Prompt for review
            System.out.print("Review: ");
            if (review == null) {
              review = scanner.next().trim();
            }

            // Add to reviews
            reviewIdx = handler.addReview(movieId, review, rating, reviewerName, reviewerId);
            if (reviewIdx < 0) throw new Exception("Unable to add review");

            colorPrint("Successfully added review", Preset.SUCCESS);

            // Flush excess scanner buffer
            scanner = new Scanner(System.in);

            status = true;
          } catch (Exception e) {
            colorPrint(e.getMessage(), Preset.ERROR);
            review = null;
            rating = -1;
          }
        }
      }
    }

    return status;
  }

  /**
   * Select review idx int.
   *
   * @param reviews the reviews
   * @return the int
   */
//+ selectReviewIdx():int
  public int selectReviewIdx(List<Review> reviews) {
    int reviewIdx = -1;
    if (reviews.size() < 1) {
      colorPrint("No reviews available . . .", Preset.WARNING);
      return reviewIdx;
    }

    List<String> reviewList = reviews.stream().map(r -> {
      int mIdx = handler.getMovieIdx(r.getMovieId());
      Movie m = handler.getMovie(mIdx);
      return m.getTitle();
    }).collect(Collectors.toList());
    reviewList.add((reviewList.size()), "Return to previous menu");

    System.out.println("Next steps:");
    this.displayMenuList(reviewList);
    int listSelectionIdx = getListSelectionIdx(reviewList, false);

    // Return to previous menu
    if (listSelectionIdx == (reviewList.size() - 1)) {
      System.out.println("\t>>> " + "Returning to previous menu...");
      return reviewIdx;
    }

    Review review = reviews.get(listSelectionIdx);
    reviewIdx = handler.getReviewIdx(review.getId());
    if (reviewIdx < 0) return reviewIdx;

    handler.setSelectedReviewIdx(reviewIdx);
    Helper.logger("MovieMenu.selectReviewIdx", "Selected Review Idx: " + reviewIdx);

    return reviewIdx;
  }

  /**
   * Select movie idx int.
   *
   * @return the int
   */
//+ selectMovieIdx():int
  public int selectMovieIdx() {
    showReviews = false;
    this.refreshMenu(this.getMovieMenu());

    // Retrieve user selection idx for movies
    List<Movie> movies = this.getViewableMovies();
    List<String> movieOptions = movies.stream()
        .map(Movie::getTitle)
        .collect(Collectors.toList());
    movieOptions.add("Return to previous menu");
    this.displayMenuList(movieOptions);
    int selectedIdx = this.getListSelectionIdx(movieOptions, false);

    Helper.logger("MovieMenu.selectMovieIdx", "Max: " + (this.menuMap.size()));
    Helper.logger("MovieMenu.selectMovieIdx", "Selected: " + selectedIdx);

    // Display movie details upon selection
    this.menuMap.get(this.menuMap.keySet().toArray()[selectedIdx]).run();
    this.awaitContinue();

    // Return to previous menu
    if (selectedIdx == (this.menuMap.size() - 1)) return -1;

    Movie movie = movies.get(selectedIdx);
    int movieIdx = handler.getMovieIdx(movie.getId());
    handler.setSelectedMovieIdx(movieIdx);
    Helper.logger("MovieMenu.selectMovieIdx", "Selected Movie Idx: " + handler.getMovie(movieIdx).getId());

    // Revert to menu with review
    showReviews = true;
    this.refreshMenu(this.getMovieMenu());

    return movieIdx;
  }

  /**
   * Select updatable action boolean.
   *
   * @param reviewId the review id
   * @return the boolean
   */
  public boolean selectUpdatableAction(String reviewId) {
    boolean status = false;

    Review review = handler.getReview(reviewId);
    if (review == null) return status;

    List<String> proceedOptions = new ArrayList<String>() {
      {
        add("Set rating");
        add("Set review");
        add("Discard changes");
        add("Remove review");
        add("Save changes & return");
        add("Return to previous menu");
      }
    };

    while (!status) {
      System.out.println("Next steps:");
      this.displayMenuList(proceedOptions);
      int proceedSelection = getListSelectionIdx(proceedOptions, false);

      // Save changes & return OR Return to previous menu
      if (proceedSelection >= proceedOptions.size() - 3) {
        // Save changes
        if (proceedSelection == proceedOptions.size() - 2) {
          Helper.logger("MovieMenu.selectUpdatableAction", "Saved Review: \n" + review);
          status = handler.updateReview(review.getRating(), review.getReview());
          colorPrint("Review updated", Preset.SUCCESS);
        }
        // Remove movie
        else if (proceedSelection == proceedOptions.size() - 3) {
          colorPrint("Review removed", Preset.SUCCESS);
          status = handler.removeReview(reviewId);
        }

        System.out.println("\t>>> " + "Returning to previous menu...");
        return status;
      }

      // Discard changes
      else if (proceedSelection == proceedOptions.size() - 4) {
        colorPrint("Changes discarded", Preset.WARNING);
        review = handler.getReview(reviewId);
        System.out.println(review);
      }

      // Set rating
      else if (proceedSelection == 0) {
        int prevStatus = review.getRating();
        colorPrint("Rating: " + prevStatus + " / " + MAX_RATING, Preset.CURRENT);

        //TODO: Extract as separate function
        scanner = new Scanner(System.in).useDelimiter("\n");

        // Prompt for rating [1-5]
        int curStatus = -1;
        while (curStatus < 0) {
          System.out.print("Set to:");
          if (scanner.hasNext()) {
            String input = scanner.nextLine();
            curStatus = Helper.parseStrToInt(input);

            // VALIDATION: Verify valid rating
            if (curStatus < MIN_RATING || curStatus > MAX_RATING) {
              colorPrint("Invalid rating value, rate from " + MIN_RATING + " – " + MAX_RATING + " [best].", Preset.ERROR);
              curStatus = -1;
              continue;
            }

            review.setRating(curStatus);
          }
        }

        this.printChanges("Rating: ", (prevStatus == curStatus), Integer.toString(prevStatus), Integer.toString(curStatus));
      }

      // Set review
      else if (proceedSelection == 1) {
        String prevStatus = review.getReview();
        colorPrint("Review: " + prevStatus, Preset.CURRENT);

        //TODO: Extract as separate function
        scanner = new Scanner(System.in).useDelimiter("\n");

        System.out.print("Set to:");
        String curStatus = scanner.next().trim();
        review.setReview(curStatus);

        this.printChanges("Review: ", prevStatus.equals(curStatus), prevStatus, curStatus);
      }

    }

    return status;
  }

  /**
   * Sets blockbuster status.
   *
   * @return the blockbuster status
   */
  public boolean setBlockbusterStatus() {
    List<String> updateOptions = new ArrayList<String>() {
      {
        add("Blockbuster");
        add("Non-Blockbuster");
      }
    };

    System.out.println("Set to:");
    this.displayMenuList(updateOptions);
    int selectionIdx = getListSelectionIdx(updateOptions, false);

    return (selectionIdx == 0);
  }

  /**
   * Sets show status.
   *
   * @return the show status
   */
  public ShowStatus setShowStatus() {
    List<ShowStatus> showStatuses = new ArrayList<ShowStatus>(EnumSet.allOf(ShowStatus.class));
    List<String> updateOptions = Stream.of(ShowStatus.values()).map(Enum::toString).collect(Collectors.toList());

    System.out.println("Set to:");
    this.displayMenuList(updateOptions);
    int selectionIdx = getListSelectionIdx(updateOptions, false);

    return showStatuses.get(selectionIdx);
  }

  /**
   * Sets content rating.
   *
   * @return the content rating
   */
  public ContentRating setContentRating() {
    List<String> updateOptions = Stream.of(ContentRating.values()).map(Enum::name).collect(Collectors.toList());
    System.out.println("Set to:");
    this.displayMenuList(updateOptions);
    int selectionIdx = getListSelectionIdx(updateOptions, false);

    List<ContentRating> contentRatings = new ArrayList<ContentRating>(EnumSet.allOf(ContentRating.class));

    return contentRatings.get(selectionIdx);
  }

  /**
   * Sets cast list.
   *
   * @return the cast list
   */
  public List<String> setCastList() {
    List<String> castList = new ArrayList<String>();

    int selectionIdx = 0;
    List<String> updateOptions = Arrays.asList(
        "Add another cast member",
        "Confirm cast list"
    );
    while (selectionIdx == 0 || castList.size() < 1) {
      scanner = new Scanner(System.in).useDelimiter("\n");
      String cast = this.setString("Cast #" + (castList.size() + 1) + ": ", "Cast name cannot be empty");
      castList.add(cast);

      // Request proceed option
      this.displayMenuList(updateOptions);
      selectionIdx = getListSelectionIdx(updateOptions, false);
    }

    return castList;
  }


  /**
   * Select editable action boolean.
   *
   * @param movieIdx the movie idx
   * @return the boolean
   */
//+ editMovie():int
  public boolean editMovie(int movieIdx) {
    boolean status = false;

    Movie movie = handler.getMovie(movieIdx);
    if (movie == null) return status;

    List<String> proceedOptions = new ArrayList<String>() {
      {
        add("Set title");
        add("Set synopsis");
        add("Set blockbuster status");
        add("Set showing status");
        add("Set content rating");
        add("Discard changes");
        add("Remove movie");
        add("Save changes & return");
        add("Return to previous menu");
      }
    };

    while (!status) {
      System.out.println("Next steps:");
      this.displayMenuList(proceedOptions);
      int proceedSelection = getListSelectionIdx(proceedOptions, false);

      // Save changes & return OR Return to previous menu
      if (proceedSelection >= proceedOptions.size() - 3) {
        // Save changes
        if (proceedSelection == proceedOptions.size() - 2) {
          handler.updateMovie(movie.getTitle(), movie.getSynopsis(), movie.getDirector(), movie.getCastList(), movie.getRuntime(), movie.getReleaseDate(), movie.isBlockbuster(), movie.getShowStatus(), movie.getContentRating(), movie.getOverallRating());
          status = true;
        }
        // Remove movie
        else if (proceedSelection == proceedOptions.size() - 3) {
          colorPrint("Movie removed", Preset.SUCCESS);
          status = handler.removeMovie(movieIdx);
        }

        System.out.println("\t>>> " + "Returning to previous menu...");
        return status;
      }

      // Discard changes
      else if (proceedSelection == proceedOptions.size() - 4) {
        colorPrint("Changes discarded", Preset.WARNING);
        movie = handler.getMovie(movieIdx);

        awaitContinue();
      }

      // Set title
      else if (proceedSelection == 0) {
        String prevStatus = movie.getTitle();
        colorPrint("Title: " + prevStatus, Preset.CURRENT);

        System.out.print("Set to: ");
        String title = scanner.next().trim();

        movie.setTitle(title);
        String curStatus = movie.getTitle();

        // Display changes
        this.printChanges("Title", prevStatus.equals(curStatus), prevStatus, curStatus);
      }

      // Set synopsis
      else if (proceedSelection == 1) {
        String prevStatus = movie.getSynopsis();
        colorPrint("Synopsis: " + prevStatus, Preset.CURRENT);

        System.out.print("Set to: ");
        String Synopsis = scanner.next().trim();

        movie.setSynopsis(Synopsis);
        String curStatus = movie.getSynopsis();

        // Display changes
        this.printChanges("Synopsis", prevStatus.equals(curStatus), prevStatus, curStatus);
      }

      // Set blockbuster status
      else if (proceedSelection == 2) {
        String prevStatus = (movie.isBlockbuster() ? "Blockbuster" : "Non-Blockbuster");
        colorPrint("Blockbuster Status: " + prevStatus, Preset.CURRENT);

        boolean isBlockbuster = this.setBlockbusterStatus();

        movie.setBlockbuster(isBlockbuster);
        String curStatus = (movie.isBlockbuster() ? "Blockbuster" : "Non-Blockbuster");

        // Display changes
        this.printChanges("Blockbuster Status", prevStatus.equals(curStatus), prevStatus, curStatus);
      }

      // Set showing status
      else if (proceedSelection == 3) {
        ShowStatus prevStatus = movie.getShowStatus();
        colorPrint("Showing Status: ", Preset.CURRENT);

        ShowStatus showStatus = this.setShowStatus();

        movie.setShowStatus(showStatus);
        ShowStatus curStatus = movie.getShowStatus();

        // Display changes
        this.printChanges("Showing Status", prevStatus.equals(curStatus), prevStatus.toString(), curStatus.toString());
      }

      // Set content rating
      else if (proceedSelection == 4) {
        ContentRating prevStatus = movie.getContentRating();
        colorPrint("Current Rating: ", Preset.CURRENT);

        ContentRating contentRating = this.setContentRating();

        movie.setContentRating(contentRating);
        ContentRating curStatus = movie.getContentRating();

        // Display changes
        this.printChanges("Current Rating", prevStatus.equals(curStatus), prevStatus.toString(), curStatus.toString());
      }
    }

    return status;
  }

  /**
   * Create movie int.
   *
   * @return the int
   */
  public int createMovie() {
    int movieIdx = -1;

    while (movieIdx < 0) {
      scanner = new Scanner(System.in).useDelimiter("\n");
      String title = this.setString("Title: ", "Title cannot be empty");

      String synopsis = this.setString("Synopsis: ", "Synopsis cannot be empty");

      System.out.print("[Blockbuster Status] ");
      boolean isBlockbuster = this.setBlockbusterStatus();
      System.out.println("Blockbuster Status: " + (isBlockbuster ? "Blockbuster" : "Non-Blockbuster"));

      System.out.print("[Show Status] ");
      ShowStatus showStatus = this.setShowStatus();
      System.out.println("Show Status: " + showStatus);

      System.out.print("[Content Rating] ");
      ContentRating contentRating = this.setContentRating();
      System.out.println("Content Rating: " + contentRating);

      List<String> castList = this.setCastList();
      System.out.println("Cast List: " + Arrays.deepToString(castList.toArray()));

      int runtime = this.setInt("Runtime: ");

      System.out.print("Director: ");
      String director = scanner.next().trim();

      LocalDate releaseDate = this.setDate("Release Date (dd-MM-yyyy): ", false);

      // Generate random 6 digit
      int id = Helper.generateDigits(6);

      // Overall rating defaulted to 0
      int overallRating = 0;

      // Add to movies
      movieIdx = handler.addMovie(id, title, synopsis, director, castList, runtime, releaseDate, isBlockbuster, showStatus, contentRating, overallRating);
      if (movieIdx < 0) colorPrint("[FAILED] Unable to add movie", Preset.ERROR);
      else colorPrint("Added new movie", Preset.SUCCESS);

      // Display movie
      handler.printMovieDetails(movieIdx, false);
    }

    return movieIdx;
  }
}
