package boundaries;

import control.handlers.BookingHandler;
import entities.Booking;
import entities.Cinema;
import entities.Movie;
import entities.Movie.ShowStatus;
import entities.Showtime;
import utils.Helper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static utils.Helper.colorizer;
import static utils.LocalDateTimeDeserializer.dateTimeFormatter;

public class BookingMenu extends Menu {
  private static BookingHandler handler;
  private static BookingMenu instance;

  private BookingMenu() {
    super();
    handler = new BookingHandler();
    this.refreshMenu(this.getShowtimeMenu(handler.getShowtimes()));
  }

  public static BookingMenu getInstance() {
    if (instance == null) instance = new BookingMenu();
    return instance;
  }

  /**
   * Get booking handler
   *
   * @return bookingHandler:BookingHandler
   */
  public static BookingHandler getHandler() {
    return handler;
  }

  @Override
  public void showMenu() {
    this.displayMenu();
  }

  /**
   * Get the updated showtime list to be displayed
   *
   * @return menuMap:LinkedHashMap<String, Runnable>
   */
  //+ getShowtimeMenu(showtimes:List<Showtime>):LinkedHashMap<String, Runnable>
  public LinkedHashMap<String, Runnable> getShowtimeMenu(List<Showtime> showtimes) {
    LinkedHashMap<String, Runnable> menuMap = new LinkedHashMap<String, Runnable>();
    if (showtimes.size() < 1) {
      System.out.println("No showtimes available.");
    } else {
      for (int i = 0; i < showtimes.size(); i++) {
        Showtime showtime = showtimes.get(i);
        int showtimeIdx = i;
        menuMap.put((i + 1) + ". " + showtime.toString(), () -> {
          handler.setSelectedShowtimeIdx(showtimeIdx);
          handler.printShowtimeDetails(showtimeIdx);
        });
      }
    }
    menuMap.put((menuMap.size() + 1) + ". Return to previous menu", () -> System.out.println("\t>>> " + "Returning to previous menu..."));
    return menuMap;
  }

  /**
   * Get the updated booking list to be displayed
   *
   * @return menuMap:LinkedHashMap<String, Runnable>
   */
  //+ getBookingMenu(customerId:String):LinkedHashMap<String, Runnable>
  public LinkedHashMap<String, Runnable> getBookingMenu(String customerId) {
    LinkedHashMap<String, Runnable> menuMap = new LinkedHashMap<String, Runnable>();
    List<Booking> bookings = handler.getBookings(customerId);
    if (bookings.size() < 1) {
      System.out.println("No bookings available.");
    } else {
      for (int i = 0; i < bookings.size(); i++) {
        Booking booking = bookings.get(i);
        int bookingIdx = i;
        menuMap.put((i + 1) + ". " + booking.getTransactionId(), () -> {
          handler.setSelectedBookingIdx(bookingIdx);
          handler.printBooking(booking.getTransactionId());
        });
      }
    }
    menuMap.put((menuMap.size() + 1) + ". Return to previous menu", () -> System.out.println("\t>>> " + "Returning to previous menu..."));
    return menuMap;
  }

  /**
   * Get the updated cinema list to be displayed
   *
   * @return menuMap:LinkedHashMap<String, Runnable>
   */
  //+ getCinemaMenu():LinkedHashMap<String, Runnable>
  public LinkedHashMap<String, Runnable> getCinemaMenu() {
    LinkedHashMap<String, Runnable> menuMap = new LinkedHashMap<String, Runnable>();
    List<Cinema> cinemas = handler.getCinemas();
    if (cinemas.size() < 1) {
      System.out.println("No cinemas available.");
    } else {
      for (int i = 0; i < cinemas.size(); i++) {
        Cinema cinema = cinemas.get(i);
        menuMap.put((i + 1) + ". " + cinema.getClassType(), () -> {
          handler.setSelectedCinemaId(cinema.getId());
          System.out.println(cinema);
//          this.editCinema(cinema.getId());
        });
      }
    }
    menuMap.put((menuMap.size() + 1) + ". Add new cinema", this::registerCinema);
    menuMap.put((menuMap.size() + 1) + ". Return to previous menu", () -> System.out.println("\t>>> " + "Returning to previous menu..."));
    return menuMap;
  }

  /**
   * Retrieves the user booking selection idx with specified customer id
   *
   * @param customerId:String
   * @return selectedBookingIdx:int
   */
  //+ selectBookingIdx(customerId:String):int
  public int selectBookingIdx(String customerId) {
    this.refreshMenu(this.getBookingMenu(customerId));

    this.displayMenu();
    return -1;
  }

