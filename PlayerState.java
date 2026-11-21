package com.variantsmp;

public final class PlayerState {

    public int maceHits;
    public boolean maceEmpowered;
    public boolean maceHit;
    public boolean maceSmash;
    public boolean maceLoadTarget;
    public boolean maceChainKill;
    public int macePendingLevel = -1;
    public int maceLandTick = -1;
    public int maceLastEmpoweredHit = -1000;
    public int maceCooldownUntil;
    public boolean maceCooling;
    public int maceShowUntil;

    public boolean spearAirChain;
    public int spearCooldownUntil;
    public boolean spearCooling;
    public int spearShowUntil;
    public int spearFreeJabTick = -1;

    public boolean wasInCombat;

    public void resetProgress() {
        maceHits = 0;
        maceEmpowered = false;
        maceHit = false;
        maceSmash = false;
        maceLoadTarget = false;
        maceChainKill = false;
        macePendingLevel = -1;
        maceLandTick = -1;
        maceLastEmpoweredHit = -1000;
        spearAirChain = false;
    }

    public void resetMace() {
        maceHits = 0;
        maceEmpowered = false;
        maceHit = false;
        maceSmash = false;
        maceLoadTarget = false;
        maceChainKill = false;
        macePendingLevel = -1;
        maceLandTick = -1;
        maceLastEmpoweredHit = -1000;
        maceCooldownUntil = 0;
        maceCooling = false;
        maceShowUntil = 0;
    }

    public void resetSpear() {
        spearAirChain = false;
        spearCooldownUntil = 0;
        spearCooling = false;
        spearShowUntil = 0;
        spearFreeJabTick = -1;
    }
}
