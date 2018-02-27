package input;

class Test_catch {
    class T {
        void t() throws Exception {}
        void n() {}
    }

    void m(T t) {
        try {
            try {
                try {
                    t.t();
                } catch (java.lang.Exception e) {
                }
            } finally {
                t.n();
            }
        } finally {
            t.n();
        }
    }
}
