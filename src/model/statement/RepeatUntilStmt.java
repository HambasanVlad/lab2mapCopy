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
        // Logic: repeat stmt until exp
        // Equivalent to: stmt; while(!exp) stmt

        // 1. Create the negation of the expression (!exp)
        // We simulate !exp by checking (exp == false)
        Exp notExp = new RelationalExp("==", exp, new ValueExp(new BoolValue(false)));

        // 2. Create the equivalent statement
        IStmt converted = new CompStmt(stmt, new WhileStmt(notExp, stmt));

        // 3. Push the new statement onto the stack
        state.getStk().push(converted);

        return null;
    }

    @Override
    public MyIDictionary<String, Type> typecheck(MyIDictionary<String, Type> typeEnv) throws MyException {
        Type typeExp = exp.typecheck(typeEnv);
        if (typeExp.equals(new BoolType())) {
            stmt.typecheck(typeEnv.deepCopy());
            return typeEnv;
        } else {
            throw new MyException("RepeatUntil: condition is not boolean!");
        }
    }

    @Override
    public String toString() {
        return "(repeat " + stmt.toString() + " until " + exp.toString() + ")";
    }
}