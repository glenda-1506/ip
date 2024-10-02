package lovespiritual;

import lovespiritual.exception.lovespiritualException;
import lovespiritual.task.Deadline;
import lovespiritual.task.Event;
import lovespiritual.task.Task;
import lovespiritual.task.Todo;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Main class for task manager application.
 * Handles user input, task management, and file storage.
 */
public class lovespiritual {
    public static final String SEPARATOR = "_".repeat(30);
    private static ArrayList<Task> tasks = new ArrayList<>();
    private Storage storage;
    private TaskList taskList;
    private UI ui;

    /**
     * Constructor initialises task manager with the specified file path.
     * Loads tasks from storage and displays the welcome screen.
     *
     * @param filePath Path to the file where the list of tasks are stored.
     */
    public lovespiritual(String filePath) {
        ui = new UI();
        storage = new Storage(filePath);
        taskList = new TaskList();
        storage.loadTasks(tasks);
        ui.printWelcomeMessage();
    }

    /**
     * Main loop that runs the task manager, the processing of user input.
     * Commands include adding, deleting, marking, and listing tasks.
     */
    public void run() {
        Scanner in = new Scanner(System.in);
        while (true) {
            String input = in.nextLine().trim();

            try {
                String command = Parser.parseCommand(input);

                switch (command) {
                case "bye":
                    storage.saveTasks(tasks);
                    ui.printExitMessage();
                    return;
                case "list":
                    ui.printList(tasks);
                    break;
                case "mark":
                    markTask(input, tasks);
                    storage.saveTasks(tasks);
                    break;
                case "unmark":
                    unmarkTask(input, tasks);
                    storage.saveTasks(tasks);
                    break;
                case "todo":
                    todo(input, tasks);
                    storage.saveTasks(tasks);
                    break;
                case "deadline":
                    deadline(input, tasks);
                    storage.saveTasks(tasks);
                    break;
                case "event":
                    event(input, tasks);
                    storage.saveTasks(tasks);
                    break;
                case "delete":
                    taskList.deleteTask(input, tasks);
                    storage.saveTasks(tasks);
                    break;
                case "find":
                    find(input);
                default:
                    throw new lovespiritualException("(^_^) Let's get started with a command!");
                }
            } catch (lovespiritualException e) {
                ui.printError(e.getMessage());
            } catch (Exception e) {
                ui.printUnexpectedError();
            }
        }
    }

    /**
     * Start of the running of application.
     * Starts task manager with the file path for storing tasks.
     *
     * @param args Command-line arguments which are not used.
     */
    public static void main(String[] args) {
        new lovespiritual("data/lovespiritual.txt").run();
    }

