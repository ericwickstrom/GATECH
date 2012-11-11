import nfa.NFABuilder;
import nfa.NFAUtil;
import nfa.NFAUtil.NFASegment;
import spec.Spec;
import spec.SpecReader;
import spec.TokenType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class TheProject {
    final InputStream specFileInputStream;
    final InputStream programFileInputStream;

    public TheProject(InputStream specFileInputStream, InputStream programFileInputStream) {
        this.specFileInputStream = specFileInputStream;
        this.programFileInputStream = programFileInputStream;
    }

    public void doStuff() {
        SpecReader specReader = new SpecReader(this.specFileInputStream);
        Spec spec = specReader.specify();
        System.out.println(spec);

        NFASegment nfa = NFABuilder.buildNFAFromSpec(spec);
        System.out.println(NFAUtil.isValid(nfa, "a"));
        System.out.println(NFAUtil.isValid(nfa, "abcbacabc"));
        System.out.println(NFAUtil.isValid(nfa, "ABCABCBAB"));
        System.out.println(NFAUtil.isValid(nfa, "012012011"));
        System.out.println(NFAUtil.isValid(nfa, "0121accb2"));
        System.out.println(NFAUtil.isValid(nfa, "acCAbcaCB"));
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Parameters: <spec-file> <program-file>");
            System.exit(1);
        }

        String specFilePath = args[0];
        String programFilePath = args[1];

        TheProject project = null;

        try {
            InputStream specFileInputStream = new FileInputStream(specFilePath);
            InputStream programFileInputStream = new FileInputStream(programFilePath);
            project = new TheProject(specFileInputStream, programFileInputStream);
        } catch (FileNotFoundException ex) {
            System.err.println(ex);
            System.exit(1);
        }

        project.doStuff();
    }
}
