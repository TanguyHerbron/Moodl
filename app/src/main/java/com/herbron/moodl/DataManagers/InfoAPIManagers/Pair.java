package com.herbron.moodl.DataManagers.InfoAPIManagers;

public class Pair {

    private String from;
    private String to;

    public Pair(String from, String to)
    {
        this.from = from;
        this.to = to;
    }

    public boolean contains(String symbol)
    {
        return symbol.equals(from) || symbol.equals(to);
    }
}
