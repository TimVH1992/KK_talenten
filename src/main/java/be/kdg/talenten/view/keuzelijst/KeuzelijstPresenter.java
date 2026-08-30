package be.kdg.talenten.view.keuzelijst;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.service.beheer.SchooljaarService;
import be.kdg.talenten.service.beheer.TalentenPeriodeService;
import be.kdg.talenten.service.voorkeuren.VoorkeurenExcelService;
import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.main.MainView;
import be.kdg.talenten.view.theme.ThemeManager;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class KeuzelijstPresenter {
    private final KeuzelijstView view; private final MainView mainView; private final SceneManager sceneManager; private final ThemeManager themeManager; private final Scene scene;
    private final SchooljaarService schooljaarService; private final TalentenPeriodeService periodeService; private final VoorkeurenExcelService excelService;
    public KeuzelijstPresenter(KeuzelijstView view, MainView mainView, SceneManager sceneManager, ThemeManager themeManager, Scene scene, SchooljaarService schooljaarService, TalentenPeriodeService periodeService, VoorkeurenExcelService excelService) {
        if (view == null || mainView == null || sceneManager == null || themeManager == null || scene == null || schooljaarService == null || periodeService == null || excelService == null) throw new IllegalArgumentException("KeuzelijstPresenter kreeg een null-afhankelijkheid");
        this.view=view; this.mainView=mainView; this.sceneManager=sceneManager; this.themeManager=themeManager; this.scene=scene; this.schooljaarService=schooljaarService; this.periodeService=periodeService; this.excelService=excelService; configureer(); laadSchooljaren();
    }
    private void configureer() { view.updateThemeIcon(themeManager.isDark()); view.getTerugButton().setOnAction(e -> sceneManager.toon(mainView)); view.getThemeButton().setOnAction(e -> toggleTheme()); configureerDoelgroep(); view.getAanmakenButton().setOnAction(e->maakKeuzelijst()); }
    private void configureerDoelgroep() { view.getDoelgroepComboBox().setConverter(new StringConverter<>() { public String toString(Doelgroep d) { if(d==null)return ""; return d==Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB?"Observatie / opleidingsfase / 1e graad A-B":"Kwalificatiefase / 2e graad A-B";} public Doelgroep fromString(String s){return null;} }); view.getDoelgroepComboBox().setItems(FXCollections.observableArrayList(Doelgroep.values())); view.getDoelgroepComboBox().setValue(Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB); }
    private void laadSchooljaren() { try { List<Schooljaar> jaren=schooljaarService.zoekAlleSchooljaren().stream().sorted(Comparator.comparing(Schooljaar::getStartDatum).reversed()).toList(); Schooljaar actief=schooljaarService.zoekActiefSchooljaar().orElse(jaren.isEmpty()?null:jaren.getFirst()); laadPeriodes(actief); } catch(RuntimeException e){view.toonStatus(boodschap(e,"Het actieve schooljaar kon niet geladen worden."),true);} }
    private void laadPeriodes(Schooljaar jaar) { if(jaar==null){view.getPeriodeComboBox().getItems().clear();return;} try { List<TalentenPeriode> periodes=periodeService.geefPeriodesVoorSchooljaar(jaar).stream().sorted(Comparator.comparing(TalentenPeriode::getStartDatum)).toList(); view.getPeriodeComboBox().setItems(FXCollections.observableArrayList(periodes)); view.getPeriodeComboBox().setValue(kiesPassendePeriode(periodes,LocalDate.now())); } catch(RuntimeException e){view.toonStatus(boodschap(e,"De talentenperiodes konden niet geladen worden."),true);} }
    static TalentenPeriode kiesPassendePeriode(List<TalentenPeriode> periodes, LocalDate vandaag) { return periodes.stream().filter(p->!vandaag.isBefore(p.getStartDatum())&&!vandaag.isAfter(p.getEindDatum())).findFirst().orElseGet(()->periodes.stream().filter(p->p.getStartDatum().isAfter(vandaag)).findFirst().orElse(periodes.isEmpty()?null:periodes.getLast())); }
    private void maakKeuzelijst() { TalentenPeriode periode=view.getPeriodeComboBox().getValue(); Doelgroep doelgroep=view.getDoelgroepComboBox().getValue(); if(periode==null||doelgroep==null){view.toonStatus("Selecteer eerst een talentenperiode en doelgroep.",true);return;} FileChooser kiezer=new FileChooser(); kiezer.setTitle("Keuzelijst opslaan"); kiezer.setInitialFileName("Keuzelijst_"+periode.getNaam()+".xlsx"); kiezer.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excelbestand (*.xlsx)","*.xlsx")); File bestand=kiezer.showSaveDialog(scene.getWindow()); if(bestand==null)return; view.getAanmakenButton().setDisable(true); Task<Void> taak=new Task<>(){protected Void call(){excelService.genereerTemplate(periode,doelgroep,Path.of(bestand.toURI()));return null;}}; taak.setOnSucceeded(e->{view.getAanmakenButton().setDisable(false);view.toonStatus("De keuzelijst is opgeslagen als "+bestand.getName()+".",false);}); taak.setOnFailed(e->{view.getAanmakenButton().setDisable(false);view.toonStatus(boodschap(taak.getException(),"De keuzelijst kon niet aangemaakt worden."),true);}); Thread thread=new Thread(taak,"keuzelijst-aanmaken"); thread.setDaemon(true); thread.start(); }
    private String boodschap(Throwable e,String standaard){return e!=null&&e.getMessage()!=null&&!e.getMessage().isBlank()?e.getMessage():standaard;} private void toggleTheme(){themeManager.toggle(scene);boolean dark=themeManager.isDark();view.updateThemeIcon(dark);mainView.updateThemeIcon(dark);}
}