  /**
   * Retrieves the user cinema selection id / idx
   *
   * @return selectedCinemaIdx:int
   */
  //+ selectCinemaIdx():int
  public int selectCinemaIdx() {
//    this.refreshMenu(this.getCinemaMenu());
//    this.showMenu();

    // Initialize options with a return at the end
    List<Cinema> cinemas = handler.getCinemas();
    List<String> cinemaOptions = cinemas.stream()
        .map(Cinema::toString)
        .collect(Collectors.toList());
    cinemaOptions.add((cinemaOptions.size()), "Add new cinema");
    cinemaOptions.add((cinemaOptions.size()), "Return to previous menu");

    // Display options and get selection input
    this.displayMenuList(cinemaOptions);
    int selectedIdx = this.getListSelectionIdx(cinemaOptions, false);

    // Return to previous menu
    if (selectedIdx == (cinemaOptions.size() - 1)) {
      System.out.println("\t>>> " + "Returning to previous menu...");
      return -1;
    }

    // Add new cinema
    else if (selectedIdx == (cinemaOptions.size() - 2)) {
      selectedIdx = this.registerCinema();
    }


    // Display selected
    Cinema cinema = cinemas.get(selectedIdx);
    System.out.println(cinema);

    return selectedIdx;
  }

  /**
   * Retrieves the user showtime selection idx with specified movie id
   *
   * @param showtimes:List<Showtime>
   * @return selectedShowtimeIdx:int
   */
  //+ selectShowtimeIdx(showtimes:List<Showtime>):int
  public int selectShowtimeIdx(List<Showtime> showtimes) {
    this.refreshMenu(this.getShowtimeMenu(showtimes));

    // Initialize options with a return at the end
    List<String> showtimeOptions = showtimes.stream()
        .map((s) -> handler.printedShowtime(s.getId()))
        .collect(Collectors.toList());
    showtimeOptions.add((showtimeOptions.size()), "Return to previous menu");

    // Display options and get selection input
    this.displayMenuList(showtimeOptions);
    int selectedIdx = this.getListSelectionIdx(showtimes, false);

    // Return to previous menu
    if (selectedIdx == (showtimeOptions.size() - 1)) {
      System.out.println("\t>>> " + "Returning to previous menu...");
      return -1;
    }

    // Retrieve showtime idx from showtime id
    Showtime showtime = showtimes.get(selectedIdx);
    int showtimeIdx = handler.getShowtimeIdx(showtime.getId());
    // Store selection idx
    handler.setSelectedShowtimeIdx(showtimeIdx);

    return showtimeIdx;
  }

  public Booking.TicketType selectTicket(List<String> ticketOptions) {

    int selectionIdx = -1;
    while (selectionIdx < 0) {
      System.out.println("Select ticket type:");
      this.displayMenuList(ticketOptions);
      selectionIdx = getListSelectionIdx(ticketOptions, false);
    }

    // Retrieve ticket type
    Booking.TicketType selectedTicket = Booking.TicketType.values()[selectionIdx];
    return selectedTicket;
  }

  /**
   * Retrieves the user list of seat selection idx with specified showtime idx
   *
   * @param showtimeIdx:int
   * @return seats:List<int[]>
   */
  //+ selectSeat(showtimeIdx:int):List<int[]>
  public List<int[]> selectSeat(int showtimeIdx) {
    List<String> confirmationOptions = new ArrayList<String>() {{
      add("Continue selecting more seats");
      add("Confirm booking");
      add("Discard selection");
      add("Return to previous menu");
    }};

    boolean[][] showtimeSeats = handler.getShowtime(showtimeIdx).getSeats();
    List<int[]> selectedSeats = new ArrayList<int[]>();

    int confirmationSelection = 0;
    while (confirmationSelection != confirmationOptions.size()) {

      // Seat selection
      switch (confirmationSelection) {
        case 0 -> {
          int[] selectedSeat = this.seatSelection(showtimeIdx);

          // VALIDATION: Check if seat was previously selected
          if (!showtimeSeats[selectedSeat[0]][selectedSeat[1]]) {
            System.out.println("Seat is already selected. Try another");
            continue;
          }

          selectedSeats.add(selectedSeat);

          // Sudo seat assignment
          handler.assignSeat(showtimeSeats, selectedSeat, true);
          handler.printSeats(showtimeSeats);
        }


        // Selection Confirmation
        case 1 -> {
          // Finalize the seat selection
          System.out.println("Confirmed Seat Selection");
          handler.printSeats(showtimeSeats);
          return selectedSeats;
        }


        // Discard Selection, Return without saving
        default -> {
          handler.bulkAssignSeat(showtimeIdx, selectedSeats, false);
          showtimeSeats = handler.getShowtime(showtimeIdx).getSeats();
          selectedSeats = new ArrayList<int[]>();

          // Return to previous menu
          if (confirmationSelection == confirmationOptions.size() - 1) return selectedSeats;
        }
      }

      System.out.println("Next steps:");
      this.displayMenuList(confirmationOptions);
      confirmationSelection = getListSelectionIdx(confirmationOptions, false);

      Helper.logger("BookingMenu.confirmationSelection", "Max: " + (confirmationOptions.size() - 1));
      Helper.logger("BookingMenu.confirmationSelection", "Selected: " + confirmationSelection);

    }

    return selectedSeats;
  }

