package org.maltparser.parser;

public interface AlgoritmInterface {
   ParserRegistry getParserRegistry();

   ParserConfiguration getCurrentParserConfiguration();

   DependencyParserConfig getManager();
}
