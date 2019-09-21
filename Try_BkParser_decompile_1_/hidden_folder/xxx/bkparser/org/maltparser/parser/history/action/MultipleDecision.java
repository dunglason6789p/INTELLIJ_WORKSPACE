package org.maltparser.parser.history.action;

import org.maltparser.core.exception.MaltChainedException;

public interface MultipleDecision extends GuideDecision {
   SingleDecision getSingleDecision(int var1) throws MaltChainedException;
}
