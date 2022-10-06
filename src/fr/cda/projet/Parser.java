package fr.cda.projet;

/**
 * The type Parser.
 */
public class Parser {
    /**
     * Teste si la valeur d'un char est comprise entre 0 et 9
     *
     * @param c the c
     * @return the boolean
     */
    public static boolean isDigit(char c)
    {
        return ((c >= '0' && c <= '9'));
    }

    /**
     * Converti une String au format int
     *
     * @param s the s
     * @return the int
     */
    public static int parseInt(String s)
    {
        boolean     sign;
        boolean     digit;
        String      num;
        int         ret;

        if (s == null || s.length() == 0)
            return (0);
        sign = true;
        digit = true;
        num = "";
        ret = -1;
        for (int i = 0; i < s.length(); i++)
            if (isDigit(s.charAt(i)) || (sign && s.charAt(i) == '-')) {
                if (sign && s.charAt(i) == '-' && i + 1 < s.length() && isDigit(s.charAt(i + 1)))
                {
                    sign = false;
                    continue;
                }
                if (digit)
                    digit = false;
                num += s.charAt(i);
            } else if (!digit) break;
        try { ret = Integer.parseInt(num) * (!sign ? -1 : 1); } catch (Exception e) { return (0); }
        return (ret);
    }

    /**
     * Converti une String au format double (positif)
     *
     * @param s the s
     * @return the double
     */
    public static double parsePositiveDouble(String s) {
        boolean     digit;
        boolean     dot;
        String      num;
        double      ret;

        if (s == null || s.length() == 0)
            return (-1);
        digit = true;
        dot = true;
        num = "";
        ret = -1;
        for (int i = 0; i < s.length(); i++)
            if (isDigit(s.charAt(i)) || (dot && s.charAt(i) == '.')) {
                if (digit)
                    digit = false;
                num += s.charAt(i);
                if (s.charAt(i) == '.')
                    dot = false;
            } else if (!digit) break;
        try { ret = Double.parseDouble(num); } catch (Exception e) { return (-1); }
        return (ret);
    }

}
