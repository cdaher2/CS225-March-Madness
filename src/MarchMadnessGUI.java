//package marchmadness;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 *  MarchMadnessGUI
 * 
 * this class contains the buttons the user interacts
 * with and controls the actions of other objects 
 *
 * @author Grant Osborn
 */
public class MarchMadnessGUI extends Application {
    
    
    //all the gui ellements
    private BorderPane root;
    private ToolBar toolBar;
    private ToolBar btoolBar;
    private Button simulate;
    private Button login;
    private Button scoreBoardButton;
    private Button viewBracketButton;
    private Button clearButton;
    private Button resetButton;
    private Button finalizeButton;
    private Button help;
    
    //allows you to navigate back to division selection screen
    private Button back;
  
    
    private  Bracket startingBracket;
    //reference to currently logged in bracket
    private Bracket selectedBracket;
    private Bracket simResultBracket;

    
    private ArrayList<Bracket> playerBrackets;
    private HashMap<String, Bracket> playerMap;

    

    private ScoreBoardTable scoreBoard;
    private TableView table;
    private BracketPane bracketPane;
    private GridPane loginP;
    private GridPane helpP;
    private TournamentInfo teamInfo;
    
    
    @Override
    public void start(Stage primaryStage) {
        //try to load all the files, if there is an error display it
        try{
            teamInfo=new TournamentInfo();
            startingBracket= new Bracket(teamInfo.loadStartingBracket());
            simResultBracket=new Bracket(teamInfo.loadStartingBracket());
        } catch (IOException ex) {
            showError(new Exception("Can't find "+ex.getMessage(),ex),true);
        }
        //deserialize stored brackets
        playerBrackets = loadBrackets();
        
        playerMap = new HashMap<>();
        addAllToMap();
        


        //the main layout container
        root = new BorderPane();
        scoreBoard= new ScoreBoardTable();
        login = new Button("Logout");
        table=scoreBoard.start();
        loginP=createLogin();
        helpP=createHelp();
        CreateToolBars();
        
        //display login screen
        login();
        
        setActions();
        root.setTop(toolBar);   
        root.setBottom(btoolBar);
        Scene scene = new Scene(root);
        primaryStage.setMaximized(true);

        primaryStage.setTitle("March Madness Bracket Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    
    
    /**
     * simulates the tournament  
     * simulation happens only once and
     * after the simulation no more users can login
     */
    private void simulate(){
        //cant login and restart prog after simulate
        //login.setDisable(true);
        simulate.setDisable(true);
        
       scoreBoardButton.setDisable(false);
       viewBracketButton.setDisable(false);
       
       teamInfo.simulate(simResultBracket);
       for(Bracket b:playerBrackets){
           scoreBoard.addPlayer(b,b.scoreBracket(simResultBracket));
       }
        
        displayPane(table);
    }
    
    /*
     * added by Paul MacAllister  4.5.18
     * displays a popup window with information on how to use the program
     */
    private void help(){
      displayPane(helpP);
      clearButton.setDisable(true);
      resetButton.setDisable(true);
      help.setDisable(true);
      finalizeButton.setDisable(true);
      back.setDisable(true);
    }
    
    /**
     * Displays the login screen
     * 
     */
    private void login(){            
        //login.setDisable(true);
        simulate.setDisable(true);
        scoreBoardButton.setDisable(true);
        viewBracketButton.setDisable(true);
        btoolBar.setDisable(true);
        displayPane(loginP);
    }
    
     /**
     * Displays the score board
     * 
     */
    private void scoreBoard(){
        displayPane(table);
    }
    
     /**
      * Displays Simulated Bracket
      * 
      */
    private void viewBracket(){
       selectedBracket=simResultBracket;
       bracketPane=new BracketPane(selectedBracket);
       GridPane full = bracketPane.getFullPane();
       full.setAlignment(Pos.CENTER);
       full.setDisable(true);
       displayPane(new ScrollPane(full)); 
    }
    
    /**
     * allows user to choose bracket
     * 
     */
   private void chooseBracket(){
        //login.setDisable(true);
        btoolBar.setDisable(false);
        bracketPane=new BracketPane(selectedBracket);
        displayPane(bracketPane);

    }
    /**
     * resets current selected sub tree
     * for final4 reset Ro2 and winner
     */
    private void clear(){

        if(bracketPane.getSubTree() == 7){ // 7 is the name for the "full" view
            selectedBracket=new Bracket(startingBracket); // full reset
            bracketPane=new BracketPane(selectedBracket);
            displayPane(bracketPane);
        }
        else{
                bracketPane.clear();
                bracketPane=new BracketPane(selectedBracket);
                displayPane(bracketPane);
        }
    }
    
    /**
     * resets entire bracket
     */
    private void reset(){
        if(confirmReset()){
            //horrible hack to reset
            selectedBracket=new Bracket(startingBracket);
            bracketPane=new BracketPane(selectedBracket);
            displayPane(bracketPane);
        }
    }
    
    private void finalizeBracket(){
       if(bracketPane.isComplete()){
    
           if(this.confirmFinalize())
           {
                   
               btoolBar.setDisable(true);
               bracketPane.setDisable(true);
               simulate.setDisable(false);
               login.setDisable(false);
               //save the bracket along with account info
               seralizeBracket(selectedBracket);
           }
            
       }else{
            infoAlert("You can only finalize a bracket once it has been completed.");
            //go back to bracket section selection screen
            // bracketPane=new BracketPane(selectedBracket);
            displayPane(bracketPane);
        
       }
       //bracketPane=new BracketPane(selectedBracket);
      
      
        
    }
    
    
    /**
     * displays element in the center of the screen
     * 
     * @param p must use a subclass of Pane for layout. 
     * to be properly center aligned in  the parent node
     */
    private void displayPane(Node p){
        root.setCenter(p);
        BorderPane.setAlignment(p,Pos.CENTER);
    }
    
    /**
     * Creates toolBar and buttons.
     * adds buttons to the toolbar and saves global references to them
     */
    private void CreateToolBars(){
        toolBar  = new ToolBar();
        btoolBar  = new ToolBar();
//        login=new Button("Login");
        simulate=new Button("Simulate");
        scoreBoardButton=new Button("ScoreBoard");
        viewBracketButton= new Button("View Simulated Bracket");
        clearButton =new Button("Clear");
        resetButton=new Button("Reset");
        finalizeButton=new Button("Finalize");
        help=new Button("HELP");
        toolBar.getItems().addAll(
                createSpacer(),
                login,
                simulate,
                scoreBoardButton,
                viewBracketButton,
                createSpacer()
        );
        btoolBar.getItems().addAll(
                createSpacer(),
                help,
                clearButton,
                resetButton,
                finalizeButton,
                back=new Button("Choose Division"),
                createSpacer()
        );
    }
    
   /**
    * sets the actions for each button
    */
    private void setActions(){
        help.setOnAction(e->help());
        login.setOnAction(e->login());
        simulate.setOnAction(e->simulate());
        scoreBoardButton.setOnAction(e->scoreBoard());
        viewBracketButton.setOnAction(e->viewBracket());
        clearButton.setOnAction(e-> clear());
        resetButton.setOnAction(e->reset());
        finalizeButton.setOnAction(e->finalizeBracket());
        back.setOnAction(e->{
            bracketPane=new BracketPane(selectedBracket);
            displayPane(bracketPane);
        });
    }
    
    /**
     * Creates a spacer for centering buttons in a ToolBar
     */
    private Pane createSpacer(){
        Pane spacer = new Pane();
        HBox.setHgrow(
                spacer,
                Priority.SOMETIMES
        );
        return spacer;
    }
    
        /*
     * added by Paul MacAllister 4/5/18
     * creates the pane which the tutorial shows up in
     */
    private GridPane createHelp(){
      GridPane helpPane = new GridPane();
      helpPane.setAlignment(Pos.CENTER);
      helpPane.setHgap(10);
      helpPane.setVgap(10);
      
      Text tutorial = new Text("this is where the tutorial goes");
      helpPane.add(tutorial,0,10);
      
      Button exitButton = new Button("Exit Tutorial");
      helpPane.add(exitButton,0,50);
      
      exitButton.setOnAction(e->{
        bracketPane=new BracketPane(selectedBracket);
        clearButton.setDisable(false);
        resetButton.setDisable(false);
        help.setDisable(false);
        finalizeButton.setDisable(false);
        back.setDisable(false);
        
        displayPane(bracketPane);
      });
      
      
      return helpPane;
    }

    
    private GridPane createLogin(){
        
        
        /*
        LoginPane
        Sergio and Joao
         */

        GridPane loginPane = new GridPane();
        loginPane.setAlignment(Pos.CENTER);
        loginPane.setHgap(10);
        loginPane.setVgap(10);
        loginPane.setPadding(new Insets(5, 5, 5, 5));

        //Paul MacAllister 4.5.18
        //slightly updated welcome message
        Text welcomeMessage = new Text("Welcome to March Maddness sim!\nCreate an account to begin");
        loginPane.add(welcomeMessage,0, 0);
        DropShadow ds = new DropShadow();
        ds.setOffsetY(3.0f);
        ds.setColor(Color.color(0.4f, 0.4f, 0.4f));
        welcomeMessage.setEffect(ds);
        welcomeMessage.setCache(true);
        welcomeMessage.setX(10.0f);
        welcomeMessage.setY(10.0f);
        welcomeMessage.setFill(Color.GREEN);
        welcomeMessage.setFont(Font.font(null, FontWeight.BOLD, 15));
        
        Label userName = new Label("User Name: ");
        loginPane.add(userName, 0, 1);

        TextField enterUser = new TextField();
        loginPane.add(enterUser, 1, 1);

        //Modded by Nikolas Brisbois
        //The text tells the user that the passwords needs to be <= 6 letters
        Label password = new Label("Password: \nmust be 6 letters or more");
        loginPane.add(password, 0, 2);

        PasswordField passwordField = new PasswordField();
        loginPane.add(passwordField, 1, 2);

        Button signButton = new Button("    Sign in    ");
        
        //added by Mike Moschella 4/7/2018. Button for solely creating a user
        Button createButton = new Button("Create User "); 
        loginPane.add(signButton, 1, 4);
        loginPane.add(createButton, 1, 5);
        signButton.setDefaultButton(true);//added by matt 5/7, lets you use sign in button by pressing enter

        Label message = new Label();
        loginPane.add(message, 1, 5);

        //modded by Mike Moschella 4/7/2018.  Split functionality between "Sign in" and new "Create User" button
        signButton.setOnAction(event -> {

            // the name user enter
            String name = enterUser.getText();
            // the password user enter
            String playerPass = passwordField.getText();

            //added by Ana Gorohovschi
            //prevent execution of follow up code if username is null
            //modded by Nikolas Brisbois
            if(name.length()==0)
            {
                infoAlert("Enter your username and password.");
                return;
            }
            
            
            
            if (playerMap.get(name) != null) {
                //check password of user
                 
                Bracket tmpBracket = this.playerMap.get(name);
               
                String password1 = tmpBracket.getPassword();

                if (Objects.equals(password1, playerPass)) {
                    // load bracket
                    selectedBracket=playerMap.get(name);
                    chooseBracket();
                    // Added by Vrej on April 5th, 2018 - Sets the logout to be usable again.
                    login.setDisable(false);
                }else{
                   infoAlert("The password you have entered is incorrect!");
                }

            } else {
                //modded by Mike Moschella, Sign in button no longer creates a user, functionality moved to "Create user"
                
                    infoAlert("No user with the Username \""  + name + "\" exists.");
                    // Added by Vrej on April 5th, 2018 - Sets the logout to be usable again.
                    login.setDisable(false);
                    
            }
        });
        
        //modded by Mike Moschella 4/7/2018.  User Creation has been moved here from "Sign in" button.
        createButton.setOnAction(event -> {

            // the name user enter
            String name = enterUser.getText();
            // the password user enter
            String playerPass = passwordField.getText();

            //added by Ana Gorohovschi
            //prevent execution of follow up code if username is null
            //modded by Nikolas Brisbois
            //prevents creation of account if the password is less then six words
            if(name.length()==0)
            {
                infoAlert("Enter your new username and password.");
                return;
            }
            else if(playerPass.length() < 6) {
              infoAlert("Password is too short.  "
                       +"\nIt must be 6 characters or longer.");
              return;
            }
            
            if (playerMap.get(name) != null) {
                //check password of user
                 
               infoAlert("That username is taken. "
                       + "\nPlease try again.");
              

            } else {
                //check for empty fields
                if(!name.equals("")&&!playerPass.equals("")){
                    //create new bracket
                    Bracket tmpPlayerBracket = new Bracket(startingBracket, name);
                    playerBrackets.add(tmpPlayerBracket);
                    tmpPlayerBracket.setPassword(playerPass);

                    playerMap.put(name, tmpPlayerBracket);
                    selectedBracket = tmpPlayerBracket;
                    //alert user that an account has been created
                    infoAlert("Your new account is \""  + name + "\". Welcome aboard!");
                    chooseBracket();
                }
            }
        });
        
        return loginPane;
    }
    
    /**
     * addAllToMap
     * adds all the brackets to the map for login
     */
    private void addAllToMap(){
        for(Bracket b:playerBrackets){
            playerMap.put(b.getPlayerName(), b);   
        }
    }
    
    /**
     * The Exception handler
     * Displays a error message to the user
     * and if the error is bad enough closes the program
     * @param fatal true if the program should exit. false otherwise
     */
    private void showError(Exception e,boolean fatal){
        String msg=e.getMessage();
        if(fatal){
            msg=msg+" \n\nthe program will now close";
            //e.printStackTrace();
        }
        Alert alert = new Alert(AlertType.ERROR,msg);
        alert.setResizable(true);
        alert.getDialogPane().setMinWidth(420);   
        alert.setTitle("Error");
        alert.setHeaderText("something went wrong");
        alert.showAndWait();
        if(fatal){ 
            System.exit(666);
        }   
    }
    
    /**
     * alerts user to the result of their actions in the login pane 
     * @param msg the message to be displayed to the user
     */
    private void infoAlert(String msg){
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("March Madness Bracket Simulator");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    /**
     * Prompts the user to confirm that they want
     * to clear all predictions from their bracket
     * @return true if the yes button clicked, false otherwise
     */
    private boolean confirmReset(){
        Alert alert = new Alert(AlertType.CONFIRMATION, 
                "Are you sure you want to reset the ENTIRE bracket?", 
                ButtonType.YES,  ButtonType.CANCEL);
        alert.setTitle("March Madness Bracket Simulator");
        alert.setHeaderText(null);
        alert.showAndWait();
        return alert.getResult()==ButtonType.YES;
    }
    
    /**
     * Prompts the user to confirm that they want
     * finalize their bracket
     * @return true if the yes button clicked, false otherwise
     */
    private boolean confirmFinalize(){
        Alert alert = new Alert(AlertType.CONFIRMATION, 
                "Are you sure you want to finalize your bracket?", 
                ButtonType.YES,  ButtonType.CANCEL);
        alert.setTitle("March Madness Bracket Simulator");
        alert.setHeaderText(null);
        alert.showAndWait();
        return alert.getResult()==ButtonType.YES;
    }
    
    
    /**
     * Tayon Watson 5/5
     * seralizedBracket
     * @param B The bracket the is going to be seralized
     */
    private void seralizeBracket(Bracket B){
        FileOutputStream outStream = null;
        ObjectOutputStream out = null;
    try 
    {
      outStream = new FileOutputStream(B.getPlayerName()+".ser");
      out = new ObjectOutputStream(outStream);
      out.writeObject(B);
      out.close();
    } 
    catch(IOException e)
    {
      // Grant osborn 5/6 hopefully this never happens 
      showError(new Exception("Error saving bracket \n"+e.getMessage(),e),false);
    }
    }
    /**
     * Tayon Watson 5/5
     * deseralizedBracket
     * @param filename of the seralized bracket file
     * @return deserialized bracket 
     */
    private Bracket deseralizeBracket(String filename){
        Bracket bracket = null;
        FileInputStream inStream = null;
        ObjectInputStream in = null;
    try 
    {
        inStream = new FileInputStream(filename);
        in = new ObjectInputStream(inStream);
        bracket = (Bracket) in.readObject();
        in.close();
    }catch (IOException | ClassNotFoundException e) {
      // Grant osborn 5/6 hopefully this never happens either
      showError(new Exception("Error loading bracket \n"+e.getMessage(),e),false);
    } 
    return bracket;
    }
    
      /**
     * Tayon Watson 5/5
     * deseralizedBracket
     * @return deserialized bracket
     */
    private ArrayList<Bracket> loadBrackets()
    {   
        ArrayList<Bracket> list=new ArrayList<Bracket>();
        File dir = new File(".");
        for (final File fileEntry : dir.listFiles()){
            String fileName = fileEntry.getName();
            String extension = fileName.substring(fileName.lastIndexOf(".")+1);
       
            if (extension.equals("ser")){
                list.add(deseralizeBracket(fileName));
            }
        }
        return list;
    }
       
}
