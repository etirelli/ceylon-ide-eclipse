package com.redhat.ceylon.eclipse.util;

import static com.redhat.ceylon.eclipse.imp.core.CeylonReferenceResolver.getIdentifyingNode;

import java.util.HashMap;
import java.util.Map;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.eclipse.imp.parser.IMessageHandler;

import com.redhat.ceylon.compiler.typechecker.analyzer.AnalysisWarning;
import com.redhat.ceylon.compiler.typechecker.parser.RecognitionError;
import com.redhat.ceylon.compiler.typechecker.tree.AnalysisMessage;
import com.redhat.ceylon.compiler.typechecker.tree.Message;
import com.redhat.ceylon.compiler.typechecker.tree.Node;
import com.redhat.ceylon.compiler.typechecker.tree.Visitor;

public abstract class ErrorVisitor extends Visitor {
    private final IMessageHandler handler;
    
    public ErrorVisitor(IMessageHandler handler) {
        this.handler = handler;
    }

    @Override
    public void visitAny(Node node) {
        super.visitAny(node);
        for (Message error: node.getErrors()) {
            String errorMessage = error.getMessage();
            int startOffset = 0;
            int endOffset = 0;
            int startCol = 0;
            int startLine = 0;

            Map<String, Object> attributes = new HashMap<String, Object>();
            if (error instanceof RecognitionError) {
                RecognitionError recognitionError = (RecognitionError) error;
                CommonToken token = (CommonToken) recognitionError
                        .getRecognitionException().token;
                if (token!=null) {
                    startOffset = token.getStartIndex();
                    endOffset = token.getStopIndex();
                    startCol = token.getCharPositionInLine();
                    startLine = token.getLine();
                }
            }
            if (error instanceof AnalysisMessage) {
                if (error instanceof AnalysisWarning &&
                            node.getUnit().getPackage().getQualifiedNameString()
                                    .startsWith("ceylon.language")) {
                    continue;
                }
                AnalysisMessage analysisMessage = (AnalysisMessage) error;
                Node errorNode = getIdentifyingNode(analysisMessage.getTreeNode());
                if (errorNode == null) {
                    errorNode = analysisMessage.getTreeNode();
                }
                Token token = errorNode.getToken();
                if (token!=null) {
                    startOffset = errorNode.getStartIndex();
                    endOffset = errorNode.getStopIndex();
                    startCol = token.getCharPositionInLine();
                    startLine = token.getLine();
                }
            }
            attributes.put("CeylonMessageClass", error.getClass().getSimpleName());
            attributes.put(IMessageHandler.SEVERITY_KEY, getSeverity(error));
            attributes.put(IMessageHandler.ERROR_CODE_KEY, error.getCode());

            handler.handleSimpleMessage(errorMessage, startOffset, endOffset,
                    startCol, startCol, startLine, startLine, attributes);
        }
    }

    public abstract int getSeverity(Message error);
}