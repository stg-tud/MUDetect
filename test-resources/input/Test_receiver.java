package input;

import java.util.ArrayList;

public static class Test_receiver {
    protected ArrayList<String> part = new ArrayList<String>();
    protected ArrayList<Object> follow = new ArrayList<Object>();
	StringBuffer sb = new StringBuffer(), buf = new StringBuffer();
	
	int index = 0;
	String oid;
	
    public String nextToken() {
        if (index == oid.length()) {
            return null;
        }
        int     end = index + 1;
        boolean quoted = false;
        boolean escaped = false;
        buf.setLength(0);
        while (end != oid.length()) {
            char    c = oid.charAt(end);
            if (c == '"') {
                if (!escaped) {
                    quoted = !quoted;
                }
                else {
                    buf.append(c);
                }
                escaped = false;
            }
            else {
                if (escaped || quoted) {
                    buf.append(c);
                    escaped = false;
                }
                else if (c == '\\') {
                    escaped = true;
                }
                else if (c == ',') {
                    break;
                }
                else {
                    buf.append(c);
                }
            }
            end++;
        }
        index = end;
        return buf.toString().trim();
    }

    void m() {
    	System.out.println(oid.CASE_INSENSITIVE_ORDER);
    	System.out.println(oid.f2);
    	System.out.println(oid.charAt(oid.indexOf(oid)));
    }

    public String getDefaultName() {
        InverseStore store = this;
        while (true) {
            Object obj = store.follow.get(0);
            if (obj instanceof String)
                return (String)obj;
            store = (InverseStore)obj;
        }
    }

    public boolean isSimilar(String name) {
        int idx = name.indexOf('[');
        name = name.substring(0, idx + 1);
        for (int k = 0; k < part.size(); ++k) {
            if (part.get(k).startsWith(name))
                return true;
        }
        return false;
    }
}