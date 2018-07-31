package com.herbron.moodl.DataManagers.ExchangeManager;

public class Exchange {

    protected int id;
    protected String name;
    protected int type;
    protected String description;
    protected String publicKey;
    protected String privateKey;
    protected boolean isEnabled;

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
}
