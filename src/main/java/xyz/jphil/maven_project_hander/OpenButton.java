package xyz.jphil.maven_project_hander;

import javafx.scene.control.Button;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.text.Text;
import javafx.util.Callback;

public class OpenButton implements Callback<TreeTableColumn<ProjectTreeItem, String>, TreeTableCell<ProjectTreeItem, String>> {
    public enum Action {
        OPENNB("Netbeans"),
        OPENINTELLIJ("Intellij"),
        OPENFOLDER("Open"),
        COPYPATH("Copy"),
        EDITPOM("Edit"),
        COPYDEPENDENCY("<dependency>..."),
        CMD("Cmd")
        ;
        public String buttonText;

        Action(String buttonText) {
            this.buttonText = buttonText;
        }
    }
    public interface ActionExecutor {
        void execute(Action action);
    }
    private final Action actionType;

    public OpenButton(Action type) {
        this.actionType = type;
    }

    @Override
    public TreeTableCell<ProjectTreeItem, String> call(TreeTableColumn<ProjectTreeItem, String> p) {
        final TreeTableCell<ProjectTreeItem, String> cell = new TreeTableCell<>() {
            final Button btn = new Button(actionType.buttonText);
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                ProjectTreeItem project_i;
                var tree_item = getTreeTableView().getTreeItem(getIndex());
                project_i = tree_item==null?null:tree_item.getValue();
                boolean showButton = tree_item==null?false:project_i.supportsAction(actionType);
                if(tree_item!=null && tree_item.getValue()!=null && tree_item.getValue().dir==null)
                    showButton = false;
                if (!showButton) {
                    setGraphic(null);
                    setText(null);
                } else {
                    btn.setOnAction(event -> project_i.executeAction(actionType));
                    setGraphic(btn);
                    setText(null);
                }
            }
        };
        return cell;
    }
}
