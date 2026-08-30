package be.kdg.talenten.view.export;

import be.kdg.talenten.domain.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ExportView extends BorderPane {
    private final Button terugButton=new Button("← Terug naar hoofdmenu"), themeButton=new Button("☾"), exportButton=new Button("Maak Excel-lijst");
    private final ComboBox<Schooljaar> schooljaar=new ComboBox<>(); private final ComboBox<TalentenPeriode> periode=new ComboBox<>();
    private final ComboBox<Doelgroep> doelgroep=new ComboBox<>(); private final ComboBox<String> type=new ComboBox<>(); private final Label status=new Label();
    public ExportView(){getStyleClass().add("app-root");setTop(header());setCenter(content());}
    private Pane header(){BorderPane h=new BorderPane();h.getStyleClass().add("section-header");h.setPadding(new Insets(18,28,18,28));terugButton.getStyleClass().add("secondary-button");h.setLeft(terugButton);Label t=new Label("Lijsten exporteren");t.getStyleClass().add("section-title");h.setCenter(t);themeButton.getStyleClass().add("theme-toggle");h.setRight(themeButton);return h;}
    private Node content(){VBox v=new VBox(18);v.setPadding(new Insets(30,60,36,60));v.setAlignment(Pos.TOP_CENTER);v.setMaxWidth(980);Label t=new Label("Lijsten exporteren");t.getStyleClass().add("content-title");Label u=new Label("Exporteer de verdeling per klas of per ingericht talent, binnen één periode en doelgroep.");u.setWrapText(true);u.getStyleClass().add("content-subtitle");VBox intro=new VBox(5,t,u);VBox card=new VBox(16);card.setPadding(new Insets(22));card.setMaxWidth(980);card.getStyleClass().add("content-card");card.getChildren().addAll(choice("Schooljaar",schooljaar,"Selecteer een schooljaar"),choice("Talentenperiode",periode,"Selecteer een talentenperiode"),choice("Doelgroep",doelgroep,"Selecteer een doelgroep"),choice("Exporteren per",type,"Selecteer een exporttype"));exportButton.getStyleClass().add("primary-button");exportButton.setMaxWidth(Double.MAX_VALUE);card.getChildren().add(exportButton);status.getStyleClass().add("status-message");status.setWrapText(true);status.setVisible(false);status.setManaged(false);v.getChildren().addAll(intro,card,status);StackPane s=new StackPane(v);s.setAlignment(Pos.TOP_CENTER);ScrollPane p=new ScrollPane(s);p.setFitToWidth(true);p.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);p.getStyleClass().add("content-scroll");return p;}
    private <T> VBox choice(String text,ComboBox<T> box,String prompt){Label l=new Label(text);l.getStyleClass().add("field-label");box.setEditable(false);box.setPromptText(prompt);box.setMaxWidth(Double.MAX_VALUE);return new VBox(6,l,box);}
    public Button getTerugButton(){return terugButton;}public Button getThemeButton(){return themeButton;}public Button getExportButton(){return exportButton;}public ComboBox<Schooljaar> getSchooljaar(){return schooljaar;}public ComboBox<TalentenPeriode> getPeriode(){return periode;}public ComboBox<Doelgroep> getDoelgroep(){return doelgroep;}public ComboBox<String> getType(){return type;}
    public void toonStatus(String m,boolean fout){status.setText(m);status.getStyleClass().removeAll("status-success","status-error");status.getStyleClass().add(fout?"status-error":"status-success");status.setVisible(true);status.setManaged(true);}public void updateThemeIcon(boolean dark){themeButton.setText(dark?"☀":"☾");}
}
