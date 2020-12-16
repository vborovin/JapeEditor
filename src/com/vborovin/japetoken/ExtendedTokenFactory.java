package com.vborovin.japetoken;

import org.fife.ui.rsyntaxtextarea.TokenImpl;
import org.fife.ui.rsyntaxtextarea.DefaultTokenFactory;

public class ExtendedTokenFactory extends DefaultTokenFactory {

    public ExtendedTokenFactory() {
        this(DEFAULT_START_SIZE, DEFAULT_INCREMENT);
    }

    public ExtendedTokenFactory(int size, int increment) {
        this.size = size;
        this.increment = increment;
        currentFreeToken = 0;

        tokenList = new ExtendedToken[size];

        for(int i = 0; i < size; ++i) {
            tokenList[i] = new ExtendedToken();
        }
    }

    @Override
    protected void augmentTokenList() {
        TokenImpl[] temp = new ExtendedToken[this.size + this.increment];
        System.arraycopy(this.tokenList, 0, temp, 0, this.size);
        this.size += this.increment;
        this.tokenList = temp;

        for(int i = 0; i < this.increment; ++i) {
            this.tokenList[this.size - i - 1] = new ExtendedToken();
        }

    }
}