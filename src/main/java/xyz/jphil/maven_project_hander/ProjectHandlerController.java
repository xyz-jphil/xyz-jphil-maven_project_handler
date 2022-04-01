package xyz.jphil.maven_project_hander;

import java.util.SortedSet;
import java.util.function.Function;
import java.util.function.Predicate;
import static javafx.application.Platform.runLater;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import xyz.jphil.jfx.controls.auto_completion_text_field.AutocompletionTextField;

public class ProjectHandlerController {

    Worker w;
    @FXML
    private VBox rootElement;

    @FXML
    private HBox topHbox;

    @FXML
    private Button clearButton;
    
    @FXML
    private Button reloadButton;

    @FXML
    private AutocompletionTextField searchBox;

    @FXML
    private TreeTableView<ProjectTreeItem> projectTreeUI;
    
    @FXML
    private TreeTableColumn<ProjectTreeItem, String> projectCol;
    
    @FXML
    private TreeTableColumn<ProjectTreeItem, String> verCol;

    @FXML
    private TreeTableColumn<ProjectTreeItem, String> intellijCol;

    @FXML
    private TreeTableColumn<ProjectTreeItem, String> nbCol;

    @FXML
    private TreeTableColumn<ProjectTreeItem, String> folderCol;

    @FXML
    private TreeTableColumn<ProjectTreeItem, String> editPomCol;

    @FXML
    private TreeTableColumn<ProjectTreeItem, String> copyPathCol;
    
    @FXML
    private TreeTableColumn<ProjectTreeItem, String> copyDependencyCol;

    @FXML
    private TreeTableColumn<ProjectTreeItem, String> cmdCol;
    
    @FXML
    private TreeTableColumn<ProjectTreeItem, String> pomDescCol;
    
    @FXML
    private TreeTableColumn<?,?> gitCol;

    @FXML
    private TreeTableColumn<ProjectTreeItem, String> ghUserCol;

    @FXML
    private TreeTableColumn<ProjectTreeItem, String> ghOrgCol;

    @FXML
    private TreeTableColumn<ProjectTreeItem, String> ghRepoCol;

    @FXML
    private TreeTableColumn<ProjectTreeItem, String> mavenPubCol;

    ProjectTreeItem vs;

    @FXML
    void initialize() {
        reload();
        initSearch();
        projectCol.setCellValueFactory(valueOf(v->v.displayName()));
        verCol.setCellValueFactory(valueOf(v->v.version()));
        copyPathCol.setCellFactory(new OpenButton(OpenButton.Action.COPYPATH));
        intellijCol.setCellFactory(new OpenButton(OpenButton.Action.OPENINTELLIJ));
        nbCol.setCellFactory(new OpenButton(OpenButton.Action.OPENNB));
        folderCol.setCellFactory(new OpenButton(OpenButton.Action.OPENFOLDER));
        editPomCol.setCellFactory(new OpenButton(OpenButton.Action.EDITPOM));
        copyDependencyCol.setCellFactory(new OpenButton(OpenButton.Action.COPYDEPENDENCY));
        cmdCol.setCellFactory(new OpenButton(OpenButton.Action.CMD));
        pomDescCol.setCellValueFactory(valueOf(v->v.description()));
        ghOrgCol.setCellValueFactory(valueOf(v->v.git().org()));
        ghRepoCol.setCellValueFactory(valueOf(v->v.git().repo()));
        ghUserCol.setCellValueFactory(valueOf(v->v.git().user()));
    }
    
    public void reload(){
        if (projectTreeUI.getRoot() != null) {
            projectTreeUI.setRoot(null);
        }
        reloadButton.setDisable(true);
        new Thread(() -> {
            final var reloadText=new String[1];
            runLater(() -> {
                reloadText[0] = reloadButton.getText();
                reloadButton.setText("Loading ... ");
            });
            try {
                vs = new ProjectTreeItem(null);
                ProjectTreeItem[]cds = new ProjectTreeItem[MavenProjectHandlerPaths.intVal("codedirscount", 1)];
                for (int idx = 0; idx < cds.length; idx++) {
                    var pi = MavenProjectHandlerPaths.path("codedir"+(idx+1));
                    System.out.println(idx+"="+pi);
                    cds[idx] = new ProjectTreeItem(pi);
                    vs.children.add(cds[idx]);
                }
                
                //vs = new ProjectTreeItem(Settings.codeDir);
            } catch (Exception e) {
                e.printStackTrace();
            }
            runLater(() -> {
                reloadButton.setDisable(false);
                reloadButton.setText(reloadText[0]);
                populateTreeUI();
            });
            populateSuggestions();
        }, "Checking directory").start();
    }

    private void populateTreeUI() {
        final String search = searchBox.getText();
        final Predicate<ProjectTreeItem> criteria = prj -> {
            if(prj == null || prj.dir == null ) return false;
            return prj.dir.getFileName().toString().contains(search);
        };
        TreeItem rootTreeItem = createTreeItemFrom(vs,criteria);
        expandTreeView(rootTreeItem); // statement 1
        projectTreeUI.setRoot(rootTreeItem); // statement 2
        // if order of statement 1 and 2 is reversed,
        // buttons do not show up
    }
    
    private void populateSuggestions(){
        var e = searchBox.getEntries();
        e.clear();
        populateEntriesFrom(e, vs);
    }
    
    private void populateEntriesFrom(SortedSet<String> fillInto, ProjectTreeItem fillFrom){
        fillInto.add(fillFrom.displayName());
        for (ProjectTreeItem projectTreeItem : fillFrom.children) {
            populateEntriesFrom(fillInto, projectTreeItem);
        }
    }

    TreeItem createTreeItemFrom(ProjectTreeItem vs, Predicate<ProjectTreeItem> criteria) {
        if(!vs.anyOneFitsCriteria(criteria))return null;
        TreeItem ti = new TreeItem(vs);
        for (ProjectTreeItem visitor : vs.children) {
            TreeItem ti2 = createTreeItemFrom(visitor,criteria);
            if(ti2!=null){
                ti.getChildren().add(ti2);
            }
        }
        return ti;
    }

    private void expandTreeView(TreeItem<?> item) {
        if (item != null && !item.isLeaf()) {
            item.setExpanded(true);
            for (TreeItem<?> child : item.getChildren()) {
                expandTreeView(child);
            }
        }
    }
    
    private void initSearch(){
        searchBox.textProperty().addListener((a, b, c) -> populateTreeUI());
    }
    
    public void clear(){
        searchBox.setText("");
    }
    
    private Callback<TreeTableColumn.CellDataFeatures<ProjectTreeItem, String>, 
        ObservableValue<String>> valueOf(
            Function<ProjectTreeItem,String> transformer) {
        return (cellDataFeatures) -> {
            var v = (ProjectTreeItem) cellDataFeatures.getValue().getValue();
            return new SimpleStringProperty(transformer.apply(v));
        };
                
        
    }
    
}