    /**
     * Find tasks from the list based on the word provided in the input.
     * All tasks that has the word provided in it would be printed out.
     *
     * @param input User input of word to find.
     * @throws lovespiritualException If the input is missing the word to find.
     */
    private static void find(String input) throws lovespiritualException {
        String findTask = input.substring("find".length()).trim();
        if (findTask.isEmpty()) {
            throw new lovespiritualException("Oops! (・_・;) What should I find? Please give me a keyword.");
        }
        System.out.println(SEPARATOR);
        System.out.println("Here are the matching tasks in your list:");
        int matchCount = 0;
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (task.toString().toLowerCase().contains(findTask.toLowerCase())) {
                matchCount++;
                System.out.println((i + 1) + "." + task);
            }
        }
        if (matchCount == 0) {
            System.out.println("No tasks found with the keyword: " + findTask);
        }
        System.out.println(SEPARATOR);
    }

    /**
     * Adds a new event task to the task list.
     * Ensures that the task description includes both 'from' and 'to' times.
     *
     * @param input User input string for adding an event.
     * @param tasks List of tasks to which the new event will be added.
     * @throws lovespiritualException If the input is formatted incorrectly or missing required details.
     */
    private static void event(String input, ArrayList <Task> tasks) throws lovespiritualException {
        String fullTaskDescription = input.substring("event".length()).trim();
        if (fullTaskDescription.isEmpty()) {
            throw new lovespiritualException("Uh-oh! (・_・;) Your event description seems to be missing!");
        }
        if (!fullTaskDescription.contains("from")) {
            throw new lovespiritualException("Hmmm (・_・) Your event is missing the 'from' time! Please add it.");
        }
        if (!fullTaskDescription.contains("to")) {
            throw new lovespiritualException("Oops! (•‿•) The 'to' part is missing! Let's add it.");
        }
        String taskDescription;
        String from;
        String to;
        String[] taskDetails = fullTaskDescription.split("from ");
        if (taskDetails[0].trim().isEmpty()) {
            throw new lovespiritualException("Yikes! (⊙_⊙;) You forgot to tell me what the event is about!");
        }
        taskDescription = taskDetails[0].trim();
        String[] time = taskDetails[1].split("to ");
        if (time.length < 2 || time[0].trim().isEmpty()) {
            throw new lovespiritualException("Start date/time? (。_。) We can't go without it!");
        }
        if (time[1].trim().isEmpty()) {
            throw new lovespiritualException("The end date/time is missing (･o･;) When does this event wrap up?");
        }
        from = time[0].trim();
        to = time[1].trim();
        tasks.add(new Event(taskDescription, from, to));
        System.out.println(SEPARATOR);
        System.out.println("Yay! (•‿•) I've added your task!");
        System.out.println(tasks.get(tasks.size() - 1));
        System.out.println("Woot! (^▽^) You now have " + tasks.size() + " tasks in your list!");
        System.out.println(SEPARATOR);
    }

    /**
     * Adds a new deadline task to the task list.
     * Ensures that the task description includes a 'by' time for the deadline.
     *
     * @param input User input string for adding a deadline.
     * @param tasks List of tasks to which the new deadline will be added.
     * @throws lovespiritualException If the input is formatted incorrectly or missing required details.
     */
    private static void deadline(String input, ArrayList <Task> tasks) throws lovespiritualException {
        String fullTaskDescription = input.substring("deadline".length()).trim();
        if (fullTaskDescription.isEmpty()) {
            throw new lovespiritualException("Oops! (｡•́︿•̀｡) Your deadline needs a little description!");
        }
        if (!fullTaskDescription.contains("by")) {
            throw new lovespiritualException("The 'by' is missing! (・_・;) When's it due?");
        }
        String taskDescription;
        String by;
        String[] taskDetails = fullTaskDescription.split("by", 2);
        if (taskDetails.length < 2 || taskDetails[0].trim().isEmpty()) {
            throw new lovespiritualException("Hmm... (・_・;) Don’t forget to tell me what this deadline is about!");
        }
        if (taskDetails[1].trim().isEmpty()) {
            throw new lovespiritualException("Uh-oh! (・へ・) I need to know the deadline date or time.");
        }
        taskDescription = taskDetails[0].trim();
        by = taskDetails[1].trim();
        tasks.add(new Deadline(taskDescription, by));
        System.out.println(SEPARATOR);
        System.out.println("Yippee! (★^O^★) Task added successfully!");
        System.out.println(tasks.get(tasks.size() - 1));
        System.out.println("Wow! (｡♥‿♥｡) You now have " + tasks.size() + " tasks! Keep going!");
        System.out.println(SEPARATOR);
    }

    /**
     * Adds a new todo task to the task list.
     *
     * @param input User input string for adding a todo.
     * @param tasks List of tasks to which the new todo will be added.
     * @throws lovespiritualException If the input is missing the task description.
     */
    private static void todo(String input, ArrayList <Task> tasks) throws lovespiritualException {
        String taskDescription = input.substring("todo".length()).trim();
        if (taskDescription.isEmpty()) {
            throw new lovespiritualException("Hmm... (¬‿¬) What's the todo? Looks like the description's missing!");
        }
        tasks.add(new Todo(taskDescription));
        System.out.println(SEPARATOR);
        System.out.println("Woohoo! (＾▽＾) Your task is safely added!");
        System.out.println(" [T][ ] " + taskDescription);
        System.out.println("Amazing! (•̀ᴗ•́) You’ve got " + tasks.size() + " tasks lined up!");
        System.out.println(SEPARATOR);
    }

    /**
     * Unmarks a task as not completed.
     *
     * @param input User input string for unmarking a task.
     * @param tasks List of tasks.
     * @throws lovespiritualException If the task number is invalid or out of range.
     */
    private static void unmarkTask(String input, ArrayList <Task> tasks) throws lovespiritualException {
        String taskNumber = input.substring("unmark".length()).trim();
        if (taskNumber.isEmpty()) {
            throw new lovespiritualException("Oopsie! (⊙_⊙) Please give me a valid number!");
        }
        int indexNumber;
        try {
            indexNumber = Integer.parseInt(taskNumber) - 1;
        } catch (NumberFormatException e) {
            throw new lovespiritualException("Hmm, that's not a number! (・_・;) Try again, please!");
        }
        if (indexNumber >= 0 && indexNumber < tasks.size()) {
            tasks.get(indexNumber).unmark();
            System.out.println(SEPARATOR);
            System.out.println("Got it! (◠‿◠) This task isn't done yet!");
            System.out.println(tasks.get(indexNumber));
            System.out.println(SEPARATOR);
        } else {
            throw new lovespiritualException("Yikes! (≧Д≦) That number doesn't look right. Can you double-check it?");
        }
    }

    /**
     * Marks a task as completed.
     *
     * @param input User input string for marking a task.
     * @param tasks List of tasks.
     * @throws lovespiritualException If the task number is invalid or out of range.
     */
    private static void markTask(String input, ArrayList <Task> tasks) throws lovespiritualException {
        String taskNumber = input.substring("mark".length()).trim();
        if (taskNumber.isEmpty()) {
            throw new lovespiritualException("Hmm... (ʘ‿ʘ) A valid number, please?");
        }
        int indexNumber;
        try {
            indexNumber = Integer.parseInt(taskNumber) - 1;
        } catch (NumberFormatException e) {
            throw new lovespiritualException("Whoa there! (O.O) That’s not a number! Can you double-check?");
        }
        if (indexNumber >= 0 && indexNumber < tasks.size()) {
            tasks.get(indexNumber).mark();
            System.out.println(SEPARATOR);
            System.out.println("Yay! (^_^) This task is all done!");
            System.out.println(tasks.get(indexNumber));
            System.out.println(SEPARATOR);
        } else {
            throw new lovespiritualException("Hmm... (°ヘ°) That number seems a bit off. Try again?");
        }
    }
}
