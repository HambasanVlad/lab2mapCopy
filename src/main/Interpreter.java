package main;

import model.expression.*;
import model.statement.*;
import model.type.*;
import model.value.*;
import java.util.ArrayList;
import java.util.List;

public class Interpreter {

    // Această metodă returnează lista de exemple pentru GUI
    public static List<IStmt> getExamples() {
        List<IStmt> examples = new ArrayList<>();



        IStmt ex = new CompStmt(
                new VarDeclStmt("v", new IntType()),
                new CompStmt(new VarDeclStmt("x", new IntType()),
                        new CompStmt(new VarDeclStmt("y", new IntType()),
                                new CompStmt(new AssignStmt("v", new ValueExp(new IntValue(0))),
                                        new CompStmt(
                                                new RepeatUntilStmt(
                                                        new CompStmt(
                                                                new ForkStmt(
                                                                        new CompStmt(
                                                                                new PrintStmt(new VarExp("v")),
                                                                                new AssignStmt("v", new ArithExp('-', new VarExp("v"), new ValueExp(new IntValue(1))))
                                                                        )
                                                                ),
                                                                new AssignStmt("v", new ArithExp('+', new VarExp("v"), new ValueExp(new IntValue(1))))
                                                        ),
                                                        new RelationalExp("==", new VarExp("v"), new ValueExp(new IntValue(3)))
                                                ),
                                                new CompStmt(
                                                        new AssignStmt("x", new ValueExp(new IntValue(1))),
                                                        new CompStmt(
                                                                new NopStmt(),
                                                                new CompStmt(
                                                                        new AssignStmt("y", new ValueExp(new IntValue(3))),
                                                                        new CompStmt(
                                                                                new NopStmt(),
                                                                                new PrintStmt(new ArithExp('*', new VarExp("v"), new ValueExp(new IntValue(10))))
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
        examples.add(ex);
        return examples;
    }

    public static void main(String[] args) {
        // Poți lăsa main-ul vechi aici dacă vrei să rulezi și din consolă,
        // sau poți să pornești direct MainFX.
    }
}