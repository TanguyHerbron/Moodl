package com.herbron.moodl.DataManagers.ExchangeManager;

public class Exchange {

    protected int id;
    protected String name;
    protected int type;
    protected String description;
    protected String publicKey;
    protected String privateKey;

    public Exchange(int id, String name, int type, String description, String publicKey, String privateKey)
    {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }
}
