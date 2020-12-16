package com.vborovin.japetoken;

import gate.util.Err;
import org.fife.ui.rsyntaxtextarea.*;

public class ExtendedToken extends TokenImpl {

    private int state;

    public ExtendedToken() {

    }

    public ExtendedToken(Token source) {
        super(source);
        if (source instanceof ExtendedToken) {
            state = ((ExtendedToken) source).getState();
        }
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public boolean isLeftParenthesis() {
        return this.getType() == 22 && this.isSingleChar('(');
    }

    public boolean isRightParenthesis() {
        return this.getType() == 22 && this.isSingleChar(')');
    }
}