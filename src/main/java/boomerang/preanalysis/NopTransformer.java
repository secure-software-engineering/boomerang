package boomerang.preanalysis;

import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.jimple.internal.JNopStmt;

public class NopTransformer extends BodyTransformer {
	

	@Override
	protected void internalTransform(Body b, String phaseName,
			Map<String, String> options) {
		b.getUnits().addFirst(new JNopStmt());
	}

	

}
