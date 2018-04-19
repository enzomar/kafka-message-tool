package application.customfxwidgets;

import application.constants.ApplicationConstants;
import application.kafka.cluster.TopicCleanupPolicy;
import application.kafka.dto.TopicToAdd;
import application.utils.GuiUtils;
import application.utils.UserGuiInteractor;
import application.utils.UserInteractor;
import application.utils.ValidatorUtils;
import javafx.beans.property.IntegerProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class AddTopicDialog extends AnchorPane {

    private static final int MIN_PARITITION_NUMBER = 1;
    private static final int MAX_PARTITION_NUMBER = 100;
    private static final int MIN_REPLICATION_FACTOR = 1;
    private static final int MAX_REPLICATION_FACTOR = 100;
    private static final String FXML_FILE = "AddTopicDialogView.fxml";
    private final Stage stage = new Stage();
    @FXML
    private TextField topicNameField;
    @FXML
    private Spinner<Integer> partitionSpinner;
    @FXML
    private Spinner<Integer> replicationFactorSpinner;

    @FXML
    private ComboBox<TopicCleanupPolicy> cleanupPolicyComboBox;

    private ButtonType returnButtonType = ButtonType.CLOSE;
    private TopicToAdd topicToAdd;

    public AddTopicDialog(Window owner) throws IOException {
        FXMLLoader loader = new FXMLLoader(AddTopicDialog.class.getResource(FXML_FILE));
        loader.setRoot(this);
        loader.setController(this);
        loader.load();
        stage.initOwner(owner);
    }

    public ButtonType call(TopicToAdd topicToAdd) {
        this.topicToAdd = topicToAdd;
        configurePartitionSpinner(topicToAdd);

        configureReplicationFactorSpinner();

        topicNameField.textProperty().bindBidirectional(topicToAdd.topicNameProperty());
        GuiUtils.configureTextFieldChangeStyleOnInvalidValue(topicNameField,
                                                             ValidatorUtils::isStringIdentifierValid);


        cleanupPolicyComboBox.setItems(FXCollections.observableArrayList(TopicCleanupPolicy.values()));
        cleanupPolicyComboBox.setValue(topicToAdd.getCleanupPolicy());
        topicToAdd.cleanupPolicyProperty().bind(cleanupPolicyComboBox.valueProperty());


        prepareStage();
        stage.showAndWait();

        return returnButtonType;
    }

    private void configureReplicationFactorSpinner() {
        final IntegerProperty referenceProperty = topicToAdd.replicationFactorProperty();
        ValidatorUtils.configureSpinner(replicationFactorSpinner, referenceProperty, MIN_REPLICATION_FACTOR, MAX_REPLICATION_FACTOR);
    }

    private void configurePartitionSpinner(TopicToAdd topicToAdd) {

        final IntegerProperty referenceProperty = topicToAdd.partitionsProperty();
        ValidatorUtils.configureSpinner(partitionSpinner, referenceProperty, MIN_PARITITION_NUMBER, MAX_PARTITION_NUMBER);

    }

    @FXML
    private void initialize() {
        GuiUtils.addApplicationIcon(stage);
    }

    @FXML
    private void cancelButtonOnAction() {
        closeThisDialogWithCancelStatus();
    }

    private void closeThisDialogWithCancelStatus() {
        returnButtonType = ButtonType.CANCEL;
        stage.close();
    }

    @FXML
    private void okButtonOnAction() {

        if (isValidTopicToAdd()) {
            closeThisDialogWithOkStatus();
        } else {
            showErrorForInvalidTopicConfig();
        }
    }

    private void closeThisDialogWithOkStatus() {
        stage.close();
        returnButtonType = ButtonType.OK;
    }

    private void showErrorForInvalidTopicConfig() {
        final UserInteractor userInteractor = new UserGuiInteractor(stage);
        stage.setAlwaysOnTop(false);
        userInteractor.showError("Invalid topic name.", "Cannot add topic with empty name.");
        stage.setAlwaysOnTop(true);
    }

    private boolean isValidTopicToAdd() {
        return !StringUtils.isBlank(this.topicToAdd.topicNameProperty().get());
    }

    private void prepareStage() {
        final Scene scene = new Scene(this);
        scene.getStylesheets().add(getClass().getResource(ApplicationConstants.GLOBAL_CSS_FILE_NAME).toExternalForm());
        scene.setRoot(this);
        stage.setScene(scene);
        stage.setTitle("Creating new topic...");
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
    }
}
