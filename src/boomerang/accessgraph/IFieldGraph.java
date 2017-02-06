package boomerang.accessgraph;

import java.util.Collection;
import java.util.Set;

interface IFieldGraph {
	Set<IFieldGraph> popFirstField();
	Set<IFieldGraph> popLastField();
	Collection<WrappedSootField> getEntryNode();
	WrappedSootField[] getFields();
	IFieldGraph appendFields(WrappedSootField[] toAppend);
	IFieldGraph append(IFieldGraph graph);
	IFieldGraph prependField(WrappedSootField f);
	Collection<WrappedSootField> getExitNode();
	boolean shouldOverApproximate();
	IFieldGraph overapproximation();
}
