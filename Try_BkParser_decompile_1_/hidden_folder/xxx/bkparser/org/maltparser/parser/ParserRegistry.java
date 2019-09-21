package org.maltparser.parser;

import org.maltparser.core.feature.AbstractFeatureFactory;
import org.maltparser.core.feature.FeatureRegistry;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTableHandler;

public class ParserRegistry implements FeatureRegistry {
   private AbstractFeatureFactory abstractParserFactory;
   private AlgoritmInterface algorithm;
   private SymbolTableHandler symbolTableHandler;
   private DataFormatInstance dataFormatInstance;
   private final HashMap<Class<?>, Object> registry = new HashMap();

   public ParserRegistry() {
   }

   public Object get(Class<?> key) {
      return this.registry.get(key);
   }

   public void put(Class<?> key, Object value) {
      this.registry.put(key, value);
      if (key == AbstractParserFactory.class) {
         this.abstractParserFactory = (AbstractParserFactory)value;
      } else if (key == AlgoritmInterface.class) {
         this.algorithm = (AlgoritmInterface)value;
      }

   }

   public AbstractFeatureFactory getFactory(Class<?> clazz) {
      return this.abstractParserFactory;
   }

   public SymbolTableHandler getSymbolTableHandler() {
      return this.symbolTableHandler;
   }

   public void setSymbolTableHandler(SymbolTableHandler symbolTableHandler) {
      this.symbolTableHandler = symbolTableHandler;
      this.registry.put(SymbolTableHandler.class, symbolTableHandler);
   }

   public DataFormatInstance getDataFormatInstance() {
      return this.dataFormatInstance;
   }

   public void setDataFormatInstance(DataFormatInstance dataFormatInstance) {
      this.dataFormatInstance = dataFormatInstance;
      this.registry.put(DataFormatInstance.class, dataFormatInstance);
   }

   public AbstractFeatureFactory getAbstractParserFeatureFactory() {
      return this.abstractParserFactory;
   }

   public void setAbstractParserFeatureFactory(AbstractParserFactory _abstractParserFactory) {
      this.registry.put(AbstractParserFactory.class, _abstractParserFactory);
      this.abstractParserFactory = _abstractParserFactory;
   }

   public AlgoritmInterface getAlgorithm() {
      return this.algorithm;
   }

   public void setAlgorithm(AlgoritmInterface algorithm) {
      this.registry.put(AlgoritmInterface.class, algorithm);
      this.algorithm = algorithm;
   }
}
