package org.maltparser.concurrent;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.lw.helper.Utils;
import org.maltparser.core.options.OptionManager;

public final class ConcurrentMaltParserService {
   private static int optionContainerCounter = 0;

   public ConcurrentMaltParserService() {
   }

   private static synchronized int getNextOptionContainerCounter() {
      return optionContainerCounter++;
   }

   private static synchronized void loadOptions() throws MaltChainedException {
      if (!OptionManager.instance().hasOptions()) {
         OptionManager.instance().loadOptionDescriptionFile();
         OptionManager.instance().generateMaps();
      }

   }

   public static ConcurrentMaltParserModel initializeParserModel(File mcoFile) throws MaltChainedException, MalformedURLException {
      return initializeParserModel(mcoFile.toURI().toURL());
   }

   public static ConcurrentMaltParserModel initializeParserModel(URL mcoURL) throws MaltChainedException {
      loadOptions();
      int optionContainer = getNextOptionContainerCounter();
      String parserModelName = Utils.getInternalParserModelName(mcoURL);
      OptionManager.instance().parseCommandLine("-m parse", optionContainer);
      OptionManager.instance().loadOptions(optionContainer, Utils.getInputStreamReaderFromConfigFileEntry(mcoURL, parserModelName, "savedoptions.sop", "UTF-8"));
      return new ConcurrentMaltParserModel(optionContainer, mcoURL);
   }
}
