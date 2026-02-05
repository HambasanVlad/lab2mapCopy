package model.statement;

import exception.MyException;
import model.PrgState;
import model.adt.MyIDictionary;
import model.expression.Exp;
import model.expression.RelationalExp;
import model.expression.ValueExp;
import model.type.BoolType;
import model.type.Type;
import model.value.BoolValue;

public class RepeatUntilStmt implements IStmt {
    private final IStmt stmt;
    private final Exp exp;

    public RepeatUntilStmt(IStmt stmt, Exp exp) {
        this.stmt = stmt;
        this.exp = exp;
    }

    @Override
    public PrgState execute(PrgState state) throws MyException {
        // Requirement: stmt1 is executed as long as exp2 is not true.
        // Execution steps from PDF:
        // 1. pop the statement (done by the interpreter before calling execute)
        // 2. create: stmt1; (while(!exp2) stmt1)
        // 3. push the new statement on the stack

        // Since we don't have a specific NotExp, we simulate !exp using (exp == false)
        Exp notExp = new RelationalExp("==", exp, new ValueExp(new BoolValue(false)));

        IStmt newStmt = new CompStmt(stmt, new WhileStmt(notExp, stmt));
        state.getStk().push(newStmt);

        return null;
    }

    @Override
    public MyIDictionary<String, Type> typecheck(MyIDictionary<String, Type> typeEnv) throws MyException {
        // Requirement: verifies if exp2 has type bool and typecheck stmt1
        Type typeExp = exp.typecheck(typeEnv);
        if (typeExp.equals(new BoolType())) {
            stmt.typecheck(typeEnv.deepCopy());
            return typeEnv;
        } else {
            throw new MyException("RepeatUntil: condition is not of type bool");
        }
    }

    @Override
    public String toString() {
        return "(repeat " + stmt.toString() + " until " + exp.toString() + ")";
    }
}