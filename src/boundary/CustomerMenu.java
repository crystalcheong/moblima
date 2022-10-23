package boundary;

import control.BookingHandler;
import control.CustomerHandler;
import control.MovieHandler;
import entity.Booking;
import entity.Customer;
import entity.Menu;
import entity.Showtime;
import tmdb.entities.Movie;
import utils.Helper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;

public class CustomerMenu extends Menu {
  private CustomerHandler handler;
  private MovieHandler movieHandler;
  private BookingHandler bookingHandler;

  private MovieMenu movieMenu;
  private BookingMenu bookingMenu;

  private static CustomerMenu instance;

  private CustomerMenu() {
    super();
    this.movieMenu = MovieMenu.getInstance();
    this.bookingMenu = BookingMenu.getInstance();

    this.handler = new CustomerHandler();
    this.movieHandler = MovieMenu.getHandler();
    this.bookingHandler = BookingMenu.getHandler();

    this.menuMap = new LinkedHashMap<String, Runnable>() {{
      put("Search/List Movies", movieHandler::printMovies);
      put("View movie details – including reviews and ratings", movieMenu::showMenu);
//      put("Check seat availability and selection of seat/s.", () -> {
//
//      });
      put("Book and purchase ticket", () -> {
        makeBooking();
      });
      put("View booking history", () -> {
        viewBookings();

      });
//      put("List the Top 5 ranking by ticket sales OR by overall reviewers’ ratings", () -> {
//      });
      put("Exit", () -> {
        System.out.println("\t>>> Quitting application...");
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("Thank you for using MOBLIMA. We hope to see you again soon!");
        System.exit(0);
      });
    }};
  }

  public static CustomerMenu getInstance() {
    if (instance == null) instance = new CustomerMenu();
    return instance;
  }

  public CustomerHandler getHandler() {
    return this.handler;
  }

  @Override
  public void showMenu() {
    this.displayMenu();
  }

  public int getCurrentCustomer() {
    int customerIdx = -1;

    Customer customer = this.handler.getCurrentCustomer();
    if(customer != null){
      customerIdx = this.handler.getCustomerIdx(customer.getId());
      this.handler.setCurrentCustomer(customerIdx);

      return customerIdx;
    }

    List<String> accountOptions = new ArrayList<String>() {{
      add("Already have an account? Login");
      add("Create account");
//      add("Discard selection");
    }};

    while (customerIdx == -1) {
      System.out.println("Next steps:");
      this.displayMenuList(accountOptions);
      int accountSelection = getListSelectionIdx(accountOptions, false);

      // Login account
      if (accountSelection == 0) {
        customerIdx = this.login();
      }

      // Create account
      if (accountSelection == 1) {
        customerIdx = this.register();
      }
    }

    this.handler.setCurrentCustomer(customerIdx);
    return customerIdx;
  }

  public int register() {
    int customerIdx = -1;

    System.out.println("Account Registration");
    while (customerIdx == -1 && scanner.hasNextLine()) {
      try {
        scanner = new Scanner(System.in).useDelimiter("\n");

        String name = null, contactNumber = null;

        System.out.print("Name: ");
        if (scanner.hasNext()) {
          name = scanner.next();
        }

        System.out.print("Contact No.: ");
        if (scanner.hasNextLong()) {
          Long phoneNumber = scanner.nextLong();
          contactNumber = Long.toString(phoneNumber);
        }

        if (name == null || contactNumber == null) {
          System.out.println("Invalid input, try again.");
          continue;
        }

        // Initialize and append to existing customer list
        customerIdx = handler.addCustomer(name, contactNumber);
        if (customerIdx == -1) throw new Exception("Unable to register account");

        System.out.println("Successful account registration");
//        scanner = new Scanner(System.in);
      } catch (Exception e) {
        System.out.println(e.getMessage());

        List<String> proceedOptions = new ArrayList<String>() {{
          add("Proceed with registration");
          add("Login with account instead");
          add("Return to previous menu");
        }};

        System.out.println("Next steps:");
        this.displayMenuList(proceedOptions);
        int proceedSelection = getListSelectionIdx(proceedOptions, false);

        // Return to previous menu
        if (proceedSelection == proceedOptions.size() - 1) return -1;
        else if (proceedSelection == 1) this.login();
        else continue;
      }
    }

    return customerIdx;
  }

