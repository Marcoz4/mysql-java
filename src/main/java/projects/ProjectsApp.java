package projects;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import projects.entity.Project;
import projects.exception.DbException;
import projects.service.ProjectService;

public class ProjectsApp {
	private Scanner scanner = new Scanner(System.in);
//A3. Imported Scanner to get input from console	
	private ProjectService projectService = new ProjectService();
//B1 created projectService class in the ProjectService.java	
           
//A1.Cleanup. Deleted the debugging line (DbConnection.getConnection();) in the main method.  Removed the import statement: import projects.dao.DbConnection;
    
// @formatter:off
    private List<String> operations = List.of(
      "1) Add a project"
    );
// @formatter:on
//A2.Created a list to hold the variables.  List of is to create immutable list. 
//Will print on console to remind what selection was picked.
    
 
    public static void main(String[] args) { 
        new ProjectsApp().processUserSelections();  
    }
//A4. Created a method that processes the menu.


	private void processUserSelections() {
//A5.Eclipse created a instance method.
//This method displays the menu selections, gets a selection from the user, and then acts on the selection.	
	  boolean done = false;
	  
	  while (!done) {
		try {
		  int selection = getUserSelection();
		  
		  switch(selection) {
		  case -1:
			done = exitMenu();
			break;
			
		  case 1:
		    createProject();
		    break;
		    
		  default:
		    System.out.println("\n" + selection + " is not valid selection. Try again.");
		    break;
		} 
	  }
	  catch(Exception e) {
		  System.out.println("\nError: " + e + " Try again.");
	  }
	 }
	}
//A6. Created a loop until the variable done is true
//A12 Created a method exit menu for that case and for the other case another method was created	
//A13 test code	
	
	private void createProject() {
//B2 create a method to gather project details from user
		String projectName = getStringInput("Enter the project name");
	    BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours");
	    BigDecimal actualHours = getDecimalInput("Enter the actual hours");
	    Integer difficulty = getIntInput("Enter the project difficulty (1-5)");
	    String notes = getStringInput("Enter the project notes");	
	    
	    Project project = new Project();

	    project.setProjectName(projectName);
	    project.setEstimatedHours(estimatedHours);
	    project.setActualHours(actualHours);
	    project.setDifficulty(difficulty);
	    project.setNotes(notes);

	    Project dbProject = projectService.addProject(project);
	    System.out.println("You have successfully created project: " + dbProject);
	}
//B3 Get all the info i need from the user. We initialize a new project object.
//We set values to the project object
// call the method in the ProjectService.java. Print a successful message.
	
	  private BigDecimal getDecimalInput(String prompt) {
		    String input = getStringInput(prompt);

		    if(Objects.isNull(input)) {
		      return null;
		    }

		    try {
		      return new BigDecimal(input).setScale(2);
		    }
		    catch(NumberFormatException e) {
		      throw new DbException(input + " is not a valid decimal number.");
		    }
		  }
//B4 Show the prompt message and read the user input as String
//if null returns null. try to convert input String to BigDecimal. If not error	  
	
	private boolean exitMenu() {
	  System.out.println("Exiting the menu.");
		return true;
	}

	private int getUserSelection() {
	  printOperations();
	  
	  Integer input = getIntInput("Enter a menu selection");
	  
	  return Objects.isNull(input) ? -1 : input;
	}
//A7.Created a method to get users input as an integer
//Method getIntInput will return user's menu selection.
//If the value is -1 it will exit the application
	            
private Integer getIntInput(String prompt) {
  String input = getStringInput(prompt); 
  
  if(Objects.isNull(input)) {
	 return null;
  }
  
  try {
	 return Integer.valueOf(input); 
  }
  catch(NumberFormatException e) {
	throw new DbException(input + " is not a valid number.");
  }
} 
//A10 created a method to get integer input from user
//calls getStringInput to get input as a string. Null if input is null.
//convert input to integer, if fails throws exception.

private String getStringInput(String prompt) {
    System.out.print(prompt + ": "); 
    String input = scanner.nextLine(); 
    
    return input.isBlank() ? null : input.trim();
  }
//A11 Created method to get string input from user
//keep cursor on the same line as the prompt. prints prompt for the user input
// Return null if input is blank, otherwise return trimmed input
	
	
private void printOperations() {
  System.out.println("\nThese are the available selections. Press the Enter key to quit:");
//A8 created a method printOperations to print to the console.
  
  operations.forEach(line -> System.out.println("  " + line));
//A9 used the lambda expression		
	} 
   }
