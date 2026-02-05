package gui;

import controller.Controller;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.PrgState;
import model.statement.IStmt;
import model.value.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProgramExecutorController {
    private Controller controller;

    @FXML
    private TextField numberOfPrgStatesTextField;
    @FXML
    private TableView<Map.Entry<Integer, Value>> heapTableView;
    @FXML
    private TableColumn<Map.Entry<Integer, Value>, Integer> addressColumn;
    @FXML
    private TableColumn<Map.Entry<Integer, Value>, String> valueColumn;
    @FXML
    private ListView<String> outputListView;
    @FXML
    private ListView<String> fileTableListView;
    @FXML
    private ListView<Integer> prgStateListIdListView;
    @FXML
    private TableView<Map.Entry<String, Value>> symbolTableView;
    @FXML
    private TableColumn<Map.Entry<String, Value>, String> variableNameColumn;
    @FXML
    private TableColumn<Map.Entry<String, Value>, String> variableValueColumn;
    @FXML
    private ListView<String> executionStackListView;
    @FXML
    private Button runOneStepButton;

    public void setController(Controller controller) {
        this.controller = controller;
        populate();
    }

    @FXML
    public void initialize() {
        prgStateListIdListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Mapare coloane Heap
        addressColumn.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().getKey()).asObject());
        valueColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getValue().toString()));

        // Mapare coloane Symbol Table
        variableNameColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getKey()));
        variableValueColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getValue().toString()));

        // Listener pentru selectarea unui ID din lista de PrgStates
        prgStateListIdListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            changeProgramState(getCurrentProgramState());
        });
    }

    private PrgState getCurrentProgramState() {
        if (controller.getRepo().getPrgList().size() == 0)
            return null;
        int currentId = prgStateListIdListView.getSelectionModel().getSelectedIndex();
        if (currentId == -1)
            return controller.getRepo().getPrgList().get(0);
        return controller.getRepo().getPrgList().get(currentId);
    }

    @FXML
    public void runOneStep() {
        if (controller.getRepo().getPrgList().size() == 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Done");
            alert.setContentText("Program finished!");
            alert.showAndWait();
            return;
        }
        try {
            controller.oneStepForAllPrg(controller.getRepo().getPrgList());
            populate();
        } catch (InterruptedException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void populate() {
        populateHeap();
        populateProgramStateIdentifiers();
        populateFileTable();
        populateOutput();
        changeProgramState(getCurrentProgramState());
    }

    private void changeProgramState(PrgState currentPrgState) {
        if (currentPrgState == null)
            return;
        populateSymbolTable(currentPrgState);
        populateExeStack(currentPrgState);
    }

    private void populateProgramStateIdentifiers() {
        List<PrgState> prgStates = controller.getRepo().getPrgList();
        prgStateListIdListView.setItems(FXCollections.observableList(getPrgStateIds(prgStates)));
        numberOfPrgStatesTextField.setText(String.valueOf(prgStates.size()));
    }

    private List<Integer> getPrgStateIds(List<PrgState> prgList) {
        return prgList.stream().map(PrgState::getId).collect(Collectors.toList());
    }

    private void populateHeap() {
        PrgState prgState = getCurrentProgramState();
        if (prgState != null)
            heapTableView.setItems(FXCollections.observableList(new ArrayList<>(prgState.getHeap().getContent().entrySet())));
    }

    private void populateFileTable() {
        PrgState prgState = getCurrentProgramState();
        if (prgState != null) {
            List<String> files = new ArrayList<>();
            for (Object entry : prgState.getFileTable().getContent().keySet()) {
                files.add(entry.toString());
            }
            fileTableListView.setItems(FXCollections.observableArrayList(files));
        }
    }

    private void populateOutput() {
        PrgState prgState = getCurrentProgramState();
        if (prgState != null) {
            List<String> output = new ArrayList<>();
            // Presupunând că lista de output este în `out` din PrgState
            // Trebuie să iterezi structura ta de date pentru Out (MyList)
            // Aici fac toString direct pe obiectul MyList, dar ideal ar fi să ai un getter pentru lista internă
            output.add(prgState.getOut().toString());
            outputListView.setItems(FXCollections.observableArrayList(output));
        }
    }

    private void populateSymbolTable(PrgState state) {
        symbolTableView.setItems(FXCollections.observableList(new ArrayList<>(state.getSymTable().getContent().entrySet())));
    }

    private void populateExeStack(PrgState state) {
        // Luăm stiva ca Iterable (presupunând că MyStack are o metodă getReverse sau toString corect)
        // Cea mai simplă metodă e să luăm String-ul și să îl curățăm,
        // dar ideal ar fi să iterezi.

        // Varianta simplă care merge cu codul tău curent:
        executionStackListView.getItems().clear();

        // Truc: Afișăm reprezentarea textuală a stivei
        String stackStr = state.getStk().toString();
        // Dacă toString-ul tău e de forma "[stmt1, stmt2]", îl punem în listă
        executionStackListView.getItems().add(stackStr);

        // RECOMANDARE: Dacă vrei să vezi statement-urile unul sub altul,
        // trebuie să modifici în MyStack să returneze List<IStmt>.
    }
}