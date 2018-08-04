package com.herbron.moodl.DataManagers.InfoAPIManagers;

public class Pair {

    private String from;
    private String to;

    public Pair(String from, String to)
    {
        this.from = from;
        this.to = to;
    }

    public Pair(Pair pair)
    {
        this.from = pair.from;
        this.to = pair.to;
    }

    public boolean contains(String symbol)
    {
        symbol = symbol.toUpperCase();

        return symbol.equals(from) || symbol.equals(to);
    }

    public String getFrom()
    {
        return from;
    }

    public String getTo()
    {
        return to;
    }
}
