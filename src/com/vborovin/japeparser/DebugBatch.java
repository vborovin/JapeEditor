package com.vborovin.japeparser;

import com.vborovin.japeparser.debuglog.DebugLog;
import gate.Document;
import gate.Gate;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.jape.ActionContext;
import gate.jape.JapeException;
import gate.jape.Transducer;
import gate.jape.parser.ParseCpsl;
import gate.jape.parser.ParseException;
import gate.util.GateClassLoader;

import java.io.StringReader;
import java.util.HashMap;

public class DebugBatch {
    private String encoding;
    private Transducer transducer;
    private ActionContext actionContext;
    private transient GateClassLoader classLoader = null;

    private DebugLog log;

    public DebugBatch(String encoding, DebugLog log) {
        this.encoding = encoding;
        this.log = log;
    }

    public void transduce(String grammar, Document doc) throws ParseException, ExecutionException, JapeException, ResourceInstantiationException {
        ParseCpsl parser = new ParseCpsl(new StringReader(grammar), new HashMap<>(), new HashMap<>());
        parser.setSptClass(DebugTransducer.class);
        DebugTransducer debugger = (DebugTransducer) parser.SinglePhaseTransducer(parser.JavaImportBlock());
        if (debugger != null) {
            debugger.setLog(log);
            debugger.finish(Gate.getClassLoader());
            debugger.transduce(doc);
        }
    }
}
