package nfa;

import nfa.NFAUtil.*;
import spec.Spec;
import spec.TokenType;

import java.util.List;

public class NFABuilder {
    public static NFASegment buildNFAFromSpec(Spec spec) {
        List<TokenType> tokenTypes = spec.getTokenTypes();
        NFASegment segments[] = new NFASegment[tokenTypes.size()];

        for (int i = 0; i < tokenTypes.size(); ++i) {
            TokenType tokenType = tokenTypes.get(i);
            segments[i] = buildNFAFromRegex(tokenType.getRe());
        }

        NFASegment nfa = NFAUtil.aOrB(segments);
        nfa.end.addTransition(new Transition(new State("trueEnd", true)));
        return nfa;
    }

    private static NFASegment buildNFAFromRegex(String regex) {
        NFASegment nfa = NFAUtil.empty();

        int idx = 0;
        while (idx < regex.length()) {
            char c = regex.charAt(idx);
            ++idx;

            if (c == '(') {
                int ct = 1;
                int subidx = idx;
                while (ct > 0) {
                    char paren = regex.charAt(subidx);
                    if (paren == '(') {
                        ++ct;
                    } else if (paren == ')') {
                        --ct;
                    }
                    ++subidx;
                }

                NFASegment segment = buildNFAFromRegex(regex.substring(idx, subidx - 1));

                idx = subidx;

                if (idx < regex.length() && regex.charAt(idx) == '*') {
                    ++idx;
                    segment = NFAUtil.aStar(segment);
                }

                nfa = NFAUtil.ab(nfa, segment);
            } else if (c == '|') {
                nfa = NFAUtil.aOrB(nfa, buildNFAFromRegex(regex.substring(idx)));
                idx = regex.length();
            } else if (c == '\\') {
                // Wait for next char
            } else {
                NFASegment segment = NFAUtil.a(String.valueOf(c));

                if (idx < regex.length() && regex.charAt(idx) == '*') {
                    ++idx;
                    segment = NFAUtil.aStar(segment);
                }

                nfa = NFAUtil.ab(nfa, segment);
            }
        }

        return nfa;
    }
}
