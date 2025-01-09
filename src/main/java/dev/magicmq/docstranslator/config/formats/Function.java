package dev.magicmq.docstranslator.config.formats;


public class Function {

    private String initDefinition;
    private String definition;
    private String parameterRegular;
    private String parameterVararg;
    private String returnRegular;
    private String returnWithValue;

    public String getInitDefinition() {
        return initDefinition;
    }

    public void setInitDefinition(String initDefinition) {
        this.initDefinition = initDefinition;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getParameterRegular() {
        return parameterRegular;
    }

    public void setParameterRegular(String parameterRegular) {
        this.parameterRegular = parameterRegular;
    }

    public String getParameterVararg() {
        return parameterVararg;
    }

    public void setParameterVararg(String parameterVararg) {
        this.parameterVararg = parameterVararg;
    }

    public String getReturnRegular() {
        return returnRegular;
    }

    public void setReturnRegular(String returnRegular) {
        this.returnRegular = returnRegular;
    }

    public String getReturnWithValue() {
        return returnWithValue;
    }

    public void setReturnWithValue(String returnWithValue) {
        this.returnWithValue = returnWithValue;
    }
}
