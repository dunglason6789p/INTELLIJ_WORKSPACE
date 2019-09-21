/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.concurrent;

import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import org.maltparser.concurrent.ConcurrentMaltParserModel;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.lw.helper.Utils;
import org.maltparser.core.options.OptionManager;

public final class ConcurrentMaltParserService {
    private static int optionContainerCounter = 0;

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
        return ConcurrentMaltParserService.initializeParserModel(mcoFile.toURI().toURL());
    }

    public static ConcurrentMaltParserModel initializeParserModel(URL mcoURL) throws MaltChainedException {
        ConcurrentMaltParserService.loadOptions();
        int optionContainer = ConcurrentMaltParserService.getNextOptionContainerCounter();
        String parserModelName = Utils.getInternalParserModelName(mcoURL);
        OptionManager.instance().parseCommandLine("-m parse", optionContainer);
        OptionManager.instance().loadOptions(optionContainer, Utils.getInputStreamReaderFromConfigFileEntry(mcoURL, parserModelName, "savedoptions.sop", "UTF-8"));
        return new ConcurrentMaltParserModel(optionContainer, mcoURL);
    }
}

