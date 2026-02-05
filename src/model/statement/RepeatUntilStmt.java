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
        // Logica: repeat S until C  ===>  execută S, apoi cât timp (!C) execută S
        // Traducere în statement-uri existente:
        // S; while(!C) S

        // 1. Creăm expresia de negație: (!exp) este simulat prin (exp == false)
        // Verifică în proiectul tău dacă "==" este operatorul corect în RelationalExp (de obicei este).
        Exp notExp = new RelationalExp("==", exp, new ValueExp(new BoolValue(false)));

        // 2. Creăm statement-ul compus: stmt urmat de while
        IStmt converted = new CompStmt(stmt, new WhileStmt(notExp, stmt));

        // 3. Punem rezultatul pe stiva de execuție
        state.getStk().push(converted);

        return null;
    }

    @Override
    public MyIDictionary<String, Type> typecheck(MyIDictionary<String, Type> typeEnv) throws MyException {
        // Verificăm dacă condiția este de tip bool
        Type typeExp = exp.typecheck(typeEnv);
        if (typeExp.equals(new BoolType())) {
            // Verificăm corpul instrucțiunii (stmt) într-un mediu clonat
            stmt.typecheck(typeEnv.deepCopy());
            return typeEnv;
        } else {
            throw new MyException("RepeatUntil: condiția (exp) nu este de tip boolean!");
        }
    }

    @Override
    public String toString() {
        return "(repeat " + stmt.toString() + " until " + exp.toString() + ")";
    }
}