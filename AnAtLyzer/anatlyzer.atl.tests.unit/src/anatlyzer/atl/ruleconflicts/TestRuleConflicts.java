package anatlyzer.atl.ruleconflicts;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import anatlyzer.atl.analyser.batch.RuleConflictAnalysis.OverlappingRules;
import anatlyzer.atl.unit.UnitTest;

public class TestRuleConflicts extends UnitTest {
	String ABCD = metamodel("ABCD");
	String WXYZ = metamodel("WXYZ");
	
	@Test
	public void testRuleConflict_SuperTypes() throws Exception {
		String T = trafo("rule_conflicts_inheritance");
	//	typing(T, new Object[] { ABCD, WXYZ }, new String[] { "ABCD", "WXYZ" }, true);
		
		List<OverlappingRules> overlaps = possibleRuleConflicts();
		System.out.println(overlaps);
		assertEquals(2, overlaps.size());
		
		boolean[] confirmed = confirmOrDiscardRuleConflicts();
		assertEquals(1, count(confirmed));
	}

	
	@Test
	public void testRuleConflict_CommonSubtype() throws Exception {
		String T = trafo("rule_conflicts_common_subtype");
	//	typing(T, new Object[] { ABCD, WXYZ }, new String[] { "ABCD", "WXYZ" }, true);
		
		List<OverlappingRules> overlaps = possibleRuleConflicts();
		System.out.println(overlaps);
		assertEquals(1, overlaps.size());
		
		boolean[] confirmed = confirmOrDiscardRuleConflicts();
		assertEquals(1, count(confirmed));
	}

	private int count(boolean[] confirmed) {
		int s = 0;
		for(int i = 0; i < confirmed.length; i++) 
			s += confirmed[i] ? 1 : 0;
		return s;
	}
}