  public int login() {
    int customerIdx = -1;

    System.out.println("Account Login");
    while (customerIdx == -1 && scanner.hasNextLine()) {
      try {
        scanner = new Scanner(System.in).useDelimiter("\n");
        ;
        System.out.print("Account Contact No.: ");
        String contactNumber = null;
        if (scanner.hasNextLong()) {
          Long phoneNumber = scanner.nextLong();
          contactNumber = Long.toString(phoneNumber);
        }

        if (contactNumber == null) {
          System.out.println("Invalid input, try again.");
          continue;
        }
        customerIdx = this.handler.checkIfAccountExists(contactNumber);
        if (customerIdx == -1) throw new Exception("Invalid login credentials, unable to authenticate");

        System.out.println("Successful account login");
//        scanner = new Scanner(System.in);
      } catch (Exception e) {
        System.out.println(e.getMessage());

        List<String> proceedOptions = new ArrayList<String>() {{
          add("Proceed with login");
          add("Register account instead");
          add("Return to previous menu");
        }};

        System.out.println("Next steps:");
        this.displayMenuList(proceedOptions);
        int proceedSelection = getListSelectionIdx(proceedOptions, false);

        // Return to previous menu
        if (proceedSelection == proceedOptions.size() - 1) return -1;
        else if (proceedSelection == 1) this.register();
        else continue;

      }
    }

    return customerIdx;
  }

  public int makeBooking() {
    int bookingIdx = -1;

    // Select movie
    System.out.println("Select movie: ");
    int movieIdx = this.movieMenu.selectMovieIdx();
    if (movieIdx < 0) return bookingIdx;
    Movie selectedMovie = this.movieHandler.getMovie(movieIdx);

    // Select showtimes for selected movie
    System.out.println("Select showtime slot: ");
    int showtimeIdx = this.bookingMenu.selectShowtimeIdx(selectedMovie.getId());
    if (showtimeIdx < 0) return bookingIdx;
    Showtime showtime = this.bookingHandler.getShowtime(showtimeIdx);

    // Print showtime details
    bookingHandler.printShowtimeDetails(showtimeIdx);

    // Select seats
    List<int[]> seats = this.bookingMenu.selectSeat(showtimeIdx);
    Helper.logger("CustomerMenu.makeBooking", "NO. OF SEATS: " + seats.size());
    if (seats.size() < 1) return bookingIdx;

    // Get customer idx via login/register
    int customerIdx = this.getCurrentCustomer();
    Helper.logger("CustomerMenu.makeBooking", "customerIdx: " + customerIdx);
    if (customerIdx < 0) return bookingIdx;

    Customer customer = this.handler.getCustomer(customerIdx);
    if (customer == null) return bookingIdx;

    bookingIdx = this.bookingHandler.addBooking(customer.getId(), showtime.getCinemaId(), showtime.getMovieId(), showtime.getId(), seats, 10.0, Booking.TicketType.PEAK);

    // Print out tx id
    Booking booking = bookingHandler.getBooking(bookingIdx);
    Helper.logger("CustomerMenu.makeBooking", "booking tx: " + booking.getTransactionId());
    if (booking == null) return bookingIdx;
    System.out.println("Successfully booked. Reference: " + booking.getTransactionId());

    return bookingIdx;
  }

  public void viewBookings() {
    // Get customer idx via login/register
    int customerIdx = this.getCurrentCustomer();
    Helper.logger("CustomerMenu.viewBookings", "customerIdx: " + customerIdx);
    if (customerIdx < 0) return;

    Customer customer = this.handler.getCustomer(customerIdx);
    if (customer == null) return;
    Helper.logger("CustomerMenu.viewBookings", "Customer ID: " + customer.getId());
//    this.bookingHandler.printBookings(customer.getId());
    this.bookingMenu.selectBookingIdx(customer.getId());
  }
}