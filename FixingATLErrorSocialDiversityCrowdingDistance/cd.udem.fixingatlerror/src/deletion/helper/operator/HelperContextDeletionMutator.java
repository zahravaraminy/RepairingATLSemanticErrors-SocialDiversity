package deletion.helper.operator;

import java.util.List;

import org.eclipse.m2m.atl.core.emf.EMFModel;
import witness.generator.MetaModel;
import anatlyzer.atl.model.ATLModel;
import anatlyzer.atlext.OCL.OclContextDefinition;
import anatlyzer.atlext.OCL.OclFeatureDefinition;
import cd.udem.fixingatlerror.CommonFunctionOperators;
import deletion.operator.AbstractDeletionMutator;
import jmetal.core.Solution;

public class HelperContextDeletionMutator extends AbstractDeletionMutator {

	@Override
	public List<Object> generateMutants(EMFModel atlModel, MetaModel inputMM, MetaModel outputMM, String outputFolder,
			ATLModel wrapper,
			Solution solution,CommonFunctionOperators cp) {
		
		return this.genericDeletion(atlModel, outputFolder, OclFeatureDefinition.class, OclContextDefinition.class, "context_",
				true,wrapper
				,solution, outputMM,cp);
	}

	@Override
	public String getDescription() {
		return "Deletion of Context";
	}	
}
