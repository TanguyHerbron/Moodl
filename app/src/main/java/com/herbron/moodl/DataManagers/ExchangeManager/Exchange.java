package com.herbron.moodl.DataManagers.ExchangeManager;

import com.herbron.moodl.DataManagers.InfoAPIManagers.Pair;

import java.util.ArrayList;
import java.util.List;

public class Exchange {

    protected int id;
    protected String name;
    protected int type;
    protected String description;
    protected String publicKey;
    protected String privateKey;
    protected boolean isEnabled;
    private List<Pair> pairs;

    public Exchange(int id, String name, int type, String description, String publicKey, String privateKey, boolean isEnabled)
    {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.isEnabled = isEnabled;
    }

    public Exchange(String name, List<Pair> pairs)
    {
        this.name = name;
        this.pairs = pairs;
    }

    public Exchange(Exchange exchange)
    {
        this.name = exchange.name;
        this.pairs = exchange.pairs;
    }

    public List<Pair> getPairs()
    {
        return pairs;
    }

    public boolean isEnabled()
    {
        return isEnabled;
    }

    public String getPublicKey()
    {
        return publicKey;
    }

    public String getPrivateKey()
    {
        return privateKey;
    }

    public int getType()
    {
        return type;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public int getId()
    {
        return id;
    }

    public List<Pair> getPairsFor(String symbol)
    {
        List<Pair> compatiblePairs = new ArrayList<>();

        for(Pair pair : pairs)
        {
            if(pair.contains(symbol))
            {
                compatiblePairs.add(pair);
            }
        }

        return compatiblePairs;
    }
}
