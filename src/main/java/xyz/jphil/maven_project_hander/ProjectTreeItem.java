package xyz.jphil.maven_project_hander;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Collections.EMPTY_LIST;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import static xyz.jphil.maven_project_hander.OpenButton.Action.*;
import static xyz.jphil.patterns.lambda.L.*;

public class ProjectTreeItem {

    Path dir;
    Model m = null;
    public ArrayList<ProjectTreeItem> children = new ArrayList<>();
    ProjectTreeItem parent;

    public ProjectTreeItem(Path dir) {
        this.dir = dir;
        check();
    }

    public boolean containsMaven() {
        if (m!=null) {
            return true;
        }
        return !children.isEmpty();
    }
    
    public boolean supportsAction(OpenButton.Action action){
        switch (action) {
            case COPYPATH,OPENFOLDER,CMD -> {return true;}
            case OPENINTELLIJ,OPENNB,COPYDEPENDENCY,EDITPOM -> { return m!=null; }
            default ->{
                throw new AssertionError();
            }
        }
    }
    
    public String displayName(){
        if(dir==null)return "";//root
        return dir.getFileName().toString();
    }
    
    public String description(){
        if(m==null)return "";
        return m.getDescription();
    }
    
    public String version(){
        if(m==null)return "";
        return m.getVersion();    
    }
    
    public GitDir git(){
        return new GitDir(dir==null?null:dir.resolve(".git"));
    }

    private void check() {
        if(dir==null)return;
        List<Path> files = ifError(() -> Files.list(dir).collect(Collectors.toList()), EMPTY_LIST, ERR);
        Supplier<Stream<String>> f = ()->files.stream().map(e -> e.getFileName().toString());
        boolean checkChildren = true;
        if (f.get().anyMatch(e -> e.equalsIgnoreCase("pom.xml"))) {
            final MavenXpp3Reader reader = new MavenXpp3Reader();
            m = ifError(() -> reader.read(new FileReader(dir.resolve("pom.xml").toFile())), null, D);
            // accept
            if (f.get().anyMatch(e -> e.equalsIgnoreCase("src"))) {
                checkChildren = false;
            }
        }
        if (checkChildren) {
            var ds = files.stream().filter(Files::isDirectory).collect(Collectors.toList());
            for (Path d : ds) {
                ProjectTreeItem vc = new ProjectTreeItem(d);
                if (vc.containsMaven()) {
                    children.add(vc);
                }
            }
        }
    }
    public void executeAction(OpenButton.Action action){
        if(!supportsAction(action))return;
        switch (action){
            case OPENNB -> {
                var binaryPath = MavenProjectHandlerPaths.property(OPENNB);
                String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
                
                if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
                    processExecute(
                        binaryPath,
                        "--open", dir.toString()
                    );
                } else if (OS.indexOf("win") >= 0) {
                    processExecute(
                        binaryPath,
                        "--console","suppress","--open",dir.toString()
                    );
                }
            }
            case OPENINTELLIJ -> {
                processExecute(
                    MavenProjectHandlerPaths.property(OPENINTELLIJ),
                    "nosplash", dir.toString());
            }
            case OPENFOLDER -> {
                try {
                    Desktop.getDesktop().open(dir.toFile());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            case CMD ->{
                try {
                    var pb = new ProcessBuilder("cmd","/k","start");
                    pb.directory(dir.toFile());
                    var p = pb.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            case COPYPATH -> {
                var strTrnsfer = new StringSelection(dir.toString());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(strTrnsfer,null);
            }
            case EDITPOM ->{
                processExecute("notepad", dir.resolve("pom.xml").toString());
            }
            case COPYDEPENDENCY -> {
                copyDependency();
            }
            default -> throw new IllegalStateException("Unexpected value: " + action);
        }
    }
    
    private void copyDependency(){
        String dependencyString = """
                                          <dependency>
                                              <groupId>%1$s</groupId>
                                              <artifactId>%2$s</artifactId>
                                              <version>%3$s</version>
                                          </dependency>
                                  """;
        dependencyString = dependencyString.replace("%1$s", m.getGroupId());
        dependencyString = dependencyString.replace("%2$s", m.getArtifactId());
        dependencyString = dependencyString.replace("%3$s", m.getVersion());
        var strTrnsfer = new StringSelection(dependencyString);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(strTrnsfer,null);    
    }

    private void processExecute(String ... commandline){
        processExecute(null, commandline);
    }
    private void processExecute(Path pth, String ... commandline){
        try {
            var pb = new ProcessBuilder(commandline).inheritIO();
            if(pth!=null)pb.directory(pth.toFile());
            var p = pb.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public boolean anyOneFitsCriteria(Predicate<ProjectTreeItem> criteria){
        if(criteria.test(this))return true;
        for (ProjectTreeItem p : children) {
            if(p.anyOneFitsCriteria(criteria))return true;
        }
        return false;
    }

}
