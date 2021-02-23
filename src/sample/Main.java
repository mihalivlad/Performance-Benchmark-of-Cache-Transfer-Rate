package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class Main extends Application {

    Button test = new Button("test");
    Button addNode = new Button("Add");
    Alert loadingDialog = new Alert(Alert.AlertType.CONFIRMATION);
    Hyperlink hyperlink = new Hyperlink("Basic information");
    Hyperlink hyperlink2 = new Hyperlink("Latency");
    Hyperlink hyperlink3 = new Hyperlink("Miss Ratio");
    Hyperlink hyperlink4 = new Hyperlink("AMAT");
    private Controller controller = new Controller();
    private int seconds;

    @Override
    public void start(Stage primaryStage) throws Exception{
        BorderPane border = new BorderPane();
        HBox hbox = addHBox();
        border.setTop(hbox);
        border.setLeft(addVBox());
        addStackPane(hbox);         // Add stack to HBox in top region

        border.setCenter(addGridPane());
       // border.setRight(addFlowPane());
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setMaximized(true);
        primaryStage.setScene(new Scene(border));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    public HBox addHBox() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #336699;");
        AtomicReference<Thread> thread = new AtomicReference<>(new Thread());
        AnimationTimer timer = new AnimationTimer() {
            private long lastTime = 0;
            @Override
            public void handle(long now) {
                if (lastTime != 0) {
                    if (now > lastTime + 1_000_000_000) {
                        seconds++;
                        loadingDialog.setContentText(Integer.toString(seconds) + " .s");
                        lastTime = now;
                        if(!thread.get().isAlive()){
                            stop();
                            loadingDialog.setHeaderText("Done");

                        }
                    }
                } else {
                    lastTime = now;

                }
            }

            @Override
            public void stop() {
                super.stop();
                lastTime = 0;
                seconds = 0;
            }
        };
        Button buttonScs1 = new Button("Basic memory info");
        buttonScs1.setOnAction(e->{
            loadingDialog.setHeaderText("Waiting...");
            loadingDialog.show();
            controller.setBenchmarkNr(1);
            thread.set(new Thread(controller));
            buttonAction(timer, thread.get());
            //loadingDialog.close();
        });
        Button buttonScs2 = new Button("Compute Latency Time");
        buttonScs2.setOnAction(e->{
            loadingDialog.setHeaderText("Waiting...(Average:5-15s)");
            loadingDialog.show();
            controller.setBenchmarkNr(2);
            thread.set(new Thread(controller));
            buttonAction(timer, thread.get());
            //loadingDialog.close();
        });

        Button buttonScs3 = new Button("Compute Miss Ratio");
        buttonScs3.setOnAction(e->{
            loadingDialog.setHeaderText("Waiting...(Average 10-15 minutes)");
            loadingDialog.show();
            controller.setBenchmarkNr(3);
            thread.set(new Thread(controller));
            buttonAction(timer, thread.get());
            //loadingDialog.close();
        });
        Button buttonScs4 = new Button("Compute AMAT");
        buttonScs4.setOnAction(e->{
            loadingDialog.setHeaderText("Waiting...(Average 10-15 minutes)");
            loadingDialog.show();
            controller.setBenchmarkNr(4);
            thread.set(new Thread(controller));
            buttonAction(timer, thread.get());
            //loadingDialog.close();
        });
        hbox.getChildren().addAll(buttonScs1,buttonScs2,buttonScs3, buttonScs4);

        return hbox;
    }

    private void buttonAction(AnimationTimer timer, Thread thread) {
        thread.start();
        loadingDialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL);
        loadingDialog.setContentText("0 .s");
        timer.start();
    }

    public GridPane addGridPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        hyperlink.setOnAction(e->{
            grid.getChildren().clear();
            TextArea textArea = getTextArea("scs11.out");
            textArea.appendText(getTextArea("scs1.out").getText());
            grid.add(textArea,0,0,10,10);
            textArea.setPrefRowCount(25);
        });

        hyperlink2.setOnAction(e->{
            grid.getChildren().clear();
            if(controller.getCache1().getLatency()==0 ||
               controller.getCache2().getLatency()==0 ||
               controller.getCache3().getLatency()==0 ||
               controller.getRamLatency()==0){
                grid.add(new Text("First you need to run Latency Benchmark"), 0, 0,10,10);
            }else {
                grid.setHgap(25);
                grid.setVgap(25);
                grid.add(new Text("In computing, memory latency is the time (the latency) between initiating a request\n" +
                        " for a byte or word in memory until it is retrieved by a processor. If the data are not in the\n" +
                        " processor's cache, it takes longer to obtain them, as the processor will have to communicate\n" +
                        " with the external memory cells. Latency is therefore a fundamental measure of the speed of\n" +
                        " memory: the less the latency, the faster the reading operation."), 0, 0, 4, 1);
                grid.add(new Text("L1"), 0, 1);
                grid.add(new Text("L2"), 0, 2);
                grid.add(new Text("L3"), 0, 3);
                grid.add(new Text("RAM"), 0, 4);
                grid.add(new Text(controller.getCache1().getLatency() + " ns"), 1, 1);
                grid.add(new Text(controller.getCache2().getLatency() + " ns"), 1, 2);
                grid.add(new Text(controller.getCache3().getLatency() + " ns"), 1, 3);
                grid.add(new Text(controller.getRamLatency() + " ns"), 1, 4);
                centerCells(grid);
            }
        });

        hyperlink3.setOnAction(e->{
            grid.getChildren().clear();
            if(controller.getCache1().getMissRatio()==0 ||
                    controller.getCache2().getMissRatio()==0 ||
                    controller.getCache3().getMissRatio()==0){
                grid.add(new Text("First you need to run Miss Ratio Benchmark"), 0, 0,10,10);
            }else {
                grid.setHgap(25);
                grid.setVgap(25);
                grid.add(new Text("A miss occurs when you need to request data from a specific memory level but that specific data it is\n" +
                        "not on that level. A memory miss occurs either because the data was never placed in the cache, or\n" +
                        "because the data was removed."), 0, 0, 4, 1);
                grid.add(new Text("Miss Ratio"), 1, 1);
                grid.add(new Text("Hit Ratio"), 2, 1);
                grid.add(new Text("L1"), 0, 2);
                grid.add(new Text("L2"), 0, 3);
                grid.add(new Text("L3"), 0, 4);
                grid.add(new Text(controller.getCache1().getMissRatio() + " %"), 1, 2);
                grid.add(new Text(controller.getCache2().getMissRatio() + " %"), 1, 3);
                grid.add(new Text(controller.getCache3().getMissRatio() + " %"), 1, 4);
                grid.add(new Text(100-controller.getCache1().getMissRatio() + " %"), 2, 2);
                grid.add(new Text(100-controller.getCache2().getMissRatio() + " %"), 2, 3);
                grid.add(new Text(100-controller.getCache3().getMissRatio() + " %"), 2, 4);
                centerCells(grid);
            }
        });

        hyperlink4.setOnAction(e->{
            grid.getChildren().clear();
            if(controller.getCache1().getMissPenalty()==0 ||
                    controller.getCache2().getMissPenalty()==0 ||
                    controller.getCache3().getMissPenalty()==0 ||
                    controller.getRamLatency()==0){
                grid.add(new Text("First you need to run Average memory access time Benchmark"), 0, 0,10,10);
            }else {
                grid.setHgap(25);
                grid.setVgap(25);

                grid.add(new Text("In computer science, average memory access time (AMAT) is a common metric\n" +
                        " to analyze memory system performance. AMAT uses hit time, miss penalty, and miss rate to measure\n" +
                        " memory performance. It accounts for the fact that hits and misses affect memory system performance differently.\n\n" +
                        "AMAT's three parameters hit time (or hit latency), miss rate, and miss penalty provide a quick analysis\n" +
                        " of memory systems. Hit latency (H) is the time to hit in the cache. Miss rate (MR) is the frequency of\n" +
                        " cache misses, while average miss penalty (AMP) is the cost of a cache miss in terms of time.\n" +
                        " Concretely it can be defined as follows\n" +
                        "\n" +
                        " AMAT=H+MR\u2022 AMP}AMAT=H+MR\u2022 AMP\n" +
                        "\n" +
                        "It can also be defined recursively as,\n" +
                        "\n" +
                        "AMAT=H1+MR1\u2022 AMP1}AMAT=H1+MR1\u2022 AMP1\n" +
                        "\n" +
                        "where\n" +
                        "\n" +
                        " AMP1=H2+MR2\u2022 AMP2}AMP1=H2+MR2\u2022 AMP2"), 0, 0, 4, 1);
                grid.add(new Text("L1"), 0, 1);
                grid.add(new Text("L2"), 0, 2);
                grid.add(new Text("L3"), 0, 3);
                grid.add(new Text(controller.getCache1().getMissPenalty() + " ns"), 1, 1);
                grid.add(new Text(controller.getCache2().getMissPenalty() + " ns"), 1, 2);
                grid.add(new Text(controller.getCache3().getMissPenalty() + " ns"), 1, 3);
                grid.add(new Text("Average memory access time of the system is "+controller.getAmat() + " ns"), 1, 4,4,1);
                centerCells(grid);
            }
        });

        return grid;
    }

    private void centerCells(GridPane gridPane){
        for (Node node: gridPane.getChildren()) {
            gridPane.setHalignment(node, HPos.CENTER);
            gridPane.setValignment(node, VPos.CENTER);
        }
    }

    private TextArea getTextArea(String path) {
        TextArea textArea = new TextArea();
        File file = new File(path);
        try (Scanner input = new Scanner(file)) {
            while (input.hasNextLine()) {
                textArea.appendText(input.nextLine()+"\n");
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        return textArea;
    }

    public VBox addVBox() {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(8);

        Text title = new Text("Data");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        vbox.getChildren().add(title);

        Hyperlink options[] = new Hyperlink[] {
                hyperlink,
                hyperlink2,
                hyperlink3,
                hyperlink4};

        for (int i=0; i<4; i++) {
            VBox.setMargin(options[i], new Insets(0, 0, 0, 8));
            vbox.getChildren().add(options[i]);
        }

        return vbox;
    }

    public void addStackPane(HBox hb) {
        StackPane stack = new StackPane();
        Rectangle helpIcon = new Rectangle(30.0, 25.0);
        helpIcon.setFill(new LinearGradient(0,0,0,1, true, CycleMethod.NO_CYCLE,
                new Stop[]{
                        new Stop(0, Color.web("#4977A3")),
                        new Stop(0.5, Color.web("#B0C6DA")),
                        new Stop(1,Color.web("#9CB6CF")),}));
        helpIcon.setStroke(Color.web("#D0E6FA"));
        helpIcon.setArcHeight(3.5);
        helpIcon.setArcWidth(3.5);

        Text helpText = new Text("?");
        helpText.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        helpText.setFill(Color.WHITE);
        helpText.setStroke(Color.web("#7080A0"));

        stack.getChildren().addAll(helpIcon, helpText);
        stack.setAlignment(Pos.CENTER_RIGHT);     // Right-justify nodes in stack
        StackPane.setMargin(helpText, new Insets(0, 10, 0, 0)); // Center "?"

        stack.setOnMouseClicked(e->{
            try {
                Desktop.getDesktop().open(new File("Documentation.pdf"));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        hb.getChildren().add(stack);            // Add to HBox from Example 1-2
        HBox.setHgrow(stack, Priority.ALWAYS);    // Give stack any extra space
    }

    public FlowPane addFlowPane() {
        FlowPane flow = new FlowPane();
        flow.setPadding(new Insets(5, 0, 5, 0));
        flow.setVgap(4);
        flow.setHgap(4);
        flow.setPrefWrapLength(170); // preferred width allows for two columns
        flow.setStyle("-fx-background-color: DAE6F3;");

//        ImageView pages[] = new ImageView[8];
//        for (int i=0; i<8; i++) {
//            pages[i] = new ImageView(
//                    new Image(Main.class.getResourceAsStream(
//                            "graphics/chart_"+(i+1)+".png")));
//            flow.getChildren().add(pages[i]);
//        }

        return flow;
    }
}
