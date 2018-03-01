package input;

import java.util.List;

class Test_constant {
    private static final int n = 0;
    private final boolean b = true;
    private final char c = 'c';
    private final String s = "s";
    private final Integer N = 0;
    private final Boolean B = true;
    private final Character C = 'c';

    void m(List l) {
        l.remove(Test_constant.n);
        l.remove(b);
        l.remove(c);
        l.remove(s);
        l.remove(N);
        l.remove(B);
        l.remove(Integer.MAX_VALUE);
        l.remove(C);
        l.remove(Integer.MAX_VALUE);
    }
}
