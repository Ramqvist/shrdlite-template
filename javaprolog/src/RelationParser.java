package src;
import java.util.ArrayList;
import java.util.List;

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.Interpreter.Goal;

public class RelationParser {


	private static Environment env;
	private static Interpreter interpreter;

	public RelationParser(String file) throws PrologException {
		env = new Environment();
		env.ensureLoaded(AtomTerm.get(file));
		env.ensureLoaded(AtomTerm.get("world.pl"));
		interpreter = env.createInterpreter();
		env.runInitialization(interpreter);
	}
	
	public List<Term> checkRelations(AtomTerm functor, Term arg1, Term arg2) throws PrologException {
		List<Term> out = new ArrayList<Term>();
		VariableTerm resultTerm = new VariableTerm("Res");
		Term goalTerm = new CompoundTerm(functor,new Term[]{arg1,arg2,resultTerm});
		Goal goal = interpreter.prepareGoal(goalTerm);
		int rc = PrologCode.SUCCESS;
		while(rc == PrologCode.SUCCESS) {
			rc = interpreter.execute(goal);
			if (rc == PrologCode.SUCCESS || rc == PrologCode.SUCCESS_LAST) {
				Term result = resultTerm.dereference();
				Debug.print("%%%%%%%%%%%%%%%%%%%% "+result);
				out.add(result);
			}
		}
		return out;
	}
	public List<Term> checkRelations(String functor, String arg1, String arg2) throws PrologException {
		return checkRelations(AtomTerm.get(functor),new VariableTerm(arg1),AtomTerm.get(arg2));
	}
	
}
