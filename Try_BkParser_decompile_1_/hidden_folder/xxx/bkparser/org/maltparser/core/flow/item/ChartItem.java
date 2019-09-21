package org.maltparser.core.flow.item;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.flow.FlowChartInstance;
import org.maltparser.core.flow.spec.ChartItemSpecification;
import org.maltparser.core.flow.system.elem.ChartElement;

public abstract class ChartItem {
   protected FlowChartInstance flowChartinstance;
   protected ChartItemSpecification chartItemSpecification;
   public static final int CONTINUE = 1;
   public static final int TERMINATE = 2;
   public static final int NEWITERATION = 3;

   public ChartItem() {
   }

   public void initialize(FlowChartInstance flowChartinstance, ChartItemSpecification chartItemSpecification) throws MaltChainedException {
      this.setFlowChartInstance(flowChartinstance);
      this.setChartItemSpecification(chartItemSpecification);
   }

   public abstract int preprocess(int var1) throws MaltChainedException;

   public abstract int process(int var1) throws MaltChainedException;

   public abstract int postprocess(int var1) throws MaltChainedException;

   public abstract void terminate() throws MaltChainedException;

   public FlowChartInstance getFlowChartInstance() {
      return this.flowChartinstance;
   }

   protected void setFlowChartInstance(FlowChartInstance flowChartinstance) {
      this.flowChartinstance = flowChartinstance;
   }

   public int getOptionContainerIndex() {
      return this.flowChartinstance.getOptionContainerIndex();
   }

   public ChartElement getChartElement(String key) {
      return this.flowChartinstance.getFlowChartManager().getFlowChartSystem().getChartElement(key);
   }

   public ChartItemSpecification getChartItemSpecification() {
      return this.chartItemSpecification;
   }

   public void setChartItemSpecification(ChartItemSpecification chartItemSpecification) {
      this.chartItemSpecification = chartItemSpecification;
   }
}
