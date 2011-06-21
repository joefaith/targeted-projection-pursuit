package weka.attributeSelection;

import weka.core.Instances;

/** A dummy attribute evaluator to be used by TPPAttributeSearch */
public class TPPAttributeEvaluation extends ASEvaluation {

	@Override
	public void buildEvaluator(Instances data) throws Exception {
	}
	
	public int[] postProcess(int[] a){
		return a;
	}


	public String globalInfo() {
		return "A dummy attribute evaluation method to be used with TPPAttributeSearch.";
	}

}