  /**
   * Retrieves the user seat selection idx with specified showtime idx
   *
   * @param showtimeIdx:int
   * @return seat:int[]
   */
  //+ seatSelection(showtimeIdx:int):int[]
  public int[] seatSelection(int showtimeIdx) {
    boolean[][] seats = handler.getShowtime(showtimeIdx).getSeats();
    handler.printSeats(seats);

    int[] seatCode = new int[2];

    List<Integer> rowRange = IntStream.rangeClosed(0, seats.length).boxed().toList();
    List<Integer> colRange = IntStream.rangeClosed(0, seats[0].length).boxed().toList();

    System.out.println("Enter the seat row: ");
    seatCode[0] = this.getListSelectionIdx(rowRange, false);

    System.out.println("Enter the seat column: ");
    seatCode[1] = this.getListSelectionIdx(colRange, false);

    return seatCode;
  }

  /**
   * Update showtime details of specified showtime idx
   *
   * @param showtimeId:String
   * @return status = true
   */
  //+ editShowtime(showtimeId:int) : boolean
  public boolean editShowtime(String showtimeId) {
    boolean status = false;

    Showtime showtime = handler.getShowtime(showtimeId);
    if (showtime == null) return status;

    int showtimeIdx = handler.getShowtimeIdx(showtimeId);
    handler.printShowtimeDetails(showtimeIdx);

    //TODO: Update with ShowType
    List<String> proceedOptions = new ArrayList<String>() {
      {
        add("Set Cinema ID");
        add("Set Movie ID");
        add("Set Datetime");
        add("Set Show Type");
        add("Discard changes");
        add("Remove showtime");
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
          handler.updateShowtime(
              showtime.getCinemaId(),
              showtime.getMovieId(),
              showtime.getType(),
              showtime.getDatetime(),
              showtime.getSeats()
          );
          status = true;
          System.out.println(colorizer("[UPDATED] Showtime updated", Helper.Preset.SUCCESS));
        }
        // Remove movie
        else if (proceedSelection == proceedOptions.size() - 3) {
          // VALIDATION: Check if showtime has associated bookings
          if (handler.checkIfShowtimeHasBooking(showtime.getId())) {
            System.out.println(colorizer("[FAILED] Unable to remove showtime with associated bookings", Helper.Preset.ERROR));
            continue;
          }

          System.out.println(colorizer("[UPDATED] Showtime removed", Helper.Preset.SUCCESS));
          handler.removeShowtime(showtime.getId());
        }

        System.out.println("\t>>> " + "Returning to previous menu...");
        return status;
      }

      // Discard changes
      else if (proceedSelection == proceedOptions.size() - 4) {
        System.out.println("[REVERTED] Changes discarded");
        showtime = handler.getShowtime(showtimeIdx);
        System.out.println(showtime);
      }

      // Set Cinema ID
      else if (proceedSelection == 0) {
        int prevStatus = showtime.getCinemaId();
        System.out.println("[CURRENT] Cinema ID: " + prevStatus);

        //TODO: Extract as separate function
        List<String> updateOptions = handler.getCinemas().stream()
            .map(Cinema::toString)
            .collect(Collectors.toList());

        System.out.println("Set to:");
        this.displayMenuList(updateOptions);
        int selectionIdx = getListSelectionIdx(updateOptions, false);

        if (handler.checkClashingShowtime(selectionIdx, showtime.getDatetime())) {
          System.out.println("[NO CHANGE] Cinema already has a showing at the given datetime");
          continue;
        }
        showtime.setCinemaId(selectionIdx);
        int curStatus = showtime.getCinemaId();

        if (prevStatus == curStatus) {
          System.out.println("[NO CHANGE] Cinema ID: " + prevStatus);
        } else {
          System.out.println("[UPDATED] Cinema ID: " + prevStatus + " -> " + curStatus);
        }
      }

      // Set Movie ID
      else if (proceedSelection == 1) {
        int prevStatus = showtime.getMovieId();
        System.out.println("[CURRENT] Movie ID: " + prevStatus);

        //TODO: Extract as separate function
        List<Movie> movies = MovieMenu.getHandler().getMovies(ShowStatus.NOW_SHOWING);
        List<String> updateOptions = movies.stream()
            .map(Movie::getTitle)
            .collect(Collectors.toList());

        System.out.println("Set to:");
        this.displayMenuList(updateOptions);
        int selectionIdx = getListSelectionIdx(updateOptions, false);

        // VALIDATION: Check if showtime has associated bookings
        if (handler.checkIfShowtimeHasBooking(showtime.getId())) {
          System.out.println("[NO CHANGE] Unable to change movie ID of showtime with associated bookings");
          continue;
        }

        showtime.setMovieId(movies.get(selectionIdx).getId());
        int curStatus = showtime.getMovieId();

        if (prevStatus == curStatus) {
          System.out.println("[NO CHANGE] Movie ID: " + prevStatus);
        } else {
          System.out.println("[UPDATED] Movie ID: " + prevStatus + " -> " + curStatus);
        }
      }

      // Set Datetime
      else if (proceedSelection == 2) {
        LocalDateTime prevStatus = showtime.getDatetime();
        System.out.println("[CURRENT] Datetime: " + prevStatus.format(dateTimeFormatter));

        //TODO: Extract as separate function
        scanner = new Scanner(System.in).useDelimiter("\n");
        System.out.print("Set to (dd-MM-yyyy hh:mm[AM/PM]):");
        String datetime = scanner.next().trim();
        if (datetime.matches("^\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}[AP]M$")) {
          LocalDateTime showDatetime = LocalDateTime.parse(datetime, dateTimeFormatter);

          if (handler.checkClashingShowtime(showtimeIdx, showDatetime)) {
            System.out.println("[NO CHANGE] Cinema already has a showing at the given datetime");
          } else {
            showtime.setDatetime(showDatetime);
            if (prevStatus.isEqual(showDatetime)) {
              System.out.println("[NO CHANGE] Datetime: " + prevStatus.format(dateTimeFormatter));
            } else {
              System.out.println("[UPDATED] Datetime: " + prevStatus.format(dateTimeFormatter) + " -> " + showDatetime.format(dateTimeFormatter));
            }
          }
        } else {
          System.out.println("Invalid input, expected format (dd-MM-yyyy hh:mma)");
        }
      }

      // Set Show Type
      else if (proceedSelection == 3) {
        Showtime.ShowType prevStatus = showtime.getType();
        System.out.println("[CURRENT] Show Type: " + prevStatus.toString());

        //TODO: Extract as separate function
        List<String> updateOptions = Stream.of(Showtime.ShowType.values())
            .map(Showtime.ShowType::toString)
            .collect(Collectors.toList());

        System.out.println("Set to:");
        this.displayMenuList(updateOptions);
        int selectionIdx = getListSelectionIdx(updateOptions, false);

        // VALIDATION: Check if showtime has associated bookings
        if (handler.checkIfShowtimeHasBooking(showtime.getId())) {
          System.out.println("[NO CHANGE] Unable to change movie ID of showtime with associated bookings");
          continue;
        }

        showtime.setType(Showtime.ShowType.values()[selectionIdx]);
        Showtime.ShowType curStatus = showtime.getType();

        if (prevStatus == curStatus) {
          System.out.println("[NO CHANGE] Show Type: " + prevStatus);
        } else {
          System.out.println("[UPDATED] Show Type: " + prevStatus + " -> " + curStatus);
        }

      }
    }
    return status;
  }

  /**
   * Update cinema details of specified cinema id
   *
   * @param cinemaId:int
   * @return status = true
   */
  //+ editCinema(cinemaId:int) : boolean
  public boolean editCinema(int cinemaId) {
    boolean status = false;

    Cinema cinema = handler.getCinema(cinemaId);
    List<Showtime> cinemaShowtimes = handler.getCinemaShowtimes(cinemaId);
    cinema.setShowtimes(cinemaShowtimes);
    Helper.logger("BookingMenu.editCinema", "Cinema: " + cinema);
    Helper.logger("BookingMenu.editCinema", "Cinema Showtimes: " + cinemaShowtimes);
    if (cinema == null) return status;

    List<String> proceedOptions = new ArrayList<String>() {
      {
        add("Set Class");
        add("Set Showtimes");
        add("Discard changes");
        add("Remove cinema");
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
          handler.updateCinema(
              cinema.getClassType(),
              cinema.getShowtimes(),
              cinema.getCineplexCode()
          );
          status = true;
        }
        // Remove movie
        else if (proceedSelection == proceedOptions.size() - 3) {
          // VALIDATION: Check if cinema has associated bookings
          if (handler.checkIfCinemaHasBooking(cinema.getId())) {
            System.out.println("Unable to remove cinema with associated bookings");
            continue;
          }

          System.out.println("[UPDATED] Cinema removed");
          handler.removeCinema(cinema.getId());
        }

        System.out.println("\t>>> " + "Returning to previous menu...");
        return status;
      }

      // Discard changes
      else if (proceedSelection == proceedOptions.size() - 4) {
        System.out.println("[REVERTED] Changes discarded");
        cinema = handler.getCinema(cinemaId);
        cinemaShowtimes = handler.getCinemaShowtimes(cinemaId);
        cinema.setShowtimes(cinemaShowtimes);
      }

      // Set Class Type
      else if (proceedSelection == 0) {
        Cinema.ClassType prevStatus = cinema.getClassType();
        System.out.println("[CURRENT] Class: " + prevStatus);

        List<Cinema.ClassType> classTypes = new ArrayList<Cinema.ClassType>(EnumSet.allOf(Cinema.ClassType.class));
        List<String> typeOptions = Stream.of(Cinema.ClassType.values())
            .map(Enum::toString)
            .collect(Collectors.toList());

        System.out.println("Set to:");
        this.displayMenuList(typeOptions);
        int selectionIdx = getListSelectionIdx(typeOptions, false);

        cinema.setClassType(classTypes.get(selectionIdx));
        Cinema.ClassType curStatus = cinema.getClassType();

        if (prevStatus == curStatus) {
          System.out.println("[NO CHANGE] Class: " + prevStatus);
        } else {
          System.out.println("[UPDATED] Class: " + prevStatus + " -> " + curStatus);
        }
      }

      // Set Showtimes
      else if (proceedSelection == 1) {
        List<Showtime> showtimes = cinema.getShowtimes();
        int showtimeIdx = this.selectShowtimeIdx(showtimes);
        while (showtimeIdx >= 0) {
          Showtime showtime = handler.getShowtime(showtimeIdx);
          this.editShowtime(showtime.getId());

//          // Print updated showtime
//          System.out.println(showtime);

          // Refresh showtimes and display options
          showtimes = handler.getCinemaShowtimes(cinemaId);
          showtimeIdx = this.selectShowtimeIdx(showtimes);
        }

      }

      // Print updated cinema
      System.out.println(cinema);

    }

    return status;
  }

  /**
   * Register cinema
   *
   * @return cinemaId:int
   */
  //+ registerCinema():int
  public int registerCinema() {
    int cinemaId = -1;

    System.out.println("Cinema Registration");
    while (cinemaId == -1 && scanner.hasNextLine()) {
      try {
        scanner = new Scanner(System.in).useDelimiter("\n");

        List<Cinema.ClassType> classTypes = new ArrayList<Cinema.ClassType>(EnumSet.allOf(Cinema.ClassType.class));
        List<String> typeOptions = Stream.of(Cinema.ClassType.values())
            .map(Enum::toString)
            .collect(Collectors.toList());
        typeOptions.add("Return to previous menu");

        System.out.println("Class type:");
        this.displayMenuList(typeOptions);
        int typeSelection = getListSelectionIdx(typeOptions, false);

        // Prompt for Cineplex Code
        String cineplexCode = null;
        while (cineplexCode == null) {
          System.out.println("Cineplex Code (i.e, XYZ):");
          String inputCineplexCode = scanner.next();

          // VALIDATION: Check if it's exactly 3 characters
          if (!inputCineplexCode.matches("^([A-Z-0-9]{3})\\b")) continue;

          cineplexCode = inputCineplexCode;
        }

        // Return to previous menu
        if (typeSelection == typeOptions.size() - 1) {
          System.out.println("\t>>> " + "Returning to previous menu...");
          return cinemaId;
        }
        Cinema.ClassType classType = classTypes.get(typeSelection);
        cinemaId = handler.addCinema(classType, new ArrayList<Showtime>(), cineplexCode);
        this.refreshMenu(this.getCinemaMenu());

        System.out.println("Successful cinema registration");
        // Flush excess scanner buffer
        scanner = new Scanner(System.in);
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
    }

    return cinemaId;
  }

}
