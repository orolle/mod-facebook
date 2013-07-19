package de.orolle.vertx2.modfacebook;

class URIHelper{
    /** Converts a string into something you can safely insert into a URL. */
    public static String encodeURIcomponent(String s)
    {
            StringBuilder o = new StringBuilder();
            for (char ch : s.toCharArray()) {
                    if (isUnsafe(ch)) {
                            o.append('%');
                            o.append(toHex(ch / 16));
                            o.append(toHex(ch % 16));
                    }
                    else o.append(ch);
            }
            return o.toString();
    }

    public static String uri(String s){
            return encodeURIcomponent(s);
    }

    private static char toHex(int ch)
    {
            return (char)(ch < 10 ? '0' + ch : 'A' + ch - 10);
    }

    private static boolean isUnsafe(char ch)
    {
            if (ch > 128 || ch < 0)
                    return true;
            return " %$&+,/:;=?@<>#%".indexOf(ch) >= 0;
    }
}