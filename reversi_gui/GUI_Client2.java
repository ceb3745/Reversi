package reversi_gui;

import javafx.application.Application;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import reversi.ReversiException;
import reversi2.Board;
import reversi2.NetworkClient;
import javafx.scene.image.Image ;

import javax.swing.*;

import static java.lang.Integer.parseInt;

/**
 * This application is the UI for Reversi.
 * Creates a GUI of BorderPane, GridPane, Hbox and Buttons.
 * @author Carson Bloomingdale
 */
public class GUI_Client2 extends Application implements Observer{
    private Label lblTitle = new Label("Reversi");
    private Region r1 = new Region();
    private Region r2 = new Region();
    private Label lblStatus = new Label("Game Running");
    private Label lblCount = new Label("cool");
    private Label lblTurn = new Label("running");
    private Board model;
    private Stage stage;


    /**
     * Connection to network interface to server
     */
    private NetworkClient serverConn;

    /**
     * Where the command line parameters will be stored once the application
     * is launched.
     */
    private Map< String, String > params = null;

    /**
     * Look up a named command line parameter (format "--name=value")
     * @param name the string after the "--"
     * @return the value after the "="
     * @throws ReversiException if name not found on command line
     */
    private String getParamNamed( String name ) throws ReversiException {
        if ( params == null ) {
            params = super.getParameters().getNamed();
        }
        if ( !params.containsKey( name ) ) {
            throw new ReversiException(
                    "Parameter '--" + name + "=xxx' missing."
            );
        }
        else {
            return params.get( name );
        }
    }
    public void init() {
        try{
            String host = getParamNamed("host");
            Integer port = parseInt(getParamNamed("port"));
            this.model = new Board();
            this.serverConn = new NetworkClient( host, port, this.model );
            this.model.initializeGame();
            this.model.addObserver( this );
        } catch (ReversiException |
                ArrayIndexOutOfBoundsException |
                NumberFormatException e){
            System.out.println( e );
            throw new RuntimeException( e );
        }

    }

    @Override
    public void start( Stage primaryStage ) {


        GridPane pane = new GridPane(); // Creates new GridePane
        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 8; j++){
                Button b1 = new Button();
                ButtonData d1 = new ButtonData(i,j);

                if(model.getContents(i,j )== Board.Move.PLAYER_ONE){
                    Image p1 = new Image(getClass().getResourceAsStream("othelloP1.jpg"));
                    b1.setGraphic(new ImageView(p1));
                    b1.setDisable(true);
                }
                else if (model.getContents(i,j)== Board.Move.PLAYER_TWO){
                    Image p2 = new Image(getClass().getResourceAsStream("othelloP2.jpg"));
                    b1.setGraphic(new ImageView(p2));
                    b1.setDisable(true);
                }
                else{
                    Image empty = new Image(getClass().getResourceAsStream("empty.jpg"));
                    b1.setGraphic(new ImageView(empty));
                    if (model.isValidMove(d1.getRow(),d1.getColumn())== false){
                        b1.setDisable(true);
                    }
                }
                b1.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
                    @Override
                    public void handle(javafx.event.ActionEvent event) {
                        if(model.isMyTurn() && model.isValidMove(d1.getRow(),d1.getColumn())){
                            serverConn.sendMove(d1.getRow(),d1.getColumn());
                        }
                    }
                });
                pane.add(b1,i,j);
            }
        }
        BorderPane borderPane = new BorderPane(); //Creates new BorderPane
        borderPane.setTop(lblTitle);
        borderPane.setCenter(pane);
        HBox hella = new HBox();
        HBox.setHgrow(r1, Priority.ALWAYS); //Creates regions to align inside hbox
        HBox.setHgrow(r2, Priority.ALWAYS);
        hella.getChildren().addAll(lblCount,r1,lblStatus,r2,lblTurn);
        borderPane.setBottom(hella);

        Scene scene = new Scene(borderPane, 670, 650); //new scene
        primaryStage.setScene(scene);
        primaryStage.setTitle("Reversi Client");
        primaryStage.show();

        if ( !this.model.isMyTurn() ) { //Runs the operations of the game
            lblCount.setText( Integer.toString(this.model.getMovesLeft()) + " moves left." );
            Board.Status status = this.model.getStatus();
            switch ( status ) {//switched based on the status given from the model at the time.
                case NOT_OVER:
                    if (model.isMyTurn()){
                        lblStatus.setText("MAKE MOVE");
                    }
                    else{
                        lblStatus.setText("WAITING");
                    }
                    break;
                case ERROR:
                    lblStatus.setText("ERROR");
                    break;
                case I_WON:
                    lblStatus.setText("YOU WIN");
                    break;
                case I_LOST:
                    lblStatus.setText("YOU LOSE");
                    break;
                case TIE:
                    lblStatus.setText("TIE GAME");
                    break;
                default:
                    break;
            }
        }
        else {
            lblStatus.setText("MAKE MOVE"); //Makes a Move

        }

        this.stage = primaryStage;

    }


    /**
     * Launch the JavaFX GUI.
     *
     * @param args not used, here, but named arguments are passed to the GUI.
     *             <code>--host=<i>hostname</i> --port=<i>portnum</i></code>
     */
    public static void main( String[] args ) {// runs the main application
        Application.launch( GUI_Client2.class, args );
    }

    @Override
    public void update(Observable t, Object o) { //updates
        assert t == this.model: "Update from non-model Observable";
        javafx.application.Platform.runLater(this::refresh);
    }

    public synchronized void refresh() //refreshes
    {
        this.start(this.stage);
    }


    public class ButtonData{//creates a class alongside button that stores the row and column #
        public int row;
        public int column;

        public ButtonData(int rowselected, int columnselected){
            this.row = rowselected;
            this.column = columnselected;
        }

        public int getRow() {
            return row;
        }

        public int getColumn() {
            return column;
        }

    }

}
